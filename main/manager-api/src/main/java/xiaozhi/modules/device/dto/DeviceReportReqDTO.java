package xiaozhi.modules.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@Schema(description = "devicefirmwareinformationreportrequestrequest")
public class DeviceReportReqDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    // region entity
    @Schema(description = "boardfirmwareversionnumber")
    private Integer version;

    @Schema(description = "flash storagelargesmall（unit：byte）")
    @JsonProperty("flash_size")
    private Integer flashSize;

    @Schema(description = "minimumemptyfree heapstore（byte）")
    @JsonProperty("minimum_free_heap_size")
    private Integer minimumFreeHeapSize;

    @Schema(description = "device MAC Address")
    @JsonProperty("mac_address")
    private String macAddress;

    @Schema(description = "deviceunique identifier UUID")
    private String uuid;

    @Schema(description = "chiptypenumbername")
    @JsonProperty("chip_model_name")
    private String chipModelName;

    @Schema(description = "chipdetailedinformation")
    @JsonProperty("chip_info")
    private ChipInfo chipInfo;

    @Schema(description = "shoulduseprocessorderinformation")
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
        @Schema(description = "chipmodelcode")
        private Integer model;

        @Schema(description = "corenumber")
        private Integer cores;

        @Schema(description = "harditemmodifysubscribeversion")
        private Integer revision;

        @Schema(description = "chipfunctionflagbit")
        private Integer features;
    }

    @Getter
    @Setter
    @Schema(description = "boardcompileinformation")
    public static class Application {
        @Schema(description = "name")
        private String name;

        @Schema(description = "shoulduseversionnumber")
        private String version;

        @Schema(description = "compiletime（UTC ISOformat）")
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
        @Schema(description = "partitionTagname")
        private String label;

        @Schema(description = "partitiontype")
        private Integer type;

        @Schema(description = "childtype")
        private Integer subtype;

        @Schema(description = "startAddress")
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

        @Schema(description = "Wi-Fi infonumberstrong（RSSI）")
        private Integer rssi;

        @Schema(description = "Wi-Fi infochannel")
        private Integer channel;

        @Schema(description = "IP Address")
        private String ip;

        @Schema(description = "MAC Address")
        private String mac;
    }
}
