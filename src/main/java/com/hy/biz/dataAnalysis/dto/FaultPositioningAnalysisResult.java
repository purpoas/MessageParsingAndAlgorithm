package com.hy.biz.dataAnalysis.dto;

import lombok.Data;

/**
 * @author shiwentao
 * @package com.hy.biz.dataAnalysis.dto
 * @create 2023-05-29 16:50
 **/
@Data
public class FaultPositioningAnalysisResult {

    /**
     * 起始与终点变电站的距离
     */
    private double distanceBetweenStations;

    /**
     * 距离和故障最近的杆塔号
     */
    private long nearestPoleId;


}
