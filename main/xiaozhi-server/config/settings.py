import os
from config.config_loader import read_config, get_project_dir, load_config


default_config_file = "config.yaml"
config_file_valid = False


def check_config_file():
    global config_file_valid
    if config_file_valid:
        return
    """
    Simplified configuration check that just reports how the config file is being used.
    """
    custom_config_file = get_project_dir() + "data/." + default_config_file
    if not os.path.exists(custom_config_file):
        raise FileNotFoundError(
            "Could not find data/.config.yaml. Please follow the guide to make sure the file exists."
        )

    # Check whether the configuration is loaded from the API
    config = load_config()
    if config.get("read_config_from_api", False):
        print("Loading configuration from the API")
        old_config_origin = read_config(custom_config_file)
        if old_config_origin.get("selected_module") is not None:
            error_msg = "Your configuration file seems to contain both the management-console config and the local config:\n"
            error_msg += "\nSuggested steps:\n"
            error_msg += "1. Copy config_from_api.yaml from the project root to data/ and rename it to .config.yaml\n"
            error_msg += "2. Follow the guide to configure the API endpoint and secret\n"
            raise ValueError(error_msg)
    config_file_valid = True
