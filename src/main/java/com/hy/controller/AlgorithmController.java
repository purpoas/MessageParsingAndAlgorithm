package com.hy.controller;

import com.hy.controller.vo.PowerFrequentCurrentOrVoltageResultVO;
import com.hy.controller.vo.R;
import com.hy.service.PowerFreqService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * =============
 * 算法前端接口  ｜
 * =============
 *
 * @author shiwentao
 * @package com.hy.controller
 * @create 2023/6/20 11:40
 **/
@RestController("/algorithm")
public class AlgorithmController {

    private final PowerFreqService powerFreqService;

    public AlgorithmController(PowerFreqService powerFreqService) {
        this.powerFreqService = powerFreqService;
    }

    @GetMapping("/power-frequent")
    public R<PowerFrequentCurrentOrVoltageResultVO> getPowerFreqCurrentOrVoltage(
            @RequestParam long phaseAWaveId,
            @RequestParam long phaseBWaveId,
            @RequestParam long phaseCWaveId)
    {
        return R.success(powerFreqService.computePowerFreqCurrentOrVoltage(phaseAWaveId, phaseBWaveId, phaseCWaveId));
    }


}
