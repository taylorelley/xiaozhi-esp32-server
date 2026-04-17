import time
import json
import asyncio
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from core.connection import ConnectionHandler
from core.utils.util import audio_to_data
from core.handle.abortHandle import handleAbortMessage
from core.handle.intentHandler import handle_user_intent
from core.utils.output_counter import check_device_output_limit
from core.handle.sendAudioHandle import send_stt_message, SentenceType

TAG = __name__


async def handleAudioMessage(conn: "ConnectionHandler", audio):
    if conn.is_exiting:
        return
    # Whether someone is speaking in the current segment
    have_voice = conn.vad.is_vad(conn, audio)
    # If the device has just been woken up, briefly ignore VAD detection
    if hasattr(conn, "just_woken_up") and conn.just_woken_up:
        have_voice = False
        # Schedule a brief delay before resuming VAD detection
        if not hasattr(conn, "vad_resume_task") or conn.vad_resume_task.done():
            conn.vad_resume_task = asyncio.create_task(resume_vad_detection(conn))
        return
    # Long-idle detection, used to say goodbye
    await no_voice_close_connect(conn, have_voice)
    # Receive audio
    await conn.asr.receive_audio(conn, audio, have_voice)


async def resume_vad_detection(conn: "ConnectionHandler"):
    # Resume VAD detection after waiting 2 seconds
    await asyncio.sleep(2)
    conn.just_woken_up = False


async def startToChat(conn: "ConnectionHandler", text):
    # Check whether the input is in JSON format (containing speaker info)
    speaker_name = None
    language_tag = None
    actual_text = text

    try:
        # Try to parse JSON-formatted input
        if text.strip().startswith("{") and text.strip().endswith("}"):
            data = json.loads(text)
            if "speaker" in data and "content" in data:
                speaker_name = data["speaker"]
                language_tag = data["language"]
                actual_text = data["content"]
                conn.logger.bind(tag=TAG).info(f"Parsed speaker info: {speaker_name}")

                # Use the JSON-formatted text directly without parsing
                actual_text = text
    except (json.JSONDecodeError, KeyError):
        # If parsing fails, continue using the original text
        pass

    # Save speaker info on the connection object
    if speaker_name:
        conn.current_speaker = speaker_name
    else:
        conn.current_speaker = None

    if conn.need_bind:
        await check_bind_device(conn)
        return

    # If today's output character count exceeds the configured limit
    if conn.max_output_size > 0:
        if check_device_output_limit(
            conn.headers.get("device-id"), conn.max_output_size
        ):
            await max_out_size(conn)
            return

    # In manual mode, do not interrupt currently playing content
    if conn.client_is_speaking and conn.client_listen_mode != "manual":
        await handleAbortMessage(conn)

    # First perform intent analysis using the actual text content
    intent_handled = await handle_user_intent(conn, actual_text)

    if intent_handled:
        # If the intent has been handled, do not continue chatting
        return

    # Intent not handled; continue the regular chat flow using the actual text content
    await send_stt_message(conn, actual_text)

    # Prepare to start a new session
    conn.client_abort = False

    conn.executor.submit(conn.chat, actual_text)


async def no_voice_close_connect(conn: "ConnectionHandler", have_voice):
    if have_voice:
        conn.last_activity_time = time.time() * 1000
        return
    # Only perform timeout check once the timestamp has been initialized
    if conn.last_activity_time > 0.0:
        no_voice_time = time.time() * 1000 - conn.last_activity_time
        close_connection_no_voice_time = int(
            conn.config.get("close_connection_no_voice_time", 120)
        )
        if (
            not conn.close_after_chat
            and no_voice_time > 1000 * close_connection_no_voice_time
        ):
            conn.close_after_chat = True
            conn.client_abort = False
            end_prompt = conn.config.get("end_prompt", {})
            if end_prompt and end_prompt.get("enable", True) is False:
                conn.logger.bind(tag=TAG).info("Ending conversation; no farewell prompt needed")
                await conn.close()
                return
            prompt = end_prompt.get("prompt")
            if not prompt:
                prompt = "Please begin with ```Time flies``` and end this conversation with heartfelt, reluctant words."
            await startToChat(conn, prompt)


async def max_out_size(conn: "ConnectionHandler"):
    # Play the notice for exceeding the maximum output size
    conn.client_abort = False
    text = "Sorry, I have something to do right now. Let's chat again tomorrow at this time, it's a promise! See you tomorrow, bye!"
    await send_stt_message(conn, text)
    file_path = "config/assets/max_output_size.wav"
    opus_packets = await audio_to_data(file_path)
    conn.tts.tts_audio_queue.put((SentenceType.LAST, opus_packets, text))
    conn.close_after_chat = True


async def check_bind_device(conn: "ConnectionHandler"):
    if conn.bind_code:
        # Ensure bind_code is 6 digits
        if len(conn.bind_code) != 6:
            conn.logger.bind(tag=TAG).error(f"Invalid bind code format: {conn.bind_code}")
            text = "The bind code format is invalid. Please check the configuration."
            await send_stt_message(conn, text)
            return

        text = f"Please log in to the control panel and enter {conn.bind_code} to bind the device."
        await send_stt_message(conn, text)

        # Play notification tone
        music_path = "config/assets/bind_code.wav"
        opus_packets = await audio_to_data(music_path)
        conn.tts.tts_audio_queue.put((SentenceType.FIRST, opus_packets, text))

        # Play each digit one by one
        for i in range(6):  # Ensure only 6 digits are played
            try:
                digit = conn.bind_code[i]
                num_path = f"config/assets/bind_code/{digit}.wav"
                num_packets = await audio_to_data(num_path)
                conn.tts.tts_audio_queue.put((SentenceType.MIDDLE, num_packets, None))
            except Exception as e:
                conn.logger.bind(tag=TAG).error(f"Failed to play digit audio: {e}")
                continue
        conn.tts.tts_audio_queue.put((SentenceType.LAST, [], None))
    else:
        # Play the not-bound prompt
        conn.client_abort = False
        text = f"Version information for this device was not found. Please configure the OTA URL correctly and re-flash the firmware."
        await send_stt_message(conn, text)
        music_path = "config/assets/bind_not_found.wav"
        opus_packets = await audio_to_data(music_path)
        conn.tts.tts_audio_queue.put((SentenceType.LAST, opus_packets, text))
