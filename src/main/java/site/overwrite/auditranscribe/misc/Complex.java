/*
 * Complex.java
 *
 * Created on 2022-02-12
 * Updated on 2022-06-28
 *
 * Description: Complex number class.
 */

package site.overwrite.auditranscribe.misc;

import site.overwrite.auditranscribe.utils.MathUtils;

import java.util.Objects;

/**
 * Complex number class.
 */
public class Complex {
    // Useful constants
    public static final Complex ZERO = new Complex(0, 0);
    public static final Complex ONE = new Complex(1, 0);
    public static final Complex IMAG_UNIT = new Complex(0, 1);

    // Attributes
    private double re;  // Real part of the complex number
    private double im;  // Imaginary part of the complex number

    /**
     * Creates a complex number with real part <code>re</code> and imaginary part <code>im</code>.
     *
     * @param re The real part of the complex number.
     * @param im The imaginary part of the complex number.
     */
    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    /**
     * Creates a complex number with real part <code>real</code> and imaginary part 0.
     *
     * @param real The real part of the complex number.
     */
    public Complex(double real) {
        re = real;
        im = 0;
    }

    // Overwritten methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        Complex that = (Complex) o;
        return (this.re == that.re) && (this.im == that.im);
    }

    @Override
    public int hashCode() {
        return Objects.hash(re, im);
    }

    // Standard methods

    /**
     * Getter method to get the real part of the <code>Complex</code> object.
     *
     * @return Real part of the complex number.
     */
    public double re() {
        return re;
    }

    /**
     * Getter method to get the imaginary part of the <code>Complex</code> object.
     *
     * @return Real part of the complex number.
     */
    public double im() {
        return im;
    }

    // Standard `Object` methods

    /**
     * Generates a string representation of the complex number.<br>
     * Note that we use "j" for the imaginary unit to follow Python's convention of the imaginary
     * unit.
     *
     * @return String representation of the complex number.
     */
    public String toString() {
        if (im == 0) return re + "";  // Concat "" to the end to make it a string
        if (re == 0) return im + "j";
        if (im < 0) return re + " - " + (-im) + "j";
        return re + " + " + im + "j";
    }

    // Assertion methods

    /**
     * Method that checks if the complex number is purely real.
     *
     * @return Boolean whether the complex number is purely real.
     */
    public boolean isPurelyReal() {
        return im == 0;
    }

    /**
     * Method that checks if the complex number is purely imaginary.
     *
     * @return Boolean whether the complex number is purely imaginary.
     */
    public boolean isPurelyImaginary() {
        return re == 0;
    }

    // Polar/Exponential/Trigonometric form methods

    /**
     * Returns the absolute value/modulus of the complex number.
     *
     * @return Double representing the absolute value/modulus of the complex number.
     */
    public double abs() {
        return Math.hypot(re, im);
    }

    /**
     * Returns the angle/phase/argument of the complex number.
     *
     * @return Double representing the angle/phase/argument of the complex number.
     */
    public double phase() {
        return Math.atan2(im, re);
    }

    // Operator methods

    /**
     * Returns the conjugate of this complex number.
     *
     * @return A <code>Complex</code> object representing the conjugate of this complex number.
     */
    public Complex conjugate() {
        return new Complex(re, -im);
    }

    /**
     * Returns the sum of this complex number with another complex number.
     *
     * @param other The other complex number.
     * @return A <code>Complex</code> object representing the resulting complex number.
     */
    public Complex plus(Complex other) {
        Complex self = this;
        double real = self.re + other.re;
        double imag = self.im + other.im;
        return new Complex(real, imag);
    }

    /**
     * Returns the difference of this complex number with another complex number,
     * i.e. (this - other).
     *
     * @param other The other complex number.
     * @return A <code>Complex</code> object representing the resulting complex number.
     */
    public Complex minus(Complex other) {
        Complex a = this;
        double real = a.re - other.re;
        double imag = a.im - other.im;
        return new Complex(real, imag);
    }

    /**
     * Returns the scaled version of this complex number when scaled by the real number
     * <code>alpha</code>.
     *
     * @param alpha The scale factor.
     * @return A <code>Complex</code> object representing the resulting complex number.
     */
    public Complex scale(double alpha) {
        return new Complex(alpha * re, alpha * im);
    }

    /**
     * Returns the product of this complex number with another complex number.
     *
     * @param other The other complex number.
     * @return A <code>Complex</code> object representing the resulting complex number.
     */
    public Complex times(Complex other) {
        // Check if the other complex number is purely real
        if (other.isPurelyReal()) return this.scale(other.re);

        // Otherwise, perform complex number multiplication
        Complex a = this;
        double real = a.re * other.re - a.im * other.im;
        double imag = a.re * other.im + a.im * other.re;
        return new Complex(real, imag);
    }

    /**
     * Returns the reciprocal of this complex number.
     *
     * @return A <code>Complex</code> object representing the reciprocal of this complex number.
     */
    public Complex reciprocal() {
        double scale = re * re + im * im;
        return new Complex(re / scale, -im / scale);
    }

    /**
     * Returns the complex number representing this complex number divided by the real number
     * <code>other</code>.
     *
     * @param other The real number to divide by.
     * @return A <code>Complex</code> object representing the resulting complex number.
     */
    public Complex divides(double other) {
        return this.scale(1.0 / other);
    }

    /**
     * Returns the complex number representing this complex number divided by <code>other</code>.
     *
     * @param other The other complex number.
     * @return A <code>Complex</code> object representing the resulting complex number.
     */
    public Complex divides(Complex other) {
        // Check if the other complex number is purely real
        if (other.isPurelyReal()) return this.divides(other.re);

        // If not, do standard complex division
        return this.times(other.reciprocal());
    }

    // Static methods

    /**
     * A static method that returns the sum <code>a + b</code> where <code>a</code> and
     * <code>b</code> are complex numbers.
     *
     * @param a The first complex number.
     * @param b The second complex number.
     * @return A <code>Complex</code> object representing the sum <code>a + b</code>.
     */
    public static Complex plus(Complex a, Complex b) {
        double real = a.re + b.re;
        double imag = a.im + b.im;
        return new Complex(real, imag);
    }

    /**
     * A static method that returns the sum <code>a - b</code> where <code>a</code> and
     * <code>b</code> are complex numbers.
     *
     * @param a The first complex number.
     * @param b The second complex number.
     * @return A <code>Complex</code> object representing the sum <code>a - b</code>.
     */
    public static Complex minus(Complex a, Complex b) {
        double real = a.re - b.re;
        double imag = a.im - b.im;
        return new Complex(real, imag);
    }

    /**
     * A static method that returns the product <code>a * b</code> where <code>a</code> and
     * <code>b</code> are complex numbers.
     *
     * @param a The first complex number.
     * @param b The second complex number.
     * @return A <code>Complex</code> object representing the product <code>a * b</code>.
     */
    public static Complex times(Complex a, Complex b) {
        double real = a.re * b.re - a.im * b.im;
        double imag = a.re * b.im + a.im * b.re;
        return new Complex(real, imag);
    }

    /**
     * A static method that returns the value of <code>a / b</code> where <code>a</code> and
     * <code>b</code> are complex numbers.
     *
     * @param a The first complex number.
     * @param b The second complex number.
     * @return A <code>Complex</code> object representing the value of <code>a / b</code>.
     */
    public static Complex divides(Complex a, Complex b) {
        // Compute numerator and denominator separately
        double numeratorReal = a.re * b.re + a.im * b.im;
        double numeratorImag = b.re * a.im - a.re * b.im;

        double denominator = 1 / (b.re * b.re + b.im * b.im);  // Note that denominator is purely real

        // Combine numerator and denominator into the final real and imaginary parts
        double real = numeratorReal * denominator;
        double imag = numeratorImag * denominator;

        // Return the new complex number
        return new Complex(real, imag);
    }

    /**
     * Compute the value of e^<code>z</code> where e is Euler's number and <code>z</code> is the
     * complex number.
     *
     * @param z Complex exponent.
     * @return Value of e^<code>z</code>
     */
    public static Complex exp(Complex z) {
        // Get the modulus of the final answer
        double mod = Math.exp(z.re);

        // Get the 'complex' part of the final answer
        Complex complexPart = new Complex(Math.cos(z.im), Math.sin(z.im));

        // Return the final answer
        return complexPart.scale(mod);
    }

    // Misc Methods

    /**
     * Rounds both the real and imaginary part of this complex number nicely to a certain number
     * of decimal places, <code>dp</code>.
     *
     * @param dp Number of decimal places to round to.
     * @return This <code>Complex</code> object.
     */
    public Complex roundNicely(int dp) {
        re = MathUtils.round(re, dp);
        im = MathUtils.round(im, dp);

        return this;
    }
}
