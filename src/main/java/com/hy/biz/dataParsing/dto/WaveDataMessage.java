package com.hy.biz.dataParsing.dto;

import com.hy.biz.dataAnalysis.commonAlgorithm.CommonAlgorithmUtil;
import com.hy.biz.dataAnalysis.extraAlgorithm.ExtraAlgorithmUtil;
import com.hy.biz.dataParsing.constants.MessageType;
import com.hy.biz.dataParsing.parser.ParserHelper;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataPush.dto.DeviceDTO;
import com.hy.biz.dataPush.dto.PoleDTO;
import com.hy.config.HyConfigProperty;
import com.hy.domain.WaveData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shiwentao
 * @package com.hy.idds.biz.dataParsing.MessageEntity
 * @description
 * @create 2023-04-21 10:13
 **/
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class WaveDataMessage extends BaseMessage {

    /**
     * 当前数据包段包含的波形数据长度
     */
    private int dataPacketLength;

    /**
     * 波形数据
     */
    private String waveData;

    /**
     * 波形起始时间
     */
    private String waveStartTime;

    /**
     * 波形数据总长度
     */
    private int waveDataLength;

    /**
     * 当前数据报段号
     */
    private int segmentNumber;

    /**
     * 数据总包数
     */
    private int dataPacketNumber;

    /**
     * 备用
     */
    private String reserved;

    /**
     * 是否故障
     */
    private boolean isFault;

    /**
     * 故障性质
     */
    private Integer relaFlag;

    public WaveData transform(ParserHelper parserHelper, long timeStamp, String deviceCode) {
        return parserHelper.setWaveDataProperty(this, timeStamp, deviceCode);
    }

    public void calculateIsFault(HyConfigProperty hyConfigProperty) {
        // 根据波形类型判断是否是故障波形
        int waveType = getMessageType();
        boolean flag = false;
        double[] data = CommonAlgorithmUtil.shiftWave(getWaveData());
        switch (waveType) {
            case MessageType
                    .TRAVELLING_WAVE_CURRENT:
                // 波形预处理
                double[] preTravelWaveData = ExtraAlgorithmUtil.preProcessTravellingWave(data);
                // 故障波形判断
                flag = ExtraAlgorithmUtil.isValidTravellingWave(preTravelWaveData, hyConfigProperty.getConstant().getTravelThreshold());
                break;
            case 3:
            case 5:
                // 波形预处理
                double[] frequencyWaveData = ExtraAlgorithmUtil.preProcessFrequencyWave(data);
                // 故障波形判断
                flag = ExtraAlgorithmUtil.isValidPowerFreqCurrentOrVoltage(frequencyWaveData, hyConfigProperty);
                break;
            default:
                break;
        }

        this.isFault = flag;
    }

    public FaultWave transform(DeviceDTO deviceDTO, PoleDTO poleDTO) {
        FaultWave faultWave = new FaultWave();
        faultWave.setDeviceId(deviceDTO.getDeviceId());
        faultWave.setDeviceCode(deviceDTO.getDeviceCode());
        faultWave.setPhase(deviceDTO.getPhase());

        faultWave.setLineId(poleDTO.getLineId());
        faultWave.setTopMainLineId(poleDTO.getTopMainLineId());
        faultWave.setPoleId(poleDTO.getPoleId());
        faultWave.setPoleSerial(poleDTO.getPoleSerial());
        faultWave.setDistanceToHeadStation(poleDTO.getDistanceToHeadStation());

        faultWave.setWaveType(this.getMessageType());
        faultWave.setHeadTime(this.getWaveStartTime());
        faultWave.setData(this.getWaveData());

        return faultWave;
    }

}

