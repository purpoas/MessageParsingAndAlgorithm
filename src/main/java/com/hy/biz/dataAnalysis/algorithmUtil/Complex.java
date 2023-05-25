package com.hy.biz.dataAnalysis.algorithmUtil;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Complex {

    private double re; // the real part, final
    private double im; // the imaginary part, final

    /**
     * create a new object with the given real and imaginary parts
     * @param real real part
     * @param imag imag part
     */
    public Complex(double real, double imag) {
        re = real;
        im = imag;
    }

    /**
     * @return abs/modulus/magnitude
     */
    public double abs() {
        return Math.hypot(re, im);
    }

    /**
     * @return angle/phase/argument, normalized to be between -pi and pi
     */
    public double phase() {
        return Math.atan2(im, re);
    }

    /**
     * Performs addition, subtraction, multiplication, and division
     * between this complex number and another complex number, respectively.
     *
     * @param b another Complex number
     */
    public Complex plus(Complex b) {
        Complex a = this;
        double real = a.re + b.re;
        double imag = a.im + b.im;
        return new Complex(real, imag);
    }

    // a static version of plus
    public static Complex plus(Complex a, Complex b) {
        double real = a.re + b.re;
        double imag = a.im + b.im;
        return new Complex(real, imag);
    }

    public Complex minus(Complex b) {
        Complex a = this;
        double real = a.re - b.re;
        double imag = a.im - b.im;
        return new Complex(real, imag);
    }

    public Complex times(Complex b) {
        Complex a = this;
        double real = a.re * b.re - a.im * b.im;
        double imag = a.re * b.im + a.im * b.re;
        return new Complex(real, imag);
    }

    public Complex divides(Complex b) {
        Complex a = this;
        return a.times(b.reciprocal());
    }

    /**
     * Scales the complex number by a given real number
     *
     * @return a new object whose value is (this * alpha),
     */
    public Complex scale(double alpha) {
        return new Complex(alpha * re, alpha * im);
    }

    /**
     * @return the conjugate of the complex number.
     */
    public Complex conjugate() {
        return new Complex(re, -im);
    }

    /**
     * @return the reciprocal of the complex number
     */
    public Complex reciprocal() {
        double scale = re * re + im * im;
        return new Complex(re / scale, -im / scale);
    }

    /**
     * @return the complex exponential, sine, cosine, and tangent of the complex number, respectively.
     */
    public Complex exp() {
        return new Complex(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
    }

    public Complex sin() {
        return new Complex(Math.sin(re) * Math.cosh(im), Math.cos(re) * Math.sinh(im));
    }

    public Complex cos() {
        return new Complex(Math.cos(re) * Math.cosh(im), -Math.sin(re) * Math.sinh(im));
    }

    public Complex tan() {
        return sin().divides(cos());
    }

    @Override
    public String toString() {
        if (im == 0)
            return String.valueOf(re);
        if (re == 0)
            return im + "i";
        if (im < 0)
            return re + " - " + (-im) + "i";
        return re + " + " + im + "i";
    }

    @Override
    public boolean equals(Object x) {
        if (x == null)
            return false;
        if (this.getClass() != x.getClass())
            return false;
        Complex that = (Complex) x;
        return this.re == that.re && this.im == that.im;
    }

    @Override
    public int hashCode() {
        return Double.valueOf(re).hashCode() ^ Double.valueOf(im).hashCode();
    }


}
