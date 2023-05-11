package com.hy.biz.dataResolver.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shiwentao
 * @package com.hy.biz.dataResolver
 * @description
 * @create 2023-05-10 09:33
 **/
public class ParamCodeRegistry {
    private static final Map<String, String> map = new HashMap<>();
    private static final Map<String, String> unmodifiableMap;

    static {
        registerParamCode();
        unmodifiableMap = Collections.unmodifiableMap(map);
    }

    private static void registerParamCode() {
        map.put("0x0001", "行波召回时间");
        map.put("0x0002", "行波电流阈值");
        map.put("0x0003", "行波电流采集时长");
        map.put("0x0004", "行波电流采集频率");
        map.put("0x0005", "工频电流召回时间");
        map.put("0x0006", "工频电流阈值");
        map.put("0x0007", "工频电流采集时长");
        map.put("0x0008", "工频电流采样频率");
        map.put("0x0009", "工作状态上报时间");
        map.put("0x000A", "工况数据采集间隔");
        map.put("0x000B", "电场电压召回时间");
        map.put("0x000C", "电场电压阈值");
        map.put("0x000D", "电场电压采集时长");
        map.put("0x000E", "电场电压采样频率");
    }

    public static Map<String, String> getParamCodeMap() {
        return unmodifiableMap;
    }

    private ParamCodeRegistry() {}

}
