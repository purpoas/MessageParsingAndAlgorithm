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
    private String lineId;
    private String poleId;
    private String supplierCode;

    public DeviceDTO from(Device device) {
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setDeviceId(device.getId());
        deviceDTO.setDeviceCode(device.getCode());
        deviceDTO.setPhase(device.getPhase());
        deviceDTO.setLineId(device.getPole() != null && device.getPole().getOrg() != null ? String.valueOf(device.getPole().getOrg().getId()) : null);
        deviceDTO.setPoleId(device.getPole() != null ? String.valueOf(device.getPole().getId()) : null);
        deviceDTO.setSupplierCode(device.getSupplier());

        return deviceDTO;
    }

}
