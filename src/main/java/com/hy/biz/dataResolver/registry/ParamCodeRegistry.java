package com.hy.biz.dataResolver.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 参数读取应答报文的参数编码注册类
 *
 * @author shiwentao
 * @package com.hy.biz.dataResolver
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
        map.put("0x0001", "waveCallbackTime");
        map.put("0x0002", "waveThreshold");
        map.put("0x0003", "waveCollectionTime");
        map.put("0x0004", "waveSampleRate");
        map.put("0x0005", "powerCallbackTime");
        map.put("0x0006", "powerThreshold");
        map.put("0x0007", "powerCollectionTime");
        map.put("0x0008", "powerSampleRate");
        map.put("0x0009", "workReportTime");
        map.put("0x000A", "workStatusCollectDuration");
        map.put("0x000B", "groundCallbackTime");
        map.put("0x000C", "groundThreshold");
        map.put("0x000D", "groundCollectionTime");
        map.put("0x000E", "groundSampleRate");
    }

    public static Map<String, String> getParamCodeMap() {
        return unmodifiableMap;
    }

    private ParamCodeRegistry() {}

}
