package com.hy.biz.dataAnalysis.util;

import java.util.Arrays;

/**
 * ============================================================
 * ｜
 * ============================================================
 *
 * @author shiwentao
 * @package com.hy.biz.dataAnalysis.util
 * @create 2023/6/20 16:56
 **/
public class TypeConverter {

    public static double[] convertStringToDoubleArray(String str) {
        return Arrays.stream(str.split(","))
                .mapToDouble(Double::parseDouble)
                .toArray();
    }


}
