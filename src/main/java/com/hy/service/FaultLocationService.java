package com.hy.service;

/**
 * ==========================
 * 故障定位接口               ｜
 * ==========================
 *
 * @author shiwentao
 * @package com.hy.service.impl
 * @create 2023/6/7 10:30
 **/
public interface FaultLocationService {

    /**
     * 定位故障杆塔
     * @param poleAId 塔A的ID
     * @param poleBId 塔B的ID
     * @param distanceBetweenTowers 两塔之间的距离
     * @return 故障杆塔id以及故障位置，例如：5号杆塔+12米
     */
    String locateFaultTower(long poleAId, long poleBId, double distanceBetweenTowers);


}
