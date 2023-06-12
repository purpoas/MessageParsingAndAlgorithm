package com.hy.biz.dataAnalysis.typeAlgorithm;

import com.hy.biz.dataAnalysis.commonAlgorithm.CommonAlgorithmUtil;
import com.hy.biz.dataAnalysis.dto.FaultWave;
import com.hy.biz.dataAnalysis.extraAlgorithm.ExtraAlgorithmUtil;
import com.hy.biz.util.ListUtil;
import com.hy.config.HyConfigProperty;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 故障类型算法 eg : 接地故障 、 过流故障
 */
public class TypeAlgorithmUtil {

    private HyConfigProperty hyConfigProperty;

    /**
     * 行波波形预处理
     *
     * @param data
     * @return
     */
    public Double[] preProcessTravelWave(Double[] data) {


        return null;
    }

    /**
     * 工频电流计算接地故障、过流故障
     *
     * @param faultWaves 故障波形
     * @return 0-未知  1-过流  2-接地
     */
    public static int judgeFrequencyCurrentWaveFaultType(List<FaultWave> faultWaves) {

        List<double[]> doubles = faultWaves.stream().map(FaultWave::getData).map(CommonAlgorithmUtil::shiftWave).collect(Collectors.toList());

        // 判断方法是否大于

        List<Double> I5s = doubles.stream().map(d -> calculateCyclicWavePH(d, 5, 256)).collect(Collectors.toList());

        Double Iset = 1D;

        Double overValue = I5s.stream().filter(aDouble -> aDouble >= Iset).findAny().orElse(null);

        // 过流故障
        if (overValue != null) return 1;

        Map<String, List<FaultWave>> map = ListUtil.convertListToMapList(faultWaves, FaultWave::getPoleId);

        List<Double> I0List = new ArrayList<>();
        for (String poleId : map.keySet()) {
            List<FaultWave> f = map.get(poleId);

            long phaseAmount = f.stream().map(FaultWave::getPhase).count();

            // 计算各杆塔（如果满足该杆塔有三相故障电流） 如果杆塔没有三相电流退出计算
            if (phaseAmount < 3) continue;

            String aDataStr = f.stream().filter(faultWave -> 1 == faultWave.getPhase()).findFirst().get().getData();
            String bDataStr = f.stream().filter(faultWave -> 2 == faultWave.getPhase()).findFirst().get().getData();
            String cDataStr = f.stream().filter(faultWave -> 3 == faultWave.getPhase()).findFirst().get().getData();

            double[] aData = CommonAlgorithmUtil.shiftWave(aDataStr);
            double[] bData = CommonAlgorithmUtil.shiftWave(bDataStr);
            double[] cData = CommonAlgorithmUtil.shiftWave(cDataStr);

            I0List.add(calculateZeroCurrent(aData, bData, cData));
        }

        Double zeroValue = I0List.stream().filter(aDouble -> aDouble >= Iset).findAny().orElse(null);

        // 接地故障
        if (zeroValue != null) return 2;

        return 0;
    }


    /**
     * 计算周波有效值  3.7.1 计算各周波幅值和相位
     *
     * @param data             波形内容
     * @param cyclicWaveSerial 周波序号
     * @param cyclicWaveLength 周波长度 默认256
     * @return 周波对应有效值
     */
    public static double calculateCyclicWavePH(double[] data, int cyclicWaveSerial, int cyclicWaveLength) {

        // 工频波形长度不满足大于10倍周波长度 不参与判断
        if (data.length < 10 * cyclicWaveLength) return 0.0;

        Double[] in = new Double[cyclicWaveLength];

        int cyclicWaveIndexSum = data.length / cyclicWaveLength;

        // 计算的周波超出波形长度 异常返回
        if (cyclicWaveSerial > cyclicWaveIndexSum) return 0.0;

        // 截取波形内容中对应的周波
        System.arraycopy(data, (cyclicWaveSerial - 1) * cyclicWaveLength, in, 0, in.length);

        int n = 1;

        double xrn = 0.0;
        double xin = 0.0;
        for (int i = 0; i < in.length; i++) {
            xrn += (2 * in[i] * Math.cos(2 * Math.PI * n * i / cyclicWaveLength)) / cyclicWaveLength;
            xin += -(2 * in[i] * Math.cos(2 * Math.PI * n * i / cyclicWaveLength)) / cyclicWaveLength;
        }

        double xn = Math.atan(xin / xrn);
        // φn 暂时计算不使用
//        double φn = Math.sqrt(xin * xin + xrn * xrn);
//        if (xrn == 0) {
//            φn = 1.5 * Math.PI;
//        } else if (xrn > 0) {
//            φn = φn;
//        } else {
//            φn = φn + Math.PI;
//        }

        double ph = xn / Math.sqrt(2);

        return ph;
    }

    /**
     * 计算周波有效值是否达标 需输出参考值
     *
     * @param data             波形内容
     * @param cyclicWaveSerial 周波序号
     * @param cyclicWaveLength 周波长度 默认256
     * @return 周波对应有效值
     */
    public static boolean judgeCyclicWavePH(Double[] data, int cyclicWaveSerial, int cyclicWaveLength) {

        // 工频波形长度不满足大于10倍周波长度 不参与判断
        if (data.length < 10 * cyclicWaveLength) return false;

        Double[] in = new Double[cyclicWaveLength];

        int cyclicWaveIndexSum = data.length / cyclicWaveLength;

        // 计算的周波超出波形长度 异常返回
        if (cyclicWaveSerial > cyclicWaveIndexSum) return false;

        // 截取波形内容中对应的周波
        System.arraycopy(data, (cyclicWaveSerial - 1) * cyclicWaveLength, in, 0, in.length);

        int n = 1;

        double xrn = 0.0;
        double xin = 0.0;
        for (int i = 0; i < in.length; i++) {
            xrn += (2 * in[i] * Math.cos(2 * Math.PI * n * i / cyclicWaveLength)) / cyclicWaveLength;
            xin += -(2 * in[i] * Math.cos(2 * Math.PI * n * i / cyclicWaveLength)) / cyclicWaveLength;
        }

        double φn = Math.sqrt(xin * xin + xrn * xrn);
        double xn = Math.atan(xin / xrn);
        if (xrn == 0) {
            φn = 1.5 * Math.PI;
        } else if (xrn > 0) {
            φn = φn;
        } else {
            φn = φn + Math.PI;
        }

        double ph = xn / φn;

        if (ph > 20) return true;

        return false;
    }


    /**
     * 零序电压电流计算  3.7.3 零序电压电流计算
     *
     * @param aData A相波形内容
     * @param bData B相波形内容
     * @param cData C相波形内容
     * @return 零序电流值
     */
    public static double calculateZeroCurrent(double[] aData, double[] bData, double[] cData) {

        double[] i0 = synthesisZeroCurrent(aData, bData, cData);

        int cyclicWaveIndexSum = i0.length / 256;

        List<Double> I0List = new ArrayList<>();

        for (int i = 0; i < cyclicWaveIndexSum; i++) {
            // 计算周波有效值
            I0List.add(calculateCyclicWavePH(i0, i + 1, 256));
        }

        // 取各波周有效值的最大值
        return Collections.max(I0List);
    }

    /**
     * 合成零序电压电流  3.7.3 (1) 零序电压电流计算
     *
     * @param aData A相波形内容
     * @param bData B相波形内容
     * @param cData C相波形内容
     * @return 合成零序电流
     */
    public static double[] synthesisZeroCurrent(double[] aData, double[] bData, double[] cData) {
        int aLength = aData.length;
        int bLength = bData.length;
        int cLength = cData.length;
        int i0Length = 0;

        // 找出三相波形中长度最短的
        if (aLength < bLength && aLength < cLength) {
            i0Length = aLength;
        } else if (bLength < aLength && bLength < cLength) {
            i0Length = bLength;
        } else if (cLength < aLength && cLength < bLength) {
            i0Length = cLength;
        }

        // 波形预处理
        aData = ExtraAlgorithmUtil.preProcessFrequencyWave(aData);
        bData = ExtraAlgorithmUtil.preProcessFrequencyWave(aData);
        cData = ExtraAlgorithmUtil.preProcessFrequencyWave(aData);

        double[] i0 = new double[i0Length];

        // 波形叠加
        for (int i = 0; i < i0Length; i++) {
            i0[i] = aData[i] + bData[i] + cData[i];
        }

        return i0;
    }

    /**
     * 零序电流极性计算  3.6.3 A
     *
     * @param data 波形内容
     * @return 零序电流极性
     */
    public static boolean calculateZeroCurrentAbsolute(double[] data) {
        int N = 256;        //一个周波对应点位
        int pos = calculateZeroCurrentPosLocation(data);    //触发位置

        int prePoint = N / 4;
        int sufPoint = N / 2;

        int startIndex = Math.max(pos - prePoint, 0);
        int endIndex = Math.min(pos + sufPoint, data.length);

        // 数据处理后的原始波形
        double[] subData = new double[endIndex - startIndex + 1];
        System.arraycopy(data, startIndex, subData, 0, subData.length);

        // 计算突变量
        List<Double> q = CommonAlgorithmUtil.calculateCurrentMutationEnergy(subData, 3, 5);

        // 将Q(k)取绝对值得到E(k)
        List<Double> e = q.stream().map(Math::abs).collect(Collectors.toList());

        double eMax = Collections.max(e);

        // 寻找E(k)极值点坐标
        List<Integer> eMaxIndexList = CommonAlgorithmUtil.calculateWaveExtremePoint(e, 3);

        // 寻找E(k)极值点坐标集合中 第一个突变量能量大于等于0.2倍E(k)Max的点
        int firstIndex = 0;
        for (int i = 0; i < eMaxIndexList.size(); i++) {
            if (e.get(eMaxIndexList.get(i)) >= 0.2 * eMax) {
                firstIndex = eMaxIndexList.get(i);
                break;
            }
        }

        double qFirstMaxValue = q.get(firstIndex);

        if (qFirstMaxValue > 0) return true;

        return false;

    }

    /**
     * 零序电流相关系数计算  3.6.3 B
     *
     * @param data 波形内容
     * @return 零序电流极性
     */
    public static double calculateZeroCurrentCoefficient(double[] data) {

        if (ArrayUtils.isEmpty(data)) return 0.0;

        int N = 256;        //一个周波对应点位
        int pos = calculateZeroCurrentPosLocation(data);    //触发位置

        int startIndex = Math.max(pos - N, 0);
        int endIndex = Math.min(pos + N, data.length);

        List<Double> xn = new ArrayList<>();
        List<Double> yn = new ArrayList<>();
        for (int i = startIndex; i < pos; i++) {
            xn.add(data[i]);
        }

        for (int i = pos; i < endIndex; i++) {
            yn.add(data[i]);
        }

        double xnMax = Collections.max(xn);
        double xnMin = Collections.min(xn);
        double ynMax = Collections.max(yn);
        double ynMin = Collections.min(yn);

        // 归一化
        xn = xn.stream().map(aDouble -> (2 * aDouble - xnMin) / (xnMax - xnMin) - 1).collect(Collectors.toList());
        yn = yn.stream().map(aDouble -> (2 * aDouble - ynMin) / (ynMax - ynMin) - 1).collect(Collectors.toList());

        double up = 0.0;
        for (int i = 0; i < xn.size(); i++) {
            up += xn.get(i) * yn.get(i);
        }

        double down = 0.0;
        for (int i = 0; i < xn.size(); i++) {
            down += xn.get(i) * xn.get(i) * yn.get(i) * yn.get(i);
        }

        return up / Math.sqrt(down);
    }

    /**
     * 零序电流触发位置Pos计算  3.6.3 A、B计算中使用通用方法
     *
     * @param data 波形内容
     * @return 触发位置
     */
    private static int calculateZeroCurrentPosLocation(double[] data) {
        int fpower = 12800; //设备采样率
        int threshold = 5;  //电流阈值
        int N = 256;        //一个周波对应点位

        // 从2N+1点开始计算pos
        List<Double> deltaI = new ArrayList<>();
        for (int n = 2 * N; n < data.length; n++) {
            // FIXME 这里需要确定是n-N还是n-N-1
            double i = Math.abs(Math.abs(data[n] - data[n - N]) - Math.abs(data[n] - data[n - 2 * N]));
            deltaI.add(i);
        }

        // Pos是第一个大于触发阈值的点
        int pos = 0;
        for (int i = 0; i < deltaI.size(); i++) {
            if (deltaI.get(i) > threshold) {
                pos = i;
                break;
            }
        }

        // 校验Pos有效性
        if ((fpower / 50 * 7 / 2 >= pos || pos >= fpower / 50 * 9 / 2)) {
            pos = 1024;
        }

        return pos;
    }


}
