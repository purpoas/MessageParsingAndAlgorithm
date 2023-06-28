package com.hy.controller.vo;

import lombok.Data;

/**
 * ============================================================
 * ｜
 * ============================================================
 *
 * @author shiwentao
 * @package com.hy.controller.vo
 * @create 2023/6/20 14:54
 **/
@Data
public class PowerFrequentCurrentOrVoltageResultVO {

    private int phaseACurrentOrVoltage;
    private int phaseBCurrentOrVoltage;
    private int phaseCCurrentOrVoltage;
    private int zeroSequenceCurrentOrVoltage;
    private int negativeSequenceCurrentOrVoltage;


}
