package com.hy.biz.dataResolver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hy.biz.dataPush.DataPushService;
import com.hy.domain.DeviceOnlineStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * @author shiwentao
 * @package com.hy.biz.dataResolver.dto.dto
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
        private long timeStamp;
    }

    public DeviceOnlineStatus transform(DataPushService dataPushService) {

        DeviceOnlineStatus deviceOnlineStatus = new DeviceOnlineStatus();

        String[] msg = this.getResult().getMsg().split(" ");
        deviceOnlineStatus.setDeviceId(dataPushService.findDeviceByCode(msg[0]).getDeviceId());
        deviceOnlineStatus.setCollectionTime(Instant.now());
        deviceOnlineStatus.setMessage("deviceCode: " + this.getHeader().getDeviceCode() + "  signal: " + this.getSenderInfo().getSignal());
        deviceOnlineStatus.setStatus(this.getResult().getStatus());

        return deviceOnlineStatus;
    }

}
