package com.hy.biz.dataAnalysis.algorithmUtil;

import com.hy.config.HyConfigProperty;
import org.springframework.stereotype.Component;

@Component
public class AnalysisConstants {

    public static final String FAULT_NATURE_SHORT_AB = "AB两相短路";
    public static final String FAULT_NATURE_SHORT_AC = "AC两相短路";
    public static final String FAULT_NATURE_SHORT_BC = "BC两相短路";
    public static final String FAULT_NATURE_SHORT_ABC = "三相短路";

    public static final String FAULT_NATURE_BREAK_AB = "AB两相断路";
    public static final String FAULT_NATURE_BREAK_AC = "AC两相断路";
    public static final String FAULT_NATURE_BREAK_BC = "BC两相断路";

    public static final String FAULT_NATURE_BREAK_A = "A相断路";
    public static final String FAULT_NATURE_BREAK_B = "B相断路";
    public static final String FAULT_NATURE_BREAK_C = "C相断路";

    public static final String FAULT_NATURE_GROUND_A = "A相接地";
    public static final String FAULT_NATURE_GROUND_B = "B相接地";
    public static final String FAULT_NATURE_GROUND_C = "C相接地";

    public static final String FAULT_NATURE_NORMAL = "正常运行";
    public static final String FAULT_NATURE_INS = "增负荷";

    public static final String FAULT_NATURE_FLASHY_FLOW = "合闸涌流";
    public static final String FAULT_NATURE_LOAD_UNDULATE = "负荷波动";

    public static final int PHASE_A = 1;    //A相
    public static final int PHASE_B = 2;    //B相
    public static final int PHASE_C = 3;    //C相

    public static int I0AM;         //零序电流阈值
    public static double IBPH_MAX;  //三相不平衡度
    public static int IMIN;         //最小工作电流
    public static int UMIN;         //最小工电压

    public static double Iset;       // 过流定值   每条线路配置不同
    public static double I0set;      // 零序定值   每条线路配置不同

    public static int CYCLE_WAVE_LENGTH;    //周波长度

    public AnalysisConstants(HyConfigProperty hyConfigProperty) {
        IMIN = hyConfigProperty.getAlgorithm().getIMIN();
        UMIN = hyConfigProperty.getAlgorithm().getUMIN();
        IBPH_MAX = hyConfigProperty.getAlgorithm().getIBPH_MAX();
        I0AM = hyConfigProperty.getAlgorithm().getI0AM();

        Iset = hyConfigProperty.getAlgorithm().getIset();
        I0set = hyConfigProperty.getAlgorithm().getI0set();

        CYCLE_WAVE_LENGTH = hyConfigProperty.getConstant().getCycleWaveLength();
    }
}
