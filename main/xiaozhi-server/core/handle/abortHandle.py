import json
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from core.connection import ConnectionHandler
TAG = __name__


async def handleAbortMessage(conn: "ConnectionHandler"):
    if conn.close_after_chat or conn.is_exiting:
        conn.logger.bind(tag=TAG).info("Interrupted during exit flow; closing the connection directly")
        return

    conn.logger.bind(tag=TAG).info("Abort message received")
    # Set the abort flag; this automatically interrupts LLM/TTS tasks
    conn.client_abort = True
    conn.clear_queues()
    # Interrupt the client's speaking state
    await conn.websocket.send(
        json.dumps({"type": "tts", "state": "stop", "session_id": conn.session_id})
    )
    conn.clearSpeakStatus()
    conn.logger.bind(tag=TAG).info("Abort message received-end")
