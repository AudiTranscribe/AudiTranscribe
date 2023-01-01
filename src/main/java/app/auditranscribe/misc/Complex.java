/*
 * Complex.java
 * Description: Complex number class.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package app.auditranscribe.misc;

import app.auditranscribe.utils.MathUtils;

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
    public double re;  // Real part of the complex number
    public double im;  // Imaginary part of the complex number

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
     * @param re The real part of the complex number.
     */
    public Complex(double re) {
        this.re = re;
        im = 0;
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
        double real = this.re + other.re;
        double imag = this.im + other.im;
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
        double real = this.re - other.re;
        double imag = this.im - other.im;
        return new Complex(real, imag);
    }

    /**
     * Returns the scaled version of this complex number when multiplied by the <b>real number</b>
     * <code>other</code>.
     *
     * @param other The real number to multiply by.
     * @return A <code>Complex</code> object representing the resulting complex number.
     */
    public Complex times(double other) {
        return new Complex(other * re, other * im);
    }

    /**
     * Returns the product of this complex number with another complex number.
     *
     * @param other The other complex number.
     * @return A <code>Complex</code> object representing the resulting complex number.
     */
    public Complex times(Complex other) {
        // Check if the other complex number is purely real
        if (other.isPurelyReal()) return new Complex(this.re * other.re, this.im * other.re);

        // Otherwise, perform complex number multiplication
        double real = this.re * other.re - this.im * other.im;
        double imag = this.re * other.im + this.im * other.re;
        return new Complex(real, imag);
    }

    /**
     * Returns the reciprocal of this complex number.
     *
     * @return A <code>Complex</code> object representing the reciprocal of this complex number.
     */
    public Complex reciprocal() {
        double denominator = re * re + im * im;
        double scale = 1 / denominator;  // This is to reduce computation later with multiplication
        return new Complex(re * scale, -im * scale);
    }

    /**
     * Returns the complex number representing this complex number divided by the real number
     * <code>other</code>.
     *
     * @param other The real number to divide by.
     * @return A <code>Complex</code> object representing the resulting complex number.
     */
    public Complex divides(double other) {
        return this.times(1. / other);
    }

    /**
     * Returns the complex number representing this complex number divided by <code>other</code>.
     *
     * @param other The other complex number.
     * @return A <code>Complex</code> object representing the resulting complex number.
     */
    public Complex divides(Complex other) {
        // Check if the other complex number is purely real
        if (other.isPurelyReal()) return this.times(1 / other.re);

        // If not, do standard complex division
        return this.times(other.reciprocal());
    }

    // Miscellaneous Methods


    /**
     * Compute the value of e<sup><code>z</code></sup> where e is Euler's number and <code>z</code> is the
     * complex number.
     *
     * @param z Complex exponent.
     * @return Value of e<sup><code>z</code></sup>
     */
    public static Complex exp(Complex z) {
        /*
         * Recall that if z = u + vi, where u and v are real numbers, then:
         *      e^z = e^(u + vi) = e^u * e^(vi).
         * The "e^u" part can be found trivially; "e^(vi)" can be found by using Euler's identity
         *      e^(vi) = cos(v) + i sin(v).
         * Therefore we have
         *      e^z = e^(u + vi) = e^u * (cos(v) + i sin(v)).
         * with "e^u" being the modulus and "cos(v) + i sin(v)" being the trigonometric part.
         */

        // Get the modulus of the final answer
        double modulus = Math.exp(z.re);

        // Get the trigonometric part of the final answer
        Complex trigPart = new Complex(Math.cos(z.im), Math.sin(z.im));

        return trigPart.times(modulus);
    }

    /**
     * Rounds both the real and imaginary part of this complex number to a certain number
     * of decimal places, <code>dp</code>.
     *
     * @param dp Number of decimal places to round both the real and imaginary parts to.
     * @return This <code>Complex</code> object, after rounding.
     */
    // Todo: rename to `round`
    public Complex roundNicely(int dp) {
        re = MathUtils.round(re, dp);
        im = MathUtils.round(im, dp);

        return this;
    }

    // Overridden methods

    /**
     * Generates a string representation of the complex number.<br>
     * Note that we use <code>j</code> for the imaginary unit to follow Python's convention for the
     * imaginary unit.
     *
     * @return String representation of the complex number.
     */
    @Override
    public String toString() {
        if (im == 0) return re + "";  // Concat "" to the end to make it a string
        if (re == 0) return im + "j";
        if (im < 0) return re + " - " + (-im) + "j";
        return re + " + " + im + "j";
    }

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
}
