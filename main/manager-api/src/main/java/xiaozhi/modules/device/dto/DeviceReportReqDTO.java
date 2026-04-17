package xiaozhi.modules.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@Schema(description = "devicefirmwareinformation上报求request")
public class DeviceReportReqDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    // region entity
    @Schema(description = "boardfirmwareversionnumber")
    private Integer version;

    @Schema(description = "闪storelargesmall（unit：byte）")
    @JsonProperty("flash_size")
    private Integer flashSize;

    @Schema(description = "minimumempty闲堆内store（byte）")
    @JsonProperty("minimum_free_heap_size")
    private Integer minimumFreeHeapSize;

    @Schema(description = "device MAC Address")
    @JsonProperty("mac_address")
    private String macAddress;

    @Schema(description = "deviceunique identifier UUID")
    private String uuid;

    @Schema(description = "chip型numbername")
    @JsonProperty("chip_model_name")
    private String chipModelName;

    @Schema(description = "chip详细information")
    @JsonProperty("chip_info")
    private ChipInfo chipInfo;

    @Schema(description = "应用程orderinformation")
    private Application application;

    @Schema(description = "partitiontablelist")
    @JsonProperty("partition_table")
    private List<Partition> partitionTable;

    @Schema(description = "currentrun  OTA partitioninformation")
    private OtaInfo ota;

    @Schema(description = "boardconfigurationinformation")
    private BoardInfo board;

    // endregion

    @Getter
    @Setter
    @Schema(description = "chipinformation")
    public static class ChipInfo {
        @Schema(description = "chipmodel代code")
        private Integer model;

        @Schema(description = "核心number")
        private Integer cores;

        @Schema(description = "硬item修订version")
        private Integer revision;

        @Schema(description = "chipfunction标志bit")
        private Integer features;
    }

    @Getter
    @Setter
    @Schema(description = "board编译information")
    public static class Application {
        @Schema(description = "name")
        private String name;

        @Schema(description = "应用versionnumber")
        private String version;

        @Schema(description = "编译time（UTC ISOformat）")
        @JsonProperty("compile_time")
        private String compileTime;

        @Schema(description = "ESP-IDF versionnumber")
        @JsonProperty("idf_version")
        private String idfVersion;

        @Schema(description = "ELF file SHA256 validate")
        @JsonProperty("elf_sha256")
        private String elfSha256;
    }

    @Getter
    @Setter
    @Schema(description = "partitioninformation")
    public static class Partition {
        @Schema(description = "partitionTag名")
        private String label;

        @Schema(description = "partitiontype")
        private Integer type;

        @Schema(description = "childtype")
        private Integer subtype;

        @Schema(description = "起始Address")
        private Integer address;

        @Schema(description = "partitionlargesmall")
        private Integer size;
    }

    @Getter
    @Setter
    @Schema(description = "OTAinformation")
    public static class OtaInfo {
        @Schema(description = "currentOTATag")
        private String label;
    }

    @Getter
    @Setter
    @Schema(description = "boardconnectionandnetworkinformation")
    public static class BoardInfo {
        @Schema(description = "boardtype")
        private String type;

        @Schema(description = "connection  Wi-Fi SSID")
        private String ssid;

        @Schema(description = "Wi-Fi 信numberstrong度（RSSI）")
        private Integer rssi;

        @Schema(description = "Wi-Fi 信道")
        private Integer channel;

        @Schema(description = "IP Address")
        private String ip;

        @Schema(description = "MAC Address")
        private String mac;
    }
}
