package com.hy.biz.dataAnalysis.dto;

import lombok.Data;

/**
 * ===========================
 * 故障定位（双端）分析结果      ｜
 *============================
 *
 * @author shiwentao
 * @package com.hy.biz.dataAnalysis.dto
 * @create 2023-05-29 16:50
 **/
@Data
public class FaultLocationAnalysisResult {

    /**
     * 起始与终点变电站的距离
     */
    private double distanceBetweenStations;

    /**
     * 距离和故障最近的杆塔号
     */
    private String nearestPoleId;

    /**
     * 故障点与最近杆塔的距离
     */
    private double distToNearestPole;


}
