package com.hy.biz.dataParsing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hy.biz.dataPush.DataPushService;
import com.hy.domain.DeviceOnlineStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * @author shiwentao
 * @package com.hy.biz.dataParsing.dto.dto
 * @description
 * @create 2023-05-04 16:37
 **/
@Getter
@Setter
public class DeviceOnlineStatusDTO {

    @JsonProperty("header")
    private Header header;

    @JsonProperty("senderInfo")
    private SenderInfo senderInfo;

    @JsonProperty("result")
    private Result result;

    @Getter
    @Setter
    public static class Result {
        @JsonProperty("Stat")
        private String status;

        @JsonProperty("Msg")
        private String msg;
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

        String[] msg = this.result.msg.split(" ");
        deviceOnlineStatus.setDeviceId(dataPushService.findDeviceByCode(msg[0]).getDeviceId());
        deviceOnlineStatus.setCollectionTime(Instant.ofEpochMilli(this.header.timeStamp));
        deviceOnlineStatus.setMessage("deviceCode: " + this.header.deviceCode + "  signal: " + this.senderInfo.signal);
        deviceOnlineStatus.setStatus(this.result.status);

        return deviceOnlineStatus;
    }


}
