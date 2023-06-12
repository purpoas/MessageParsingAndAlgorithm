package com.hy.biz.dataAnalysis.algorithmUtil;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

/**
 * 矩阵线性代数运算
 */
public class EjmlUtil {

    public static double[] calculate(double[][] aArray, double[][] bArray) {

        DMatrixRMaj A = new DMatrixRMaj(aArray);
        DMatrixRMaj B = new DMatrixRMaj(bArray);

        DMatrixRMaj AT = new DMatrixRMaj(A.numCols, A.numRows);
        // 执行矩阵倒置
        CommonOps_DDRM.transpose(A, AT);

        DMatrixRMaj ATA = new DMatrixRMaj(AT.getNumRows(), A.getNumCols());
        // 执行矩阵相乘
        CommonOps_DDRM.mult(AT, A, ATA);

        DMatrixRMaj ATAD = new DMatrixRMaj(ATA.numRows, ATA.numCols);
        // 执行矩阵求逆
        CommonOps_DDRM.invert(ATA, ATAD);

        DMatrixRMaj APlus = new DMatrixRMaj(ATAD.getNumRows(), AT.getNumCols());
        // 执行矩阵相乘
        CommonOps_DDRM.mult(ATAD, AT, APlus);

        DMatrixRMaj X = new DMatrixRMaj(APlus.getNumRows(), B.getNumCols());
        // 执行矩阵相乘
        CommonOps_DDRM.mult(APlus, B, X);

        return X.getData();
    }


}
