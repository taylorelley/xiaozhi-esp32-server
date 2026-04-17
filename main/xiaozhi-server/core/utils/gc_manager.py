"""
Global GC management module.
Runs garbage collection on a timer to avoid the GIL contention caused by frequent GCs.
"""

import gc
import asyncio
import threading
from config.logger import setup_logging

TAG = __name__
logger = setup_logging()


class GlobalGCManager:
    """Global garbage collection manager."""

    def __init__(self, interval_seconds=300):
        """
        Initialize the GC manager.

        Args:
            interval_seconds: GC interval in seconds; defaults to 300 (5 minutes).
        """
        self.interval_seconds = interval_seconds
        self._task = None
        self._stop_event = asyncio.Event()
        self._lock = threading.Lock()

    async def start(self):
        """Start the scheduled GC task."""
        if self._task is not None:
            logger.bind(tag=TAG).warning("GC manager is already running")
            return

        logger.bind(tag=TAG).info(f"Starting the global GC manager with interval {self.interval_seconds}s")
        self._stop_event.clear()
        self._task = asyncio.create_task(self._gc_loop())

    async def stop(self):
        """Stop the scheduled GC task."""
        if self._task is None:
            return

        logger.bind(tag=TAG).info("Stopping the global GC manager")
        self._stop_event.set()

        if self._task and not self._task.done():
            self._task.cancel()
            try:
                await self._task
            except asyncio.CancelledError:
                pass

        self._task = None

    async def _gc_loop(self):
        """GC loop task."""
        try:
            while not self._stop_event.is_set():
                # Wait for the configured interval
                try:
                    await asyncio.wait_for(
                        self._stop_event.wait(), timeout=self.interval_seconds
                    )
                    # If stop_event was set, exit the loop
                    break
                except asyncio.TimeoutError:
                    # Timeout means it's time to run GC
                    pass

                # Run GC
                await self._run_gc()

        except asyncio.CancelledError:
            logger.bind(tag=TAG).info("GC loop task was cancelled")
            raise
        except Exception as e:
            logger.bind(tag=TAG).error(f"GC loop task exception: {e}")
        finally:
            logger.bind(tag=TAG).info("GC loop task has exited")

    async def _run_gc(self):
        """Run garbage collection."""
        try:
            # Run GC in a thread pool to avoid blocking the event loop
            loop = asyncio.get_running_loop()

            def do_gc():
                with self._lock:
                    before = len(gc.get_objects())
                    collected = gc.collect()
                    after = len(gc.get_objects())
                    return before, collected, after

            before, collected, after = await loop.run_in_executor(None, do_gc)
            logger.bind(tag=TAG).debug(
                f"Global GC run complete - objects collected: {collected}, "
                f"object count: {before} -> {after}"
            )
        except Exception as e:
            logger.bind(tag=TAG).error(f"Error while running GC: {e}")


# Global singleton
_gc_manager_instance = None


def get_gc_manager(interval_seconds=300):
    """
    Get the global GC manager instance (singleton).

    Args:
        interval_seconds: GC interval in seconds; defaults to 300 (5 minutes).

    Returns:
        A GlobalGCManager instance.
    """
    global _gc_manager_instance
    if _gc_manager_instance is None:
        _gc_manager_instance = GlobalGCManager(interval_seconds)
    return _gc_manager_instance
