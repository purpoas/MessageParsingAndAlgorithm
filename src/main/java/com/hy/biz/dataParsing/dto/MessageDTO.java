package com.hy.biz.dataParsing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class MessageDTO {

    @JsonProperty("deviceCode")
    private String deviceCode;

    @JsonProperty("dateTime")
    private long timeStamp;

    @JsonProperty("data")
    private Data data;

    @Getter
    @Setter
    public static class Data {
        @JsonProperty("command")
        private String command;
    }

}

