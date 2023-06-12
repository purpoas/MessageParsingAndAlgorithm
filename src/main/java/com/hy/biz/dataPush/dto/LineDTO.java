package com.hy.biz.dataPush.dto;

import lombok.Data;

@Data
public class LineDTO {

    private String lineId;

    private Double lineLength;

    public LineDTO() {
    }

    public LineDTO(String lineId, Double lineLength) {
        this.lineId = lineId;
        this.lineLength = lineLength;
    }
}
