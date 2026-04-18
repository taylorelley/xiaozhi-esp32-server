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

RAW_BASE_PRIMARY="https://ghfast.top/https://raw.githubusercontent.com/xinnan-tech/xiaozhi-esp32-server/refs/heads/main"
RAW_BASE_FALLBACK="https://raw.githubusercontent.com/xinnan-tech/xiaozhi-esp32-server/refs/heads/main"

COMPOSE_PATH_REL="main/xiaozhi-server/docker-compose_all.yml"
CONFIG_PATH_REL="main/xiaozhi-server/config_from_api.yaml"

MODEL_URL="https://modelscope.cn/models/iic/SenseVoiceSmall/resolve/master/model.pt"

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

# Download a file from the upstream repo, trying the ghfast.top proxy first
# and falling back to raw.githubusercontent.com on failure.
# Usage: check_and_download <abs-local-path> <repo-relative-path>
check_and_download() {
    local filepath="$1"
    local relpath="$2"

    if [ -f "$filepath" ]; then
        echo "${filepath} already exists, skipping download"
        return 0
    fi

    mkdir -p "$(dirname "$filepath")"

    local url
    for url in "${RAW_BASE_PRIMARY}/${relpath}" "${RAW_BASE_FALLBACK}/${relpath}"; do
        echo "Downloading ${filepath} from ${url}"
        if curl -fL --connect-timeout 10 --retry 3 --retry-delay 2 --progress-bar "$url" -o "$filepath"; then
            return 0
        fi
        echo "Download via ${url} failed, trying next source..."
        rm -f "$filepath"
    done

    whiptail --title "Error" --msgbox "${filepath} file download failed (both primary and fallback URLs)" 10 60
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

        # Delete known images if they exist
        images=(
            "ghcr.nju.edu.cn/xinnan-tech/xiaozhi-esp32-server:server_latest"
            "ghcr.nju.edu.cn/xinnan-tech/xiaozhi-esp32-server:web_latest"
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

        echo "Starting the latest version of the services..."
        UPGRADE_COMPLETED=1
        $COMPOSE -f "$COMPOSE_FILE" up -d
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

    # Use a domestic mirror source instead of the official source
    case "$ID" in
        debian) MIRROR_URL="https://mirrors.aliyun.com/docker-ce/linux/debian"
                GPG_URL="https://mirrors.aliyun.com/docker-ce/linux/debian/gpg" ;;
        *)      MIRROR_URL="https://mirrors.aliyun.com/docker-ce/linux/ubuntu"
                GPG_URL="https://mirrors.aliyun.com/docker-ce/linux/ubuntu/gpg" ;;
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
# Docker mirror configuration
# ---------------------------------------------------------------------------
MIRROR_OPTIONS=(
    "1" "Xuanyuan mirror (recommended)"
    "2" "Tencent Cloud mirror"
    "3" "USTC mirror"
    "4" "NetEase 163 mirror"
    "5" "Huawei Cloud mirror"
    "6" "Alibaba Cloud personal mirror"
    "7" "Custom mirror"
    "8" "Skip configuration"
)

MIRROR_CHOICE=$(whiptail --title "Select Docker mirror source" --menu "Please select the Docker mirror source to use" 20 60 10 \
"${MIRROR_OPTIONS[@]}" 3>&1 1>&2 2>&3) || {
    echo "User cancelled selection, exiting script"
    exit 1
}

MIRROR_URL=""
case $MIRROR_CHOICE in
    1) MIRROR_URL="https://docker.xuanyuan.me" ;;
    2) MIRROR_URL="https://mirror.ccs.tencentyun.com" ;;
    3) MIRROR_URL="https://docker.mirrors.ustc.edu.cn" ;;
    4) MIRROR_URL="https://hub-mirror.c.163.com" ;;
    5) MIRROR_URL="https://05f073ad3c0010ea0f4bc00b7105ec20.mirror.swr.myhuaweicloud.com" ;;
    6)
        MIRROR_URL=$(whiptail --title "Alibaba Cloud personal mirror" \
            --inputbox "Alibaba Cloud requires a personal mirror URL.\nLog in to https://cr.console.aliyun.com/, open 'Image Accelerator', and copy the accelerator URL (looks like https://<id>.mirror.aliyuncs.com).\n\nEnter your personal accelerator URL:" \
            15 70 3>&1 1>&2 2>&3) || MIRROR_URL=""
        ;;
    7)
        MIRROR_URL=$(whiptail --title "Custom mirror source" --inputbox "Please enter the full mirror source URL (must start with https://):" 10 60 3>&1 1>&2 2>&3) || MIRROR_URL=""
        ;;
    8) MIRROR_URL="" ;;
esac

if [ -n "$MIRROR_URL" ]; then
    case "$MIRROR_URL" in
        https://*) ;;
        *)
            whiptail --title "Invalid mirror URL" --msgbox "The mirror URL must start with https://. Skipping mirror configuration." 10 60
            MIRROR_URL=""
            ;;
    esac
fi

if [ -n "$MIRROR_URL" ]; then
    mkdir -p /etc/docker
    if [ -f /etc/docker/daemon.json ]; then
        cp /etc/docker/daemon.json /etc/docker/daemon.json.bak
    fi
    cat > /etc/docker/daemon.json <<EOF
{
    "dns": ["8.8.8.8", "114.114.114.114"],
    "registry-mirrors": ["$MIRROR_URL"]
}
EOF
    whiptail --title "Configuration successful" --msgbox "Mirror source added successfully: $MIRROR_URL\nPress Enter to restart the Docker service and continue..." 12 60
    echo "------------------------------------------------------------"
    echo "Restarting the Docker service..."
    systemctl restart docker.service
fi

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
fi

# ---------------------------------------------------------------------------
# Start the services
# ---------------------------------------------------------------------------
echo "------------------------------------------------------------"
echo "Pulling Docker images..."
echo "This may take a few minutes, please be patient"
if ! $COMPOSE -f "$COMPOSE_FILE" up -d; then
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
whiptail --title "Configure server secret" --msgbox "Please use a browser to access the link below, open the control console and register an account: \n\nInternal address: http://127.0.0.1:8002/\nPublic address: http://${PUBLIC_IP}:8002/ (if this is a cloud server, please open ports 8000 8001 8002 in the server's security group).\n\nThe first registered user is the super administrator; subsequently registered users are ordinary users. Ordinary users can only bind devices and configure agents; the super administrator can manage models, users, parameter configuration, and more.\n\nAfter registration, press Enter to continue" 18 70

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
WebSocket address: ws://${LOCAL_IP}:8000/xiaozhi/v1/\n\
\nInstallation complete! Thank you for using!\nPress Enter to exit..." 16 70
