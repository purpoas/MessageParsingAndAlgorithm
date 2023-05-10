package com.hy.biz.dataPush.dto;

/**
 * 设备基础信息
 */
public class DeviceDTO {

    private String deviceCode;
    private Integer phase;
    private String lineId;
    private String towerId;
    private String supplierCode;
    private Double distanceToHeadStation;
    private Double lineLength;

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public Integer getPhase() {
        return phase;
    }

    public void setPhase(Integer phase) {
        this.phase = phase;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getTowerId() {
        return towerId;
    }

    public void setTowerId(String towerId) {
        this.towerId = towerId;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public Double getDistanceToHeadStation() {
        return distanceToHeadStation;
    }

    public void setDistanceToHeadStation(Double distanceToHeadStation) {
        this.distanceToHeadStation = distanceToHeadStation;
    }

    public Double getLineLength() {
        return lineLength;
    }

    public void setLineLength(Double lineLength) {
        this.lineLength = lineLength;
    }


    @Override
    public String toString() {
        return "DeviceDetailsDTO{" +
                "deviceCode='" + deviceCode + '\'' +
                ", phase=" + phase +
                ", lineId='" + lineId + '\'' +
                ", towerId='" + towerId + '\'' +
                ", supplierCode='" + supplierCode + '\'' +
                ", distanceToHeadStation=" + distanceToHeadStation +
                ", lineLength=" + lineLength +
                '}';
    }
}
