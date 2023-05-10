package com.hy.biz.dataPush;

import com.hy.biz.dataPush.dto.DeviceDTO;
import com.hy.biz.dataPush.dto.PushDataType;

public interface DataPushService {

    /**
     * 数据推送
     *
     * @param dataJson JSON格式数据
     * @param dataType 数据类型
     * @return
     */
    boolean push(String dataJson, PushDataType dataType);

    /**
     * 是否在第三方平台存在设备编号
     *
     * @param deviceCode 设备编号
     * @return
     */
    boolean isExistDevice(String deviceCode);

    /**
     * 根据设备编号，从第三方平台获取该设备的详细信息
     *
     * @param deviceCode 设备编号
     * @return
     */
    DeviceDTO findDeviceByCode(String deviceCode);

//    /**
//     * 根据杆塔编号，从第三方平台获取该线路的详细信息
//     * 根据线路信息，从第三方平台后去
//     * @param lineId 线路编号
//     * @return
//     */
//    LineDistanceDTO getLineDistanceByLineId(String lineId);
//
//    /**
//     * 根据线路编号，从第三方平台获取该线路下所有杆塔的详细信息
//     *
//     * @param lineId 线路编号
//     * @return
//     */
//    List<TowerDetailsDTO> getTowerDistanceByLineId(String lineId);
//
//    /**
//     * 根据线路编号、杆塔编号，从第三方平台获取该线路下该杆塔的详细信息
//     *
//     * @param lineId  线路编号
//     * @param towerId 杆塔编号
//     * @return
//     */
//    TowerDetailsDTO getTowerDetails(String lineId, String towerId);

}
