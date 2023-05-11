package com.hy.biz.dataPush.dto;

import com.hy.domain.Device;
import lombok.Data;

/**
 * 设备基础信息
 */
@Data
public class DeviceDTO {

    private Long deviceId;
    private String deviceCode;
    private Integer phase;
    private Long lineId;
    private Long poleId;
    private String supplierCode;
    private Double distanceToHeadStation;

    public DeviceDTO from(Device device) {
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setDeviceId(device.getId());
        deviceDTO.setDeviceCode(device.getCode());
        deviceDTO.setPhase(device.getPhase());
        deviceDTO.setLineId(device.getPole().getOrg().getId());
        deviceDTO.setPoleId(device.getPole().getId());
        deviceDTO.setSupplierCode(device.getSupplier());
        deviceDTO.setDistanceToHeadStation(device.getPole().getDistanceToLastPole());

        return deviceDTO;
    }

}
