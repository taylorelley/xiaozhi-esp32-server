#!/bin/bash
# Script author @VanillaNahida
# This file is used to automatically download all required files for this project with one click, and to create the necessary directories automatically.
# Only Debian/Ubuntu on x86_64 is officially tested; other systems may work but are not supported.

set -o pipefail

# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------
INSTALL_DIR="/opt/xiaozhi-server"
DATA_DIR="${INSTALL_DIR}/data"
MODEL_DIR="${INSTALL_DIR}/models/SenseVoiceSmall"
COMPOSE_FILE="${INSTALL_DIR}/docker-compose_all.yml"
CONFIG_FILE="${DATA_DIR}/.config.yaml"
MODEL_FILE="${MODEL_DIR}/model.pt"

# Default host-side port for the device WebSocket service. Port 8000 is
# commonly used by other dev tools (Django runserver, python -m http.server,
# etc.), so we default to 18000 and let the user override interactively.
# The container still listens on 8000 internally.
DEFAULT_WS_HOST_PORT=18000
WS_HOST_PORT="$DEFAULT_WS_HOST_PORT"

RAW_BASE="https://raw.githubusercontent.com/taylorelley/xiaozhi-esp32-server/refs/heads/main"

COMPOSE_PATH_REL="main/xiaozhi-server/docker-compose_all.yml"
CONFIG_PATH_REL="main/xiaozhi-server/config_from_api.yaml"

MODEL_URL="https://huggingface.co/FunAudioLLM/SenseVoiceSmall/resolve/main/model.pt"

# Source tree used to build the server and web images locally instead of pulling them.
SRC_DIR="${INSTALL_DIR}/src"
SRC_TARBALL_URL="https://github.com/taylorelley/xiaozhi-esp32-server/archive/refs/heads/main.tar.gz"
LOCAL_SERVER_IMAGE="xiaozhi-esp32-server-local:latest"
LOCAL_WEB_IMAGE="xiaozhi-esp32-server-web-local:latest"

COMPOSE=""            # resolved later to "docker compose" or "docker-compose"
UPGRADE_COMPLETED=""

# ---------------------------------------------------------------------------
# Interrupt handling
# ---------------------------------------------------------------------------
handle_interrupt() {
    echo ""
    echo "Installation interrupted by user (Ctrl+C)"
    echo "To reinstall, please run the script again"
    exit 1
}
trap handle_interrupt SIGINT

# ---------------------------------------------------------------------------
# Banner
# ---------------------------------------------------------------------------
printf '\e[1;32m'
cat << "EOF"
Script author: @Bilibili Vanilla-flavored Nahida Meow
 __      __            _  _  _            _   _         _      _      _
 \ \    / /           (_)| || |          | \ | |       | |    (_)    | |
  \ \  / /__ _  _ __   _ | || |  __ _    |  \| |  __ _ | |__   _   __| |  __ _
   \ \/ // _` || '_ \ | || || | / _` |   | . ` | / _` || '_ \ | | / _` | / _` |
    \  /| (_| || | | || || || || (_| |   | |\  || (_| || | | || || (_| || (_| |
     \/  \__,_||_| |_||_||_||_| \__,_|   |_| \_| \__,_||_| |_||_| \__,_| \__,_|
EOF
printf '\e[0m\n'
printf '\e[1;36m  LittleWise Server Full Deployment One-Click Install Script Ver 0.3 \e[0m\n\n'
sleep 1

# ---------------------------------------------------------------------------
# Root check (runs before anything that touches apt)
# ---------------------------------------------------------------------------
if [ "${EUID:-$(id -u)}" -ne 0 ]; then
    echo "ERROR: this script must be run as root (try: sudo bash $0)" >&2
    exit 1
fi

# ---------------------------------------------------------------------------
# Install whiptail if missing (needed for the rest of the script)
# ---------------------------------------------------------------------------
check_whiptail() {
    if ! command -v whiptail >/dev/null 2>&1; then
        echo "Installing whiptail..."
        apt update
        apt install -y whiptail
    fi
}
check_whiptail

# Install confirmation dialog
whiptail --title "Install Confirmation" --yesno "About to install the LittleWise server. Continue?" \
  --yes-button "Continue" --no-button "Exit" 10 50
case $? in
  0) ;;
  *) exit 1 ;;
esac

# ---------------------------------------------------------------------------
# OS detection (Debian/Ubuntu only)
# ---------------------------------------------------------------------------
if [ -f /etc/os-release ]; then
    . /etc/os-release
    if [ "$ID" != "debian" ] && [ "$ID" != "ubuntu" ]; then
        whiptail --title "System error" --msgbox "This script only supports Debian/Ubuntu systems" 10 60
        exit 1
    fi
else
    whiptail --title "System error" --msgbox "Unable to determine system version. This script only supports Debian/Ubuntu systems" 10 60
    exit 1
fi

# ---------------------------------------------------------------------------
# Directory bootstrap (must happen before any download writes into them)
# ---------------------------------------------------------------------------
mkdir -p "$DATA_DIR" "$MODEL_DIR"

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

# Download a file from the upstream repo on raw.githubusercontent.com.
# Usage: check_and_download <abs-local-path> <repo-relative-path>
check_and_download() {
    local filepath="$1"
    local relpath="$2"

    if [ -f "$filepath" ]; then
        echo "${filepath} already exists, skipping download"
        return 0
    fi

    mkdir -p "$(dirname "$filepath")"

    local url="${RAW_BASE}/${relpath}"
    echo "Downloading ${filepath} from ${url}"
    if curl -fL --connect-timeout 10 --retry 3 --retry-delay 2 --progress-bar "$url" -o "$filepath"; then
        return 0
    fi
    rm -f "$filepath"

    whiptail --title "Error" --msgbox "${filepath} file download failed from ${url}" 10 60
    exit 1
}

# Return the correct compose command in $COMPOSE ("docker compose" or "docker-compose").
# Installs docker-compose-plugin if neither is available.
resolve_compose_cmd() {
    if docker compose version >/dev/null 2>&1; then
        COMPOSE="docker compose"
        return
    fi
    if command -v docker-compose >/dev/null 2>&1; then
        COMPOSE="docker-compose"
        return
    fi
    echo "Neither 'docker compose' nor 'docker-compose' found; installing docker-compose-plugin..."
    apt update
    apt install -y docker-compose-plugin || apt install -y docker-compose
    if docker compose version >/dev/null 2>&1; then
        COMPOSE="docker compose"
    elif command -v docker-compose >/dev/null 2>&1; then
        COMPOSE="docker-compose"
    else
        whiptail --title "Error" --msgbox "Failed to install a Docker Compose command; aborting." 10 60
        exit 1
    fi
}

# Ask the user which host port to publish the device WebSocket on. Loops
# on invalid input (non-numeric, out of range) and warn-confirms privileged
# ports (<1024). Blank input keeps the default. Result stored in WS_HOST_PORT.
prompt_ws_port() {
    local prompt_msg="The device WebSocket service is published on a host port.\n\nPort 8000 is commonly used by other web services and often collides. The new default is ${DEFAULT_WS_HOST_PORT}.\n\nPress Enter to keep the default, or type a different port (1-65535):"
    local input
    while true; do
        input=$(whiptail --title "WebSocket host port" \
            --inputbox "$prompt_msg" 15 70 "$DEFAULT_WS_HOST_PORT" \
            3>&1 1>&2 2>&3) || exit 1
        if [ -z "$input" ]; then
            input="$DEFAULT_WS_HOST_PORT"
        fi
        if ! [[ "$input" =~ ^[0-9]+$ ]]; then
            whiptail --title "Invalid port" \
                --msgbox "Port must be numeric. Please try again." 10 50
            continue
        fi
        if [ "$input" -lt 1 ] || [ "$input" -gt 65535 ]; then
            whiptail --title "Invalid port" \
                --msgbox "Port must be between 1 and 65535. Please try again." 10 60
            continue
        fi
        if [ "$input" -lt 1024 ]; then
            if ! whiptail --title "Privileged port" \
                --yesno "Port ${input} is in the privileged range (<1024). Use it anyway?" 10 60; then
                continue
            fi
        fi
        WS_HOST_PORT="$input"
        break
    done
    echo "Using WebSocket host port: ${WS_HOST_PORT}"
}

# Ensure python3 + PyYAML (used to edit .config.yaml for the secret key).
ensure_python_yaml() {
    if ! command -v python3 >/dev/null 2>&1; then
        apt update
        apt install -y python3
    fi
    if ! python3 -c "import yaml" >/dev/null 2>&1; then
        apt install -y python3-yaml
    fi
}

# Fetch the repo source tarball into $SRC_DIR so the server and web images
# can be built locally via docker compose. Refreshes on every call.
download_source_tree() {
    mkdir -p "$SRC_DIR"
    local tmp
    tmp=$(mktemp -d)
    echo "Downloading source tarball from ${SRC_TARBALL_URL}"
    if ! curl -fL --connect-timeout 15 --retry 3 --retry-delay 2 \
            "$SRC_TARBALL_URL" -o "${tmp}/src.tar.gz"; then
        rm -rf "$tmp"
        whiptail --title "Error" --msgbox "Failed to download source tarball from ${SRC_TARBALL_URL}" 10 60
        exit 1
    fi
    # Purge stale files so upgrades don't keep deleted paths around.
    find "$SRC_DIR" -mindepth 1 -maxdepth 1 -exec rm -rf {} +
    if ! tar -xzf "${tmp}/src.tar.gz" -C "$SRC_DIR" --strip-components=1; then
        rm -rf "$tmp"
        whiptail --title "Error" --msgbox "Failed to extract source tarball" 10 60
        exit 1
    fi
    rm -rf "$tmp"
    for path in Dockerfile-server Dockerfile-web main/xiaozhi-server main/manager-api main/manager-web; do
        if [ ! -e "${SRC_DIR}/${path}" ]; then
            whiptail --title "Error" --msgbox "Extracted source is missing ${path}" 10 60
            exit 1
        fi
    done
}

# Rewrite the downloaded compose file so the xiaozhi-esp32-server and
# xiaozhi-esp32-server-web services build from ${SRC_DIR} instead of
# pulling prebuilt images. Idempotent.
rewrite_compose_for_build() {
    ensure_python_yaml
    COMPOSE_FILE="$COMPOSE_FILE" \
    LOCAL_SERVER_IMAGE="$LOCAL_SERVER_IMAGE" \
    LOCAL_WEB_IMAGE="$LOCAL_WEB_IMAGE" \
    WS_HOST_PORT="$WS_HOST_PORT" \
    python3 - <<'PY'
import os
import yaml

path = os.environ["COMPOSE_FILE"]
with open(path) as f:
    compose = yaml.safe_load(f) or {}

services = compose.get("services", {})
targets = {
    "xiaozhi-esp32-server": {
        "dockerfile": "Dockerfile-server",
        "image": os.environ["LOCAL_SERVER_IMAGE"],
    },
    "xiaozhi-esp32-server-web": {
        "dockerfile": "Dockerfile-web",
        "image": os.environ["LOCAL_WEB_IMAGE"],
    },
}

for name, cfg in targets.items():
    svc = services.get(name)
    if svc is None:
        raise SystemExit(f"{name} service missing from compose file")
    svc.pop("image", None)
    svc["build"] = {"context": "./src", "dockerfile": cfg["dockerfile"]}
    svc["image"] = cfg["image"]

ws_host_port = os.environ["WS_HOST_PORT"]
server_svc = services["xiaozhi-esp32-server"]
ports = server_svc.get("ports", [])
for idx, entry in enumerate(ports):
    if isinstance(entry, str):
        host, sep, container = entry.partition(":")
        if sep and container == "8000":
            ports[idx] = f"{ws_host_port}:8000"
    elif isinstance(entry, dict):
        if str(entry.get("target")) == "8000":
            entry["published"] = int(ws_host_port)
server_svc["ports"] = ports

with open(path, "w") as f:
    yaml.dump(compose, f, sort_keys=False)
PY
}

# ---------------------------------------------------------------------------
# Ask the user which host port to publish the WebSocket service on. Runs
# once, before either the upgrade or fresh-install path, so both paths can
# feed the value into rewrite_compose_for_build.
# ---------------------------------------------------------------------------
prompt_ws_port

# ---------------------------------------------------------------------------
# Detect whether an existing install is present
# ---------------------------------------------------------------------------
check_installed() {
    local dir_check=0 container_check=0
    if [ -d "$INSTALL_DIR" ] && [ -n "$(ls -A "$INSTALL_DIR" 2>/dev/null)" ]; then
        dir_check=1
    fi
    if command -v docker >/dev/null 2>&1 && docker inspect xiaozhi-esp32-server >/dev/null 2>&1; then
        container_check=1
    fi
    if [ "$dir_check" -eq 1 ] && [ "$container_check" -eq 1 ]; then
        return 0
    fi
    return 1
}

# ---------------------------------------------------------------------------
# Upgrade flow (runs only if an existing install is detected)
# ---------------------------------------------------------------------------
if check_installed; then
    if whiptail --title "Install detected" --yesno "LittleWise server is already installed. Would you like to upgrade?" 10 60; then
        echo "Starting upgrade..."

        resolve_compose_cmd

        # Stop docker-compose services (ignore failures if file is missing/stale)
        if [ -f "$COMPOSE_FILE" ]; then
            $COMPOSE -f "$COMPOSE_FILE" down || true
        fi

        # Stop and delete known containers if they still exist
        containers=(
            "xiaozhi-esp32-server"
            "xiaozhi-esp32-server-web"
            "xiaozhi-esp32-server-db"
            "xiaozhi-esp32-server-redis"
        )
        for container in "${containers[@]}"; do
            if docker ps -a --format '{{.Names}}' | grep -q "^${container}$"; then
                docker stop "$container" >/dev/null 2>&1 && \
                docker rm "$container" >/dev/null 2>&1 && \
                echo "Successfully removed container: $container"
            else
                echo "Container does not exist, skipping: $container"
            fi
        done

        # Delete known images if they exist. The ghcr.io entries cover users
        # upgrading from the pull-path era; the *-local entries force a fresh
        # rebuild of the images produced by this script.
        images=(
            "ghcr.io/taylorelley/xiaozhi-esp32-server:server_latest"
            "ghcr.io/taylorelley/xiaozhi-esp32-server:web_latest"
            "$LOCAL_SERVER_IMAGE"
            "$LOCAL_WEB_IMAGE"
        )
        for image in "${images[@]}"; do
            if docker images --format '{{.Repository}}:{{.Tag}}' | grep -q "^${image}$"; then
                docker rmi "$image" >/dev/null 2>&1 && \
                echo "Successfully deleted image: $image"
            else
                echo "Image does not exist, skipping: $image"
            fi
        done

        echo "All cleanup operations complete"

        # Back up the original config file
        mkdir -p "${INSTALL_DIR}/backup/"
        if [ -f "$CONFIG_FILE" ]; then
            cp "$CONFIG_FILE" "${INSTALL_DIR}/backup/.config.yaml"
            echo "Existing config file backed up to ${INSTALL_DIR}/backup/.config.yaml"
        fi

        # Pull the latest compose + config templates
        check_and_download "$COMPOSE_FILE" "$COMPOSE_PATH_REL"
        check_and_download "$CONFIG_FILE"  "$CONFIG_PATH_REL"

        # Refresh source tree and switch the compose file to build locally.
        download_source_tree
        rewrite_compose_for_build

        echo "Starting the latest version of the services..."
        UPGRADE_COMPLETED=1
        $COMPOSE -f "$COMPOSE_FILE" up -d --build
    else
        whiptail --title "Skip upgrade" --msgbox "Upgrade cancelled. The current version will continue to be used." 10 50
        # Fall through to the installation flow
    fi
fi

# ---------------------------------------------------------------------------
# Install curl (needed for the Docker repo setup)
# ---------------------------------------------------------------------------
if ! command -v curl >/dev/null 2>&1; then
    echo "------------------------------------------------------------"
    echo "curl not detected, installing..."
    apt update
    apt install -y curl
else
    echo "------------------------------------------------------------"
    echo "curl is already installed, skipping installation"
fi

# ---------------------------------------------------------------------------
# Install Docker (if missing)
# ---------------------------------------------------------------------------
if ! command -v docker >/dev/null 2>&1; then
    echo "------------------------------------------------------------"
    echo "Docker not detected, installing..."

    # Determine distro codename without relying on lsb_release
    DISTRO="${VERSION_CODENAME:-}"
    if [ -z "$DISTRO" ] && command -v lsb_release >/dev/null 2>&1; then
        DISTRO="$(lsb_release -cs)"
    fi
    if [ -z "$DISTRO" ]; then
        apt update
        apt install -y lsb-release
        DISTRO="$(lsb_release -cs)"
    fi

    # Use the official Docker apt repository
    case "$ID" in
        debian) MIRROR_URL="https://download.docker.com/linux/debian"
                GPG_URL="https://download.docker.com/linux/debian/gpg" ;;
        *)      MIRROR_URL="https://download.docker.com/linux/ubuntu"
                GPG_URL="https://download.docker.com/linux/ubuntu/gpg" ;;
    esac

    # Install base dependencies
    apt update
    apt install -y apt-transport-https ca-certificates curl software-properties-common gnupg

    # Add the Docker key via the modern keyring method
    mkdir -p /etc/apt/keyrings
    curl -fsSL "$GPG_URL" | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    chmod a+r /etc/apt/keyrings/docker.gpg

    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] $MIRROR_URL $DISTRO stable" \
        > /etc/apt/sources.list.d/docker.list

    # Install Docker + Compose plugin
    apt update
    apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

    # Start the service
    systemctl start docker
    systemctl enable docker

    if docker --version; then
        echo "------------------------------------------------------------"
        echo "Docker installation complete!"
    else
        whiptail --title "Error" --msgbox "Docker installation failed, please check the logs." 10 50
        exit 1
    fi
else
    echo "Docker is already installed, skipping installation"
fi

# Resolve which compose command to use from this point on
resolve_compose_cmd

# ---------------------------------------------------------------------------
# Fresh-install download path (skipped when the upgrade path already ran)
# ---------------------------------------------------------------------------
if [ -z "$UPGRADE_COMPLETED" ]; then
    echo "------------------------------------------------------------"
    echo "Starting to download the speech recognition model"
    if [ ! -f "$MODEL_FILE" ]; then
        # Resumable download with retries; ~900 MB file, so flaky links must not restart.
        curl -fL --retry 5 --retry-delay 3 --connect-timeout 15 -C - \
            "$MODEL_URL" -o "$MODEL_FILE" || {
            whiptail --title "Error" --msgbox "Failed to download model.pt" 10 50
            exit 1
        }
    else
        echo "model.pt already exists, skipping download"
    fi

    check_and_download "$COMPOSE_FILE" "$COMPOSE_PATH_REL"
    check_and_download "$CONFIG_FILE"  "$CONFIG_PATH_REL"

    # Fetch source tree and switch compose to build the server and web images locally.
    download_source_tree
    rewrite_compose_for_build
fi

# ---------------------------------------------------------------------------
# Start the services
# ---------------------------------------------------------------------------
echo "------------------------------------------------------------"
echo "Building xiaozhi-esp32-server and xiaozhi-esp32-server-web from source and starting services..."
echo "This may take a few minutes, please be patient"
if ! $COMPOSE -f "$COMPOSE_FILE" up -d --build; then
    whiptail --title "Error" --msgbox "Docker service failed to start. Please try switching the mirror source and rerun this script" 10 60
    exit 1
fi

echo "------------------------------------------------------------"
echo "Checking service startup status..."
TIMEOUT=300
START_TIME=$(date +%s)
while true; do
    CURRENT_TIME=$(date +%s)
    if [ $((CURRENT_TIME - START_TIME)) -gt $TIMEOUT ]; then
        whiptail --title "Error" --msgbox "Service startup timed out; the expected log content was not found within the allotted time" 10 60
        exit 1
    fi
    if docker logs xiaozhi-esp32-server-web 2>&1 | \
        grep -E -q "Started AdminApplication|admin-api.*started|Tomcat started on port"; then
        break
    fi
    sleep 2
done

# Sanity check: all four containers should be running
RUNNING_CONTAINERS=$(docker ps --format '{{.Names}}')
MISSING=""
for c in xiaozhi-esp32-server xiaozhi-esp32-server-web xiaozhi-esp32-server-db xiaozhi-esp32-server-redis; do
    if ! echo "$RUNNING_CONTAINERS" | grep -q "^${c}$"; then
        MISSING="${MISSING}${c} "
    fi
done
if [ -n "$MISSING" ]; then
    whiptail --title "Error" --msgbox "The following containers are not running: ${MISSING}\nCheck 'docker ps -a' and the container logs." 12 70
    exit 1
fi

echo "Server started successfully! Finishing configuration..."

# ---------------------------------------------------------------------------
# Secret configuration
# ---------------------------------------------------------------------------
get_primary_ip() {
    local ip
    ip=$(hostname -I 2>/dev/null | awk '{print $1}')
    if [ -z "$ip" ] && command -v ip >/dev/null 2>&1; then
        ip=$(ip -4 route get 1.1.1.1 2>/dev/null | awk '{print $7; exit}')
    fi
    echo "${ip:-127.0.0.1}"
}

PUBLIC_IP=$(get_primary_ip)
whiptail --title "Configure server secret" --msgbox "Please use a browser to access the link below, open the control console and register an account: \n\nInternal address: http://127.0.0.1:8002/\nPublic address: http://${PUBLIC_IP}:8002/ (if this is a cloud server, please open ports ${WS_HOST_PORT} 8002 8003 in the server's security group).\n\nThe first registered user is the super administrator; subsequently registered users are ordinary users. Ordinary users can only bind devices and configure agents; the super administrator can manage models, users, parameter configuration, and more.\n\nAfter registration, press Enter to continue" 18 70

SECRET_KEY=$(whiptail --title "Configure server secret" --inputbox "Please log in to the control console with a super-admin account\nInternal address: http://127.0.0.1:8002/\nPublic address: http://${PUBLIC_IP}:8002/\nFrom the top menu Parameter Dictionary -> Parameter Management, find the parameter code: server.secret (server secret) \nCopy that parameter value and enter it in the box below\n\nPlease enter the secret (leave blank to skip configuration):" 15 60 3>&1 1>&2 2>&3)

if [ -n "$SECRET_KEY" ]; then
    ensure_python_yaml
    SECRET_KEY="$SECRET_KEY" CONFIG_FILE="$CONFIG_FILE" python3 - <<'PY'
import os
import yaml

config_path = os.environ["CONFIG_FILE"]
secret = os.environ["SECRET_KEY"]

with open(config_path, "r") as f:
    config = yaml.safe_load(f) or {}

config["manager-api"] = {
    "url": "http://xiaozhi-esp32-server-web:8002/xiaozhi",
    "secret": secret,
}

with open(config_path, "w") as f:
    yaml.dump(config, f)
PY
    docker restart xiaozhi-esp32-server
fi

# ---------------------------------------------------------------------------
# Final summary
# ---------------------------------------------------------------------------
LOCAL_IP=$(get_primary_ip)
whiptail --title "Installation complete!" --msgbox "\
Server addresses are as follows:\n\
Admin console address: http://${LOCAL_IP}:8002\n\
OTA address: http://${LOCAL_IP}:8002/xiaozhi/ota/\n\
Vision analysis API address: http://${LOCAL_IP}:8003/mcp/vision/explain\n\
WebSocket address: ws://${LOCAL_IP}:${WS_HOST_PORT}/xiaozhi/v1/\n\
\nInstallation complete! Thank you for using!\nPress Enter to exit..." 16 70
