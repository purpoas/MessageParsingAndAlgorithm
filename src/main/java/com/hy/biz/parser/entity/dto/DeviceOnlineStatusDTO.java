package com.hy.biz.parser.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

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

}
