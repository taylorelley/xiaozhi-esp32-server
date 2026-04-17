import asyncio
import logging
import os
import time
import concurrent.futures
from typing import Dict, Optional
import aiohttp
from tabulate import tabulate
from core.utils.asr import create_instance as create_stt_instance

# Set the global log level to WARNING to suppress INFO-level logs
logging.basicConfig(level=logging.WARNING)

description = "Speech recognition model performance test"

class ASRPerformanceTester:
    def __init__(self):
        self.config = self._load_config_from_data_dir()
        self.test_wav_list = self._load_test_wav_files()
        self.results = {"stt": {}}

        # Debug logs
        print(f"[DEBUG] Loaded ASR config: {self.config.get('ASR', {})}")
        print(f"[DEBUG] Audio file count: {len(self.test_wav_list)}")

    def _load_config_from_data_dir(self) -> Dict:
        """Load all .config.yaml configurations from the data directory"""
        config = {"ASR": {}}
        data_dir = os.path.join(os.getcwd(), "data")
        print(f"[DEBUG] Scanning config directory: {data_dir}")

        for root, _, files in os.walk(data_dir):
            for file in files:
                if file.endswith(".config.yaml"):
                    file_path = os.path.join(root, file)
                    try:
                        with open(file_path, "r", encoding="utf-8") as f:
                            import yaml
                            file_config = yaml.safe_load(f)
                            # Accept both ASR and asr casing
                            asr_config = file_config.get("ASR") or file_config.get("asr")
                            if asr_config:
                                config["ASR"].update(asr_config)
                                print(f"[DEBUG] Loaded ASR config from {file_path} successfully")
                    except Exception as e:
                        print(f" Failed to load config file {file_path}: {str(e)}")
        return config

    def _load_test_wav_files(self) -> list:
        """Load test audio files (with path debugging)"""
        wav_root = os.path.join(os.getcwd(), "config", "assets")
        print(f"[DEBUG] Audio file directory: {wav_root}")
        test_wav_list = []

        if os.path.exists(wav_root):
            file_list = os.listdir(wav_root)
            print(f"[DEBUG] Found audio files: {file_list}")
            for file_name in file_list:
                file_path = os.path.join(wav_root, file_name)
                if os.path.getsize(file_path) > 300 * 1024:  # 300KB
                    with open(file_path, "rb") as f:
                        test_wav_list.append(f.read())
        else:
            print(f" Directory does not exist: {wav_root}")
        return test_wav_list

    async def _test_single_audio(self, stt_name: str, stt, audio_data: bytes) -> Optional[float]:
        """Test the performance of a single audio file"""
        try:
            start_time = time.time()
            text, _ = await stt.speech_to_text([audio_data], "1", stt.audio_format)
            if text is None:
                return None

            duration = time.time() - start_time

            # Detect anomalous timings of 0.000s
            if abs(duration) < 0.001:  # less than 1 millisecond is considered anomalous
                print(f"{stt_name} detected anomalous time: {duration:.6f}s (treated as error)")
                return None

            return duration
        except Exception as e:
            error_msg = str(e).lower()
            if "502" in error_msg or "bad gateway" in error_msg:
                print(f"{stt_name} encountered a 502 error")
                return None
            return None

    async def _test_stt_with_timeout(self, stt_name: str, config: Dict) -> Dict:
        """Async test of a single STT with timeout control"""
        try:
            # Check config validity
            token_fields = ["access_token", "api_key", "token"]
            if any(
                field in config
                and str(config[field]).lower() in ["your", "placeholder", "none", "null", ""]
                for field in token_fields
            ):
                print(f"  STT {stt_name} has no valid access_token/api_key, skipping")
                return {
                    "name": stt_name,
                    "type": "stt",
                    "errors": 1,
                    "error_type": "Configuration error"
                }

            module_type = config.get("type", stt_name)
            stt = create_stt_instance(module_type, config, delete_audio_file=True)
            stt.audio_format = "pcm"

            print(f" Testing STT: {stt_name}")

            # Use a thread pool with timeout control
            loop = asyncio.get_event_loop()

            # Use the first audio file as a connectivity check
            try:
                with concurrent.futures.ThreadPoolExecutor() as executor:
                    future = executor.submit(
                        lambda: asyncio.run(self._test_single_audio(stt_name, stt, self.test_wav_list[0]))
                    )
                    first_result = await asyncio.wait_for(
                        asyncio.wrap_future(future), timeout=10.0
                    )

                    if first_result is None:
                        print(f" {stt_name} connection failed")
                        return {
                            "name": stt_name,
                            "type": "stt",
                            "errors": 1,
                            "error_type": "Network error"
                        }
            except asyncio.TimeoutError:
                print(f" {stt_name} connection timed out (10s), skipping")
                return {
                    "name": stt_name,
                    "type": "stt",
                    "errors": 1,
                    "error_type": "Connection timeout"
                }
            except Exception as e:
                error_msg = str(e).lower()
                if "502" in error_msg or "bad gateway" in error_msg:
                    print(f" {stt_name} encountered a 502 error, skipping")
                    return {
                        "name": stt_name,
                        "type": "stt",
                        "errors": 1,
                        "error_type": "502 network error"
                    }
                print(f" {stt_name} connection exception: {str(e)}")
                return {
                    "name": stt_name,
                    "type": "stt",
                    "errors": 1,
                    "error_type": "Network error"
                }

                       # Full test with timeout control
            total_time = 0
            valid_tests = 0
            test_count = len(self.test_wav_list)

            for i, audio_data in enumerate(self.test_wav_list, 1):
                try:
                    with concurrent.futures.ThreadPoolExecutor() as executor:
                        future = executor.submit(
                            lambda: asyncio.run(self._test_single_audio(stt_name, stt, audio_data))
                        )
                        duration = await asyncio.wait_for(
                            asyncio.wrap_future(future), timeout=10.0
                        )

                        if duration is not None and duration > 0.001:
                            total_time += duration
                            valid_tests += 1
                            print(f" {stt_name} [{i}/{test_count}] elapsed: {duration:.2f}s")
                        else:
                            print(f" {stt_name} [{i}/{test_count}] test failed (includes 0.000s anomaly)")

                except asyncio.TimeoutError:
                    print(f" {stt_name} [{i}/{test_count}] timed out (10s), skipping")
                    continue
                except Exception as e:
                    error_msg = str(e).lower()
                    if "502" in error_msg or "bad gateway" in error_msg:
                        print(f" {stt_name} [{i}/{test_count}] 502 error, skipping")
                        return {
                            "name": stt_name,
                            "type": "stt",
                            "errors": 1,
                            "error_type": "502 network error"
                        }
                    print(f" {stt_name} [{i}/{test_count}] exception: {str(e)}")
                    continue
            # Check the number of valid runs
            if valid_tests < test_count * 0.3:  # require at least a 30% success rate
                print(f" {stt_name} too few successful runs ({valid_tests}/{test_count}); network may be unstable")
                return {
                    "name": stt_name,
                    "type": "stt",
                    "errors": 1,
                    "error_type": "Network error"
                }

            if valid_tests == 0:
                return {
                    "name": stt_name,
                    "type": "stt",
                    "errors": 1,
                    "error_type": "Network error"
                }

            avg_time = total_time / valid_tests
            return {
                "name": stt_name,
                "type": "stt",
                "avg_time": avg_time,
                "success_rate": f"{valid_tests}/{test_count}",
                "errors": 0,
            }

        except Exception as e:
            error_msg = str(e).lower()
            if "502" in error_msg or "bad gateway" in error_msg:
                error_type = "502网络错误"
            elif "timeout" in error_msg:
                error_type = "超时连接"
            else:
                error_type = "网络错误"
            print(f"⚠️ {stt_name} 测试失败: {str(e)}")
            return {
                "name": stt_name,
                "type": "stt",
                "errors": 1,
                "error_type": error_type
            }

    def _print_results(self):
        """打印测试结果，按响应时间排序"""
        print("\n" + "=" * 50)
        print("ASR 性能测试结果")
        print("=" * 50)

        if not self.results.get("stt"):
            print("没有可用的测试结果")
            return

        headers = ["模型名称", "平均耗时(s)", "成功率", "状态"]
        table_data = []

        # 收集所有数据并分类
        valid_results = []
        error_results = []

        for name, data in self.results["stt"].items():
            if data["errors"] == 0:
                # 正常结果
                avg_time = f"{data['avg_time']:.3f}"
                success_rate = data.get("success_rate", "N/A")
                status = "✅ 正常"
                
                # 保存用于排序的值
                sort_key = data["avg_time"]
                
                valid_results.append({
                    "name": name,
                    "avg_time": avg_time,
                    "success_rate": success_rate,
                    "status": status,
                    "sort_key": sort_key,
                })
            else:
                # 错误结果
                avg_time = "-"
                success_rate = "0/N"
                
                # 获取具体错误类型
                error_type = data.get("error_type", "网络错误")
                status = f"❌ {error_type}"
                
                error_results.append([name, avg_time, success_rate, status])

        # 按响应时间升序排序（从快到慢）
        valid_results.sort(key=lambda x: x["sort_key"])

        # 将排序后的有效结果转换为表格数据
        for result in valid_results:
            table_data.append([
                result["name"],
                result["avg_time"],
                result["success_rate"],
                result["status"],
            ])

        # 将错误结果添加到表格数据末尾
        table_data.extend(error_results)

        print(tabulate(table_data, headers=headers, tablefmt="grid"))
        print("\n测试说明:")
        print("- 超时控制：单个音频最大等待时间为10秒")
        print("- 错误处理：自动跳过502错误、超时和网络异常的模型")
        print("- 成功率：成功识别的音频数量/总测试音频数量")
        print("- 排序规则：按平均耗时从快到慢排序，错误模型排最后")
        print("\n测试完成！")

    async def run(self):
        """执行全量异步测试""" 
        print("开始筛选可用ASR模块...")
        if not self.config.get("ASR"):
            print("配置中未找到 ASR 模块")
            return

        all_tasks = []
        for stt_name, config in self.config["ASR"].items():
            # 检查配置有效性
            token_fields = ["access_token", "api_key", "token"]
            if any(
                field in config
                and str(config[field]).lower() in ["你的", "placeholder", "none", "null", ""]
                for field in token_fields
            ):
                print(f"ASR {stt_name} 未配置有效access_token/api_key，已跳过")
                continue
            
            print(f"添加 ASR 测试任务: {stt_name}")
            all_tasks.append(self._test_stt_with_timeout(stt_name, config))

        if not all_tasks:
            print("没有可用的ASR模块进行测试。")
            return

        print(f"\n找到 {len(all_tasks)} 个可用ASR模块")
        print("\n开始并发测试所有ASR模块...")
        all_results = await asyncio.gather(*all_tasks, return_exceptions=True)

        # 处理结果
        for result in all_results:
            if isinstance(result, dict) and result.get("type") == "stt":
                self.results["stt"][result["name"]] = result

        # 打印结果
        self._print_results()


async def main():
    tester = ASRPerformanceTester()
    await tester.run()


if __name__ == "__main__":
    asyncio.run(main())