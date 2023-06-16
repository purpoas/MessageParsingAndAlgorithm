package com.hy.biz.dataAnalysis.faultLocationAlgorithm.exception;

/**
 * ================================
 *  故障（双端）定位异常类            ｜
 * ================================
 *
 * @author shiwentao
 * @package com.hy.biz.dataAnalysis.faultLocationAlgorithm.exception
 * @create 2023/6/16 10:29
 **/
public class FaultLocationException extends RuntimeException{

    public FaultLocationException(String errorMsg) {
        super(errorMsg);
    }

    public FaultLocationException(String errorMsg, Throwable cause) {
        super(errorMsg, cause);
    }


}
