package com.hy.service.impl;

import com.hy.biz.dataAnalysis.featureAlgorithm.FeatureBreakCalculateUtil;
import com.hy.biz.dataAnalysis.typeAlgorithm.FrequencyCharacterCalculateUtil;
import com.hy.biz.dataAnalysis.util.TypeConverter;
import com.hy.controller.vo.PowerFrequentCurrentOrVoltageResultVO;
import com.hy.domain.WaveData;
import com.hy.repository.WaveDataRepository;
import com.hy.service.PowerFreqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author shiwentao
 * @package com.hy.service.impl
 * @create 2023/6/21 09:37
 **/
@Service
@Slf4j
public class PowerFreqServiceImpl implements PowerFreqService {

    private final WaveDataRepository waveDataRepository;

    public PowerFreqServiceImpl(WaveDataRepository waveDataRepository) {
        this.waveDataRepository = waveDataRepository;
    }

    @Override
    public PowerFrequentCurrentOrVoltageResultVO computePowerFreqCurrentOrVoltage(long phaseAWaveId, long phaseBWaveId, long phaseCWaveId) {
        List<Long> ids = Arrays.asList(phaseAWaveId, phaseBWaveId, phaseCWaveId);

        List<double[]> data = ids.stream()
                .map(Optional::ofNullable)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(waveDataRepository::findById)
                .map(optionalWaveData -> optionalWaveData.orElseThrow(() -> new IllegalArgumentException("无法根据ID查询到波形")))
                .map(WaveData::getData)
                .map(TypeConverter::convertStringToDoubleArray)
                .collect(Collectors.toList());

        if(data.size() != ids.size())
            throw new IllegalArgumentException("输入参数不能为null");

        double[] aData = data.get(0);
        double[] bData = data.get(1);
        double[] cData = data.get(2);

        double aImax8 = FrequencyCharacterCalculateUtil.IMAX8(aData);  // A相电流/电压
        double bImax8 = FrequencyCharacterCalculateUtil.IMAX8(bData);  // B相电流/电压
        double cImax8 = FrequencyCharacterCalculateUtil.IMAX8(cData);  // C相电流/电压

        double zeroCurrent = FrequencyCharacterCalculateUtil.calculateZeroCurrent(aData, bData, cData);  // 零序电流/电压
        double negCurrent = FeatureBreakCalculateUtil.negSeq(aData, bData, cData);  //负序电流/电压

        PowerFrequentCurrentOrVoltageResultVO resultVO = new PowerFrequentCurrentOrVoltageResultVO();
        resultVO.setPhaseACurrentOrVoltage((int) aImax8);
        resultVO.setPhaseBCurrentOrVoltage((int) bImax8);
        resultVO.setPhaseCCurrentOrVoltage((int) cImax8);
        resultVO.setZeroSequenceCurrentOrVoltage((int) zeroCurrent);
        resultVO.setNegativeSequenceCurrentOrVoltage((int) negCurrent);

        return resultVO;
    }


}
