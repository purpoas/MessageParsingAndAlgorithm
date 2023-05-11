package com.hy.biz.dataResolver.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class MessageDTO {

    @JsonProperty("dateTime")
    private long timeStamp;

    @JsonProperty("data")
    private Data data;

    @JsonProperty("deviceCode")
    private String deviceCode;

    @Getter
    @Setter
    public static class Data {
        @JsonProperty("command")
        private String command;
    }

}

