package com.hy.biz.dataPush;

import com.hy.biz.dataPush.dto.DeviceDTO;
import com.hy.biz.dataPush.dto.LineDTO;
import com.hy.biz.dataPush.dto.PoleDTO;
import com.hy.biz.dataPush.dto.PushDataType;
import com.hy.biz.dataResolver.dto.BaseMessage;

import java.util.List;

public interface DataPushService {

    /**
     * 数据推送
     *
     * @param message 报文
     * @return 数据推送结果
     */
    boolean push(String data, BaseMessage message, PushDataType pushDataType);

    /**
     * 根据设备编号，从第三方平台获取该设备的详细信息
     *
     * @param deviceCode 设备编号
     * @return 设备实体类
     */
    DeviceDTO findDeviceByCode(String deviceCode);

    /**
     * 根据线路编号，从第三方平台获取该线路的详细信息
     *
     * @param lineId 线路编号
     * @return
     */
    LineDTO findLineByLineId(String lineId);

    /**
     * 根据线路编号，从第三方平台获取该线路下所有杆塔的详细信息
     *
     * @param lineId 线路编号
     * @return
     */
    List<PoleDTO> findPolesByLineId(String lineId);

    /**
     * 根据线路编号、杆塔编号，从第三方平台获取该线路下该杆塔的详细信息
     *
     * @param lineId 线路编号
     * @param poleId 杆塔编号
     * @return
     */
    PoleDTO getPoleByPoleId(String lineId, String poleId);


}
