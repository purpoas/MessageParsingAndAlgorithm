package com.hy.biz.dataPush;

import com.hy.biz.dataPush.dto.DeviceDTO;
import com.hy.biz.dataPush.dto.PushDataType;
import com.hy.biz.dataResolver.entity.BaseMessage;

public interface DataPushService {

    /**
     * 数据推送
     *
     * @param message 报文
     * @return 数据推送结果
     */
    boolean push(String data, BaseMessage message, PushDataType pushDataType);

    /**
     * 是否在第三方平台存在设备编号
     *
     * @param deviceCode 设备编号
     * @return 设备是否存在
     */
    boolean deviceExists(String deviceCode);

    /**
     * 根据设备编号，从第三方平台获取该设备的详细信息
     *
     * @param deviceCode 设备编号
     * @return 设备实体类
     */
    DeviceDTO findDeviceByCode(String deviceCode);

}
