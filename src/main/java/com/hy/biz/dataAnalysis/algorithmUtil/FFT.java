package com.hy.biz.dataAnalysis.algorithmUtil;

import java.util.*;
import java.util.stream.Collectors;

/*************************************************************************
 *  Compilation:     javac FFT.java
 *  Execution:       java FFT N
 *  Dependencies:    Complex.java
 *
 * <p>
 *
 *  Compute the FFT and inverse FFT of a length N complex sequence.
 *  Bare-bones implementation that runs in O(N log N) time. Our goal
 *  is to optimize the clarity of the code, rather than performance.
 *
 * <p>
 *
 *  Limitations
 *  ----------------------------------------------------------------------
 *   -  assumes N is a power of 2
 *   -  not the most memory efficient algorithm (because it uses
 *      an object type for representing complex numbers and because
 *      it re-allocates memory for the subarray, instead of doing
 *      in-place or reusing a single temporary array)
 *************************************************************************/

public class FFT {

    /**
     * compute the FFT of a Complex array, assuming its length is a power of 2
     *
     * @param inputArr The complex array
     * @return The FFT of the complex array
     */
    public static Complex[] fft(Complex[] inputArr) {

        int N = inputArr.length;

        // Base case for FFT recursion, single point DFT is the point itself
        if (N == 1) return new Complex[]{inputArr[0]};

        // Ensure inputArr length is a power of 2 for Cooley-Tukey FFT algorithm
        if (N % 2 != 0) throw new RuntimeException("N is not a power of 2");

        // Split inputArr into even and odd indexed arrays
        Complex[] evenInput = new Complex[N / 2];
        Complex[] oddInput = new Complex[N / 2];
        for (int i = 0; i < N / 2; i++) {
            evenInput[i] = inputArr[2 * i];
            oddInput[i] = inputArr[2 * i + 1];
        }

        // Recursively compute FFT for even and odd arrays
        Complex[] evenFFT = fft(evenInput);
        Complex[] oddFFT = fft(oddInput);

        // Combine results from evenFFT and oddFFT
        Complex[] combinedFFT = new Complex[N];
        for (int i = 0; i < N / 2; i++) {
            double ith = -2 * i * Math.PI / N;
            Complex wi = new Complex(Math.cos(ith), Math.sin(ith));

            combinedFFT[i] = evenFFT[i].plus(wi.times(oddFFT[i]));
            combinedFFT[i + N / 2] = evenFFT[i].minus(wi.times(oddFFT[i]));
        }

        return combinedFFT;
    }

    /**
     * Compute the Inverse Fast Fourier Transform (IFFT) of a complex array.
     * This function assumes the length of the array is a power of 2.
     *
     * @param inputArr The complex array
     * @return The IFFT of the complex array
     */
    public static Complex[] ifft(Complex[] inputArr) {

        int N = inputArr.length;
        Complex[] conjugateArray = new Complex[N];

        // Take conjugate of the input complex array
        for (int i = 0; i < N; i++) {
            conjugateArray[i] = inputArr[i].conjugate();
        }

        // Compute forward FFT on the conjugate array
        conjugateArray = fft(conjugateArray);

        // Take conjugate again and divide each element by the length of the array
        for (int i = 0; i < N; i++) {
            conjugateArray[i] = conjugateArray[i].conjugate().scale(1.0 / N);
        }

        return conjugateArray;
    }


    /**
     * Compute the circular convolution of two complex arrays x and y.
     * It assumes that x and y have the same length and are powers of 2.
     *
     * @param x The first complex array
     * @param y The second complex array
     * @return The circular convolution of the two complex arrays
     */
    public static Complex[] circularConvolve(Complex[] x, Complex[] y) {

        // Ensure the length of the extended arrays are the same and a power of two
        if (x.length != y.length || (x.length & (y.length - 1)) != 0)
            throw new RuntimeException("The lengths of the input arrays must be equal and a power of two");

        int N = x.length;

        // Compute FFT of each sequence
        Complex[] a = fft(x);
        Complex[] b = fft(y);

        // Perform point-wise multiplication of the transformed sequences
        Complex[] product = new Complex[N];
        for (int i = 0; i < N; i++) {
            product[i] = a[i].times(b[i]);
        }

        // Compute and return inverse FFT of the product
        return ifft(product);
    }


    /**
     * Compute the linear convolution of two complex arrays complexArr1 and complexArr2.
     * The arrays are first extended to twice their length by appending zeros.
     * Then, the circular convolution of the extended arrays is computed.
     *
     * @param complexArr1 The first complex array
     * @param complexArr2 The second complex array
     * @return The linear convolution of the two complex arrays
     */
    public static Complex[] linearConvolve(Complex[] complexArr1, Complex[] complexArr2) {
        Complex zero = new Complex(0, 0);

        // Extend complexArr1[] and complexArr2[] to twice their length by appending zeros
        Complex[] extendedArr1 = extendArrayWithZeros(complexArr1, 2 * complexArr1.length, zero);
        Complex[] extendedArr2 = extendArrayWithZeros(complexArr2, 2 * complexArr2.length, zero);

        // Compute and return the circular convolution of the extended arrays
        return circularConvolve(extendedArr1, extendedArr2);
    }

    /**
     * Compute the circular convolution of two double arrays arr1 and arr2.
     * The arrays are first transformed to complex arrays and their length is adjusted to be a power of 2.
     * Then, the FFT is applied on the complex arrays, they are multiplied point-wise, and the inverse FFT is applied.
     *
     * @param arr1 The first array
     * @param arr2 The second array
     * @return The circular convolution of the two arrays
     */
    public static Double[] linearConvolve(double[] arr1, double[] arr2) {
        // Adjust the length of arr1 and arr2 to be a power of 2 by appending zeros
        arr1 = pow2DoubleArr(arr1);
        arr2 = pow2DoubleArr(arr2);

        // Convert arr1 and arr2 into Complex arrays
        Complex[] complexArr1 = new Complex[arr1.length];
        Complex[] complexArr2 = new Complex[arr2.length];
        for (int i = 0; i < arr1.length; i++) {
            complexArr1[i] = new Complex(arr1[i], 0);
            complexArr2[i] = new Complex(arr2[i], 0);
        }

        // Perform the circular convolution using FFT and IFFT
        Complex[] convolutionComplex = circularConvolve(complexArr1, complexArr2);

        // Convert the Complex result back into a Double array
        Double[] convolution = new Double[convolutionComplex.length];
        for (int i = 0; i < convolutionComplex.length; i++) {
            convolution[i] = convolutionComplex[i].getRe();
        }

        return convolution;
    }

    /**
     * 计算卷积
     *
     * @param data 波形数据
     * @param lowPassFilter 低通滤波器
     * @return 处理后的数据
     */
    public static double[] computeConvolution(double[] data, double[] lowPassFilter) {
        int inLength = data.length;
        int paramsLength = lowPassFilter.length;
        int resultLength = inLength + paramsLength - 1;

        double[] copyArray = Arrays.copyOf(data, resultLength);
        Arrays.fill(copyArray, inLength, resultLength, 0D);

        double[] result = new double[resultLength];
        for (int i = 0; i < resultLength; i++) {
            double sum = 0D;
            for (int j = 0; j < paramsLength; j++) {
                if (i >= j) {
                    sum += lowPassFilter[j] * copyArray[i - j];
                }
            }
            result[i] = sum;
        }

        return result;
    }

    /**
     * Display an array of Complex numbers to standard output
     */
    public static void show(Complex[] x, String title, Integer length) {
        if (x == null || length == null || length <= 0)
            throw new IllegalArgumentException("Invalid input");

        System.out.println(title);
        System.out.println("-------------------");

        int complexLength = x.length;
        if (complexLength == 0) {
            System.out.println("Empty Complex array");
            return;
        }
        // Use String.format() for precise control over output format
        System.out.printf("%.4f%n", x[0].abs() / length);
        for (int i = 1; i < complexLength / 2; i++) {
            System.out.printf("%.4f", x[i].abs() * 2 / length);
            if (i != complexLength / 2 - 1) {
                System.out.print(",");
            }
        }

        System.out.println();
    }

    /**
     * 去偏移量
     *
     * @param originalArr 原数组
     * @return 目标数组
     */
    public static Double[] deskew(Double[] originalArr) {

        Objects.requireNonNull(originalArr, "Input array must not be null");

        if (originalArr.length == 0) return new Double[0];

        Double[] resArr = originalArr.clone(); // Cloning the array to prevent modifications to the original array

        // Calculating the sum
        double sum = 0;
        for (double value : originalArr)
            sum += value;

        // Calculating the average
        double average = sum / originalArr.length;

        // Subtracting the average
        for (int i = 0; i < resArr.length; i++)
            resArr[i] -= average;

        return resArr;
    }

    /**
     * 根据坐标集 以及坐标对应值 找出 最大值坐标附近40个点的坐标集合 即需要剔除的点位
     */
    public static List<Integer> getRemoveIndexList(List<Integer> indexList, Map<Integer, Double> indexValueMap) {

        // Instead of returning null, it's better to return an empty list.
        if (indexList == null || indexList.size() <= 1) {
            return new ArrayList<>();
        }

        // Find the maximum value index
        Integer maxIndex = getMaxValueIndex(indexValueMap);

        // Safety check, if maxIndex is null, return empty list
        if (maxIndex == null) {
            return new ArrayList<>();
        }

        indexValueMap.remove(maxIndex);

        // Find the indices to be removed
        List<Integer> indicesToRemove = deleteMaxIndex(maxIndex, indexList, indexValueMap);

        // Add all indices to the result list
        List<Integer> result = new ArrayList<>(indicesToRemove);

        // Generate new index set based on the current map
        List<Integer> nextIndexes = new ArrayList<>(indexValueMap.keySet());

        // Recursively find and remove indices
        result.addAll(getRemoveIndexList(nextIndexes, indexValueMap));

        return result;
    }

    /**
     * 根据最大值坐标点 、坐标集合 、 坐标map 找出需要剔除的点位
     *
     * @param maxIndex 最大值坐标
     * @param integerList 坐标集合
     * @param integerDoubleMap 坐标map
     * @return 剔除坐标点集合
     */
    public static List<Integer> deleteMaxIndex(Integer maxIndex, List<Integer> integerList, Map<Integer, Double> integerDoubleMap) {
        List<Integer> indicesToRemove = integerList.stream()
                .filter(i -> Math.abs(i - maxIndex) <= 40)
                .collect(Collectors.toList());

        indicesToRemove.forEach(integerDoubleMap::remove);
        return indicesToRemove;
    }


    /**
     * 找最大值坐标点 即map集合中value值最大对应的key
     *
     * @param integerDoubleMap map
     * @return 坐标点最大值
     */
    public static Integer getMaxValueIndex(Map<Integer, Double> integerDoubleMap) {
        return integerDoubleMap.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }


    // 私有方法==========================================================================================================


    /**
     * Helper method to extend a complex array with zeros.
     *
     * @param array The original complex array
     * @param length The desired length of the new array
     * @param zero The complex zero to be appended
     * @return The extended complex array
     */
    private static Complex[] extendArrayWithZeros(Complex[] array, int length, Complex zero) {

        Complex[] result = new Complex[length];
        System.arraycopy(array, 0, result, 0, array.length);
        for (int i = array.length; i < length; i++) {
            result[i] = zero;
        }

        return result;
    }

    /**
     * 将数组数据重组成2的幂次方输出
     */
    private static double[] pow2DoubleArr(double[] data) {
        int originalLength = data.length;

        // Calculate the next power of 2
        int nextPowerOfTwo = (int) Math.pow(2, Math.ceil(Math.log(originalLength) / Math.log(2)));

        // If original length is already a power of two, return the original array
        if (nextPowerOfTwo == originalLength)
            return data;

        // Extend and fill the array with zeroes
        double[] extendedData = Arrays.copyOf(data, nextPowerOfTwo);
        Arrays.fill(extendedData, originalLength, nextPowerOfTwo, 0d);

        return extendedData;
    }


}
