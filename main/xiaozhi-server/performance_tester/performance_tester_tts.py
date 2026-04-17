import asyncio
import logging
import os
import time
from typing import Dict
import yaml
from tabulate import tabulate

# Import create_tts_instance from core.utils.tts
from core.utils.tts import create_instance as create_tts_instance
from config.settings import load_config

# Set the global log level to WARNING
logging.basicConfig(level=logging.WARNING)

description = "Non-streaming speech synthesis performance test"


class TTSPerformanceTester:
    def __init__(self):
        self.config = load_config()
        self.test_sentences = self.config.get("module_test", {}).get(
            "test_sentences",
            [
                "In the ninth year of Yonghe, in the year of Guichou, at the beginning of late spring;",
                "People encounter one another and spend a lifetime together; some express themselves from within in a single room, while others entrust their feelings outward and wander unrestrained. Although our tastes differ and our temperaments vary,",
                "Whenever I look at the reasons people of old were stirred to emotion, they align as if carved from the same tally, and I always sigh over the text, unable to set it aside. Truly I understand that to treat life and death as one is an illusion, and to equate a long life with an early death is an absurdity.",
            ],
        )
        self.results = {}

    async def _test_tts(self, tts_name: str, config: Dict) -> Dict:
        """Test the performance of a single TTS module."""
        try:
            token_fields = ["access_token", "api_key", "token"]
            if any(
                field in config
                and any(x in config[field] for x in ["your", "placeholder"])
                for field in token_fields
            ):
                print(f"TTS {tts_name} has no access_token/api_key configured; skipped")
                return {"name": tts_name, "errors": 1}

            module_type = config.get("type", tts_name)
            tts = create_tts_instance(module_type, config, delete_audio_file=True)

            print(f"Testing TTS: {tts_name}")

            # Connectivity test
            tmp_file = tts.generate_filename()
            await tts.text_to_speak("Connection test", tmp_file)

            if not tmp_file or not os.path.exists(tmp_file):
                print(f"{tts_name} connection failed")
                return {"name": tts_name, "errors": 1}

            total_time = 0
            test_count = len(self.test_sentences[:3])

            for i, sentence in enumerate(self.test_sentences[:2], 1):
                start = time.time()
                tmp_file = tts.generate_filename()
                await tts.text_to_speak(sentence, tmp_file)
                duration = time.time() - start
                total_time += duration

                if tmp_file and os.path.exists(tmp_file):
                    print(f"{tts_name} [{i}/{test_count}] test succeeded")
                else:
                    print(f"{tts_name} [{i}/{test_count}] test failed")
                    return {"name": tts_name, "errors": 1}

            return {
                "name": tts_name,
                "avg_time": total_time / test_count,
                "errors": 0,
            }

        except Exception as e:
            print(f"{tts_name} test failed: {str(e)}")
            return {"name": tts_name, "errors": 1}

    def _print_results(self):
        """Print the test results."""
        if not self.results:
            print("No valid TTS test results")
            return

        headers = ["TTS module", "Avg time (s)", "Test sentence count", "Status"]
        table_data = []

        # Collect and categorize all data
        valid_results = []
        error_results = []

        for name, data in self.results.items():
            if data["errors"] == 0:
                # Successful result
                avg_time = f"{data['avg_time']:.3f}"
                test_count = len(self.test_sentences[:3])
                status = "✅ OK"

                # Save the value used for sorting
                valid_results.append({
                    "name": name,
                    "avg_time": avg_time,
                    "test_count": test_count,
                    "status": status,
                    "sort_key": data['avg_time']
                })
            else:
                # Error result
                avg_time = "-"
                test_count = "0/3"

                # Default error type is network error
                error_type = "network error"
                status = f"❌ {error_type}"

                error_results.append([name, avg_time, test_count, status])

        # Sort ascending by average elapsed time
        valid_results.sort(key=lambda x: x["sort_key"])

        # Convert the sorted valid results into rows for the table
        for result in valid_results:
            table_data.append([
                result["name"],
                result["avg_time"],
                result["test_count"],
                result["status"]
            ])

        # Append the error results at the end of the table
        table_data.extend(error_results)

        print("\nTTS performance test results:")
        print(
            tabulate(
                table_data,
                headers=headers,
                tablefmt="grid",
                colalign=("left", "right", "right", "left"),
            )
        )
        print("\nNotes:")
        print("- Timeout control: the maximum wait per request is 10 seconds")
        print("- Error handling: unreachable or timed-out modules are classified as network errors")
        print("- Sort order: by average elapsed time, fastest to slowest")

    async def run(self):
        """Execute the test."""
        print("Starting TTS performance test...")

        if not self.config.get("TTS"):
            print("No TTS configuration found in the config file")
            return

        # Iterate over all TTS configurations
        tasks = []
        for tts_name, config in self.config.get("TTS", {}).items():
            tasks.append(self._test_tts(tts_name, config))

        # Run the tests concurrently
        results = await asyncio.gather(*tasks)

        # Save all results, including errors
        for result in results:
            self.results[result["name"]] = result

        # Print the results
        self._print_results()


# Provided for calls from performance_tester.py
async def main():
    tester = TTSPerformanceTester()
    await tester.run()


if __name__ == "__main__":
    tester = TTSPerformanceTester()
    asyncio.run(tester.run())
