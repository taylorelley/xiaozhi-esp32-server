import json
import time
import asyncio
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from core.connection import ConnectionHandler
from core.utils import textUtils
from core.utils.util import audio_to_data
from core.providers.tts.dto.dto import SentenceType
from core.utils.audioRateController import AudioRateController

TAG = __name__
# Audio frame duration (milliseconds)
AUDIO_FRAME_DURATION = 60
# Number of pre-buffered packets, sent directly to reduce latency
PRE_BUFFER_COUNT = 5


async def sendAudioMessage(conn: "ConnectionHandler", sentenceType, audios, text, sentence_id=None):
    # Skip leftover audio from old sentences
    if sentence_id is not None and sentence_id != conn.sentence_id:
        return

    if conn.tts.tts_audio_first_sentence:
        conn.logger.bind(tag=TAG).info(f"Sending first voice segment: {text}")
        conn.tts.tts_audio_first_sentence = False

    if sentenceType == SentenceType.FIRST:
        # Subsequent messages of the same sentence are added to the flow-control queue; otherwise send immediately
        if (
            hasattr(conn, "audio_rate_controller")
            and conn.audio_rate_controller
            and getattr(conn, "audio_flow_control", {}).get("sentence_id")
            == conn.sentence_id
        ):
            conn.audio_rate_controller.add_message(
                lambda: send_tts_message(conn, "sentence_start", text)
            )
        else:
            # New sentence or flow controller not initialized, send immediately
            await send_tts_message(conn, "sentence_start", text)

    await sendAudio(conn, audios)
    # Send sentence start message
    if sentenceType is not SentenceType.MIDDLE:
        conn.logger.bind(tag=TAG).info(f"Sending audio message: {sentenceType}, {text}")

    # Send end message (if it is the last text)
    if sentenceType == SentenceType.LAST:
        await send_tts_message(conn, "stop", None)
        if conn.close_after_chat:
            await conn.close()


async def _wait_for_audio_completion(conn: "ConnectionHandler"):
    """
    Wait for the audio queue to drain and for pre-buffered packets to finish playing

    Args:
        conn: connection object
    """
    if hasattr(conn, "audio_rate_controller") and conn.audio_rate_controller:
        rate_controller = conn.audio_rate_controller
        conn.logger.bind(tag=TAG).debug(
            f"Waiting for audio send completion, {len(rate_controller.queue)} packets remaining in queue"
        )
        await rate_controller.queue_empty_event.wait()

        # Wait for pre-buffered packets to finish playing
        # The first N packets are sent directly, plus 2 extra network jitter packets, so we need to wait for them to finish playing on the client
        frame_duration_ms = rate_controller.frame_duration
        pre_buffer_playback_time = (PRE_BUFFER_COUNT + 2) * frame_duration_ms / 1000.0
        await asyncio.sleep(pre_buffer_playback_time)

        conn.logger.bind(tag=TAG).debug("Audio send completed")


async def _send_to_mqtt_gateway(
    conn: "ConnectionHandler", opus_packet, timestamp, sequence
):
    """
    Send opus packet with a 16-byte header to mqtt_gateway
    Args:
        conn: connection object
        opus_packet: opus packet
        timestamp: timestamp
        sequence: sequence number
    """
    # Add a 16-byte header to the opus packet
    header = bytearray(16)
    header[0] = 1  # type
    header[2:4] = len(opus_packet).to_bytes(2, "big")  # payload length
    header[4:8] = sequence.to_bytes(4, "big")  # sequence
    header[8:12] = timestamp.to_bytes(4, "big")  # timestamp
    header[12:16] = len(opus_packet).to_bytes(4, "big")  # opus length

    # Send the complete packet including the header
    complete_packet = bytes(header) + opus_packet
    await conn.websocket.send(complete_packet)


async def sendAudio(
    conn: "ConnectionHandler", audios, frame_duration=AUDIO_FRAME_DURATION
):
    """
    Send audio packets using AudioRateController for precise flow control

    Args:
        conn: connection object
        audios: a single opus packet (bytes) or a list of opus packets
        frame_duration: frame duration (milliseconds); defaults to the global constant AUDIO_FRAME_DURATION
    """
    if audios is None or len(audios) == 0:
        return

    send_delay = conn.config.get("tts_audio_send_delay", -1) / 1000.0
    is_single_packet = isinstance(audios, bytes)

    # Initialize or obtain the RateController
    rate_controller, flow_control = _get_or_create_rate_controller(
        conn, frame_duration, is_single_packet
    )

    # Convert to a list for uniform handling
    audio_list = [audios] if is_single_packet else audios

    # Send audio packets
    await _send_audio_with_rate_control(
        conn, audio_list, rate_controller, flow_control, send_delay
    )


def _get_or_create_rate_controller(
    conn: "ConnectionHandler", frame_duration, is_single_packet
):
    """
    Get or create a RateController and flow_control

    Args:
        conn: connection object
        frame_duration: frame duration
        is_single_packet: whether in single-packet mode (True: TTS streaming single packet, False: batch packets)

    Returns:
        (rate_controller, flow_control)
    """
    # Check whether the controller needs to be reset
    need_reset = False

    if not hasattr(conn, "audio_rate_controller"):
        # Controller does not exist; needs to be created
        need_reset = True
    else:
        rate_controller = conn.audio_rate_controller

        # If the background send task has stopped, a reset is needed
        if (
            not rate_controller.pending_send_task
            or rate_controller.pending_send_task.done()
        ):
            need_reset = True
        # When sentence_id changes, a reset is needed
        elif (
            getattr(conn, "audio_flow_control", {}).get("sentence_id")
            != conn.sentence_id
        ):
            need_reset = True

    if need_reset:
        # Create or obtain the rate_controller
        if not hasattr(conn, "audio_rate_controller"):
            conn.audio_rate_controller = AudioRateController(frame_duration)
        else:
            conn.audio_rate_controller.reset()

        # Initialize flow_control
        conn.audio_flow_control = {
            "packet_count": 0,
            "sequence": 0,
            "sentence_id": conn.sentence_id,
        }

        # Start the background send loop
        _start_background_sender(
            conn, conn.audio_rate_controller, conn.audio_flow_control
        )

    return conn.audio_rate_controller, conn.audio_flow_control


def _start_background_sender(conn: "ConnectionHandler", rate_controller, flow_control):
    """
    Start the background send loop task

    Args:
        conn: connection object
        rate_controller: rate controller
        flow_control: flow control state
    """

    async def send_callback(packet):
        # Check whether it should be aborted
        if conn.client_abort:
            raise asyncio.CancelledError("Client aborted")

        conn.last_activity_time = time.time() * 1000
        await _do_send_audio(conn, packet, flow_control)

    # Use start_sending to launch the background loop
    rate_controller.start_sending(send_callback)


async def _send_audio_with_rate_control(
    conn: "ConnectionHandler", audio_list, rate_controller, flow_control, send_delay
):
    """
    Send audio packets using rate_controller

    Args:
        conn: connection object
        audio_list: list of audio packets
        rate_controller: rate controller
        flow_control: flow control state
        send_delay: fixed delay (seconds); -1 means dynamic flow control
    """
    for packet in audio_list:
        if conn.client_abort:
            return

        conn.last_activity_time = time.time() * 1000

        # Pre-buffer: the first N packets are sent directly
        if flow_control["packet_count"] < PRE_BUFFER_COUNT:
            await _do_send_audio(conn, packet, flow_control)
        elif send_delay > 0:
            # Fixed delay mode
            await asyncio.sleep(send_delay)
            await _do_send_audio(conn, packet, flow_control)
        else:
            # Dynamic flow control mode: only add to the queue; the background loop handles sending
            rate_controller.add_audio(packet)


async def _do_send_audio(conn: "ConnectionHandler", opus_packet, flow_control):
    """
    Perform the actual audio send
    """
    packet_index = flow_control.get("packet_count", 0)
    sequence = flow_control.get("sequence", 0)

    if conn.conn_from_mqtt_gateway:
        # Compute the timestamp (based on playback position)
        start_time = time.time()
        timestamp = int(start_time * 1000) % (2**32)
        await _send_to_mqtt_gateway(conn, opus_packet, timestamp, sequence)
    else:
        # Send the opus packet directly
        await conn.websocket.send(opus_packet)

    # Update flow-control state
    flow_control["packet_count"] = packet_index + 1
    flow_control["sequence"] = sequence + 1


async def send_tts_message(conn: "ConnectionHandler", state, text=None):
    """Send a TTS status message"""
    if text is None and state == "sentence_start":
        return
    message = {"type": "tts", "state": state, "session_id": conn.session_id}
    if text is not None:
        message["text"] = textUtils.check_emoji(text)

    # TTS playback ended
    if state == "stop":
        # Save the current sentence_id to later check whether it is still the current round
        current_sentence_id = conn.sentence_id
        # Play the notification tone
        tts_notify = conn.config.get("enable_stop_tts_notify", False)
        if tts_notify:
            stop_tts_notify_voice = conn.config.get(
                "stop_tts_notify_voice", "config/assets/tts_notify.mp3"
            )
            audios = await audio_to_data(stop_tts_notify_voice, is_opus=True)
            await sendAudio(conn, audios)
        # Wait for all audio packets to be sent
        await _wait_for_audio_completion(conn)

        # Check whether it is still the current round
        if current_sentence_id != conn.sentence_id:
            return

        # Stop the audio send loop (only when the flow controller has been initialized)
        if hasattr(conn, "audio_rate_controller") and conn.audio_rate_controller:
            conn.audio_rate_controller.stop_sending()
        conn.clearSpeakStatus()

    # Send the message to the client
    await conn.websocket.send(json.dumps(message))


async def send_stt_message(conn: "ConnectionHandler", text):
    """Send an STT status message"""
    end_prompt_str = conn.config.get("end_prompt", {}).get("prompt")
    if end_prompt_str and end_prompt_str == text:
        await send_tts_message(conn, "start")
        return

    # Parse JSON format and extract the actual user speech content
    display_text = text
    try:
        # Try to parse JSON format
        if text.strip().startswith("{") and text.strip().endswith("}"):
            parsed_data = json.loads(text)
            if isinstance(parsed_data, dict) and "content" in parsed_data:
                # If it is a JSON format containing speaker info, only display the content part
                display_text = parsed_data["content"]
                # Save speaker info to the conn object
                if "speaker" in parsed_data:
                    conn.current_speaker = parsed_data["speaker"]
    except (json.JSONDecodeError, TypeError):
        # If not JSON, use the original text directly
        display_text = text
    stt_text = textUtils.get_string_no_punctuation_or_emoji(display_text)
    await conn.websocket.send(
        json.dumps({"type": "stt", "text": stt_text, "session_id": conn.session_id})
    )
    await send_tts_message(conn, "start")
    # After the start message is sent the client enters speaking state; sync the server state
    conn.client_is_speaking = True


async def send_display_message(conn: "ConnectionHandler", text):
    """Send a display-only message"""
    message = {
        "type": "stt",
        "text": text,
        "session_id": conn.session_id
    }
    await conn.websocket.send(json.dumps(message))
