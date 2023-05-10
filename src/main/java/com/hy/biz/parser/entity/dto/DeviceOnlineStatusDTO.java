package com.hy.biz.parser.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hy.domain.DeviceOnlineStatus;
import com.hy.repository.DeviceRepository;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * @author shiwentao
 * @package com.hy.biz.parser.entity.dto
 * @description
 * @create 2023-05-04 16:37
 **/
@Getter
@Setter
public class DeviceOnlineStatusDTO {

    @JsonProperty("result")
    private Result result;

    @JsonProperty("senderInfo")
    private SenderInfo senderInfo;

    @JsonProperty("header")
    private Header header;

    @Getter
    @Setter
    public static class Result {
        @JsonProperty("Msg")
        private String msg;

        @JsonProperty("Stat")
        private String status;
    }

    @Getter
    @Setter
    public static class SenderInfo {
        @JsonProperty("Signal")
        private String signal;
    }

    @Getter
    @Setter
    public static class Header {
        @JsonProperty("DeviceCode")
        private String deviceCode;

        @JsonProperty("TimeStamp")
        private String timeStamp;
    }

    public DeviceOnlineStatus transform(DeviceRepository deviceRepository) {
        DeviceOnlineStatus deviceOnlineStatus = new DeviceOnlineStatus();

        String[] msg = this.getResult().getMsg().split(" ");
        deviceOnlineStatus.setDeviceId(deviceRepository.findDeviceIdByCode(msg[0]));
        deviceOnlineStatus.setCollectionTime(Instant.now());
        deviceOnlineStatus.setMessage(this.getResult().getMsg());
        deviceOnlineStatus.setStatus(this.getResult().getStatus());

        return deviceOnlineStatus;
    }

}
