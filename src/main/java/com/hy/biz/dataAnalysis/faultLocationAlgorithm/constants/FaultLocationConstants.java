package com.hy.biz.dataAnalysis.faultLocationAlgorithm.constants;

/**
 * ===================
 * 故障定位常量        ｜
 * ===================
 *
 * @author shiwentao
 * @package com.hy.biz.dataAnalysis.faultLocationAlgorithm.constants
 * @create 2023/6/16 10:36
 **/
public class FaultLocationConstants {

    /**
     * 时间格式纳秒pattern
     */
    public static final String NANO_PATTERN = "yyyy:MM:dd HH:mm:ss.SSSSSSSSS";

    /**
     * 异常
     */
    public static final String EMPTY_FAULT_WAVE_SET_ERROR = "故障波形集合为空";
    public static final String UNKNOWN_WAVEFORM_TYPE_ERROR = "未知波形数据类型";
    public static final String NONE_VALIDATED_FAULT_WAVE_ERROR = "无通过校验的故障波形";
    public static final String FAIL_TO_LOCATE_REFERENCE_POINT_ERROR = "无法定位参照点";


}
