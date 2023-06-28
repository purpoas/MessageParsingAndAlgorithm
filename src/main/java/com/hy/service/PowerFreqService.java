package com.hy.service;

import com.hy.controller.vo.PowerFrequentCurrentOrVoltageResultVO;

/**
 * =========================
 * 工频电压计算Service       ｜
 * =========================
 *
 * @author shiwentao
 * @package com.hy.service
 * @create 2023/6/21 09:35
 **/
public interface PowerFreqService {

    /**
     *
     * 计算工频电流/电压
     *
     * @param phaseAWaveId 相位为A的波形数据ID
     * @param phaseBWaveId 相位为B的波形数据ID
     * @param phaseCWaveId 相位为C的波形数据ID
     * @return PowerFrequentCurrentOrVoltageResultVO
     */
    PowerFrequentCurrentOrVoltageResultVO computePowerFreqCurrentOrVoltage(long phaseAWaveId, long phaseBWaveId, long phaseCWaveId);


}
