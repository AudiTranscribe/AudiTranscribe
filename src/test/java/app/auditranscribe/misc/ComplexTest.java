package app.auditranscribe.misc;

import app.auditranscribe.utils.MathUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComplexTest {
    // Assertion methods
    @Test
    void isPurelyReal() {
        assertTrue((new Complex(1, 0)).isPurelyReal());
        assertTrue((new Complex(1.23, 0)).isPurelyReal());

        assertFalse((new Complex(0, 1)).isPurelyReal());
        assertFalse((new Complex(0, 1.23)).isPurelyReal());

        assertFalse((new Complex(1, 1)).isPurelyReal());
        assertFalse((new Complex(1.23, 1.23)).isPurelyReal());

        assertTrue((new Complex(0, 0)).isPurelyReal());
    }

    @Test
    void isPurelyImaginary() {
        assertFalse((new Complex(1, 0)).isPurelyImaginary());
        assertFalse((new Complex(1.23, 0)).isPurelyImaginary());

        assertTrue((new Complex(0, 1)).isPurelyImaginary());
        assertTrue((new Complex(0, 1.23)).isPurelyImaginary());

        assertFalse((new Complex(1, 1)).isPurelyImaginary());
        assertFalse((new Complex(1.23, 1.23)).isPurelyImaginary());

        assertTrue((new Complex(0, 0)).isPurelyImaginary());
    }

    // Polar/Exponential/Trigonometric form methods
    @Test
    void abs() {
        assertEquals(0, (new Complex(0, 0)).abs());

        assertEquals(1, (new Complex(1, 0)).abs());
        assertEquals(2.34, (new Complex(2.34, 0)).abs());

        assertEquals(1, (new Complex(0, 1)).abs());
        assertEquals(2.34, (new Complex(0, 2.34)).abs());

        assertEquals(5, (new Complex(3, 4)).abs());
        assertEquals(5, (new Complex(-3, 4)).abs());
        assertEquals(5, (new Complex(3, -4)).abs());
        assertEquals(5, (new Complex(-3, -4)).abs());

        Assertions.assertEquals(5.8105, MathUtils.round((new Complex(1.234, 5.678)).abs(), 4));
        assertEquals(5.8105, MathUtils.round((new Complex(-1.234, 5.678)).abs(), 4));
        assertEquals(5.8105, MathUtils.round((new Complex(1.234, -5.678)).abs(), 4));
        assertEquals(5.8105, MathUtils.round((new Complex(-1.234, -5.678)).abs(), 4));
    }

    @Test
    void phase() {
        assertEquals(-Math.PI / 2, (new Complex(0, -1)).phase());
        assertEquals(-Math.PI / 2, (new Complex(0, -12.34)).phase());

        assertEquals(0, (new Complex(1, 0)).phase());
        assertEquals(0, (new Complex(12.34, 0)).phase());

        assertEquals(Math.PI / 2, (new Complex(0, 1)).phase());
        assertEquals(Math.PI / 2, (new Complex(0, 12.34)).phase());

        assertEquals(Math.PI, (new Complex(-1, 0)).phase());
        assertEquals(Math.PI, (new Complex(-12.34, 0)).phase());

        assertEquals(0.6435, MathUtils.round((new Complex(4, 3)).phase(), 4));
        assertEquals(-0.6435, MathUtils.round((new Complex(4, -3)).phase(), 4));
        assertEquals(2.4981, MathUtils.round((new Complex(-4, 3)).phase(), 4));
        assertEquals(-2.4981, MathUtils.round((new Complex(-4, -3)).phase(), 4));
    }

    // Operator methods
    @Test
    void conjugate() {
        assertEquals(new Complex(1, 0), (new Complex(1, 0)).conjugate());
        assertEquals(new Complex(12.34, 0), (new Complex(12.34, 0)).conjugate());

        assertEquals(new Complex(0, -1), (new Complex(0, 1)).conjugate());
        assertEquals(new Complex(0, -12.34), (new Complex(0, 12.34)).conjugate());

        assertEquals(new Complex(1, -2), (new Complex(1, 2)).conjugate());
        assertEquals(new Complex(12.34, -56.78), (new Complex(12.34, 56.78)).conjugate());

        assertEquals(new Complex(1, 2), (new Complex(1, -2)).conjugate());
        assertEquals(new Complex(12.34, 56.78), (new Complex(12.34, -56.78)).conjugate());

        assertEquals(new Complex(-1, -2), (new Complex(-1, 2)).conjugate());
        assertEquals(new Complex(-12.34, -56.78), (new Complex(-12.34, 56.78)).conjugate());

        assertEquals(new Complex(-1, 2), (new Complex(-1, -2)).conjugate());
        assertEquals(new Complex(-12.34, 56.78), (new Complex(-12.34, -56.78)).conjugate());
    }

    @Test
    void plus() {
        assertEquals(
                new Complex(2.23, 6.56),
                (new Complex(1, 2)).plus(new Complex(1.23, 4.56)).round(3)
        );
        assertEquals(
                new Complex(2.23, 6.56),
                (new Complex(1.23, 4.56)).plus(new Complex(1, 2)).round(3)
        );
    }

    @Test
    void minus() {
        assertEquals(
                new Complex(-0.23, -2.56),
                (new Complex(1, 2)).minus(new Complex(1.23, 4.56)).round(3)
        );
        assertEquals(
                new Complex(0.23, 2.56),
                (new Complex(1.23, 4.56)).minus(new Complex(1, 2)).round(3)
        );
    }

    @Test
    void times() {
        assertEquals(
                new Complex(2.46, 3.69),
                (new Complex(2, 3)).times(1.23).round(3)
        );
        assertEquals(
                new Complex(-2.46, -3.69),
                (new Complex(2, 3)).times(-1.23).round(3)
        );

        assertEquals(
                new Complex(2.46, 3.69),
                (new Complex(2, 3)).times(new Complex(1.23, 0)).round(3)
        );

        assertEquals(
                new Complex(-5, 10),
                (new Complex(1, 2)).times(new Complex(3, 4))
        );
        assertEquals(
                new Complex(11, 2),
                (new Complex(1, 2)).times(new Complex(3, -4))
        );
    }

    @Test
    void reciprocal() {
        assertEquals(
                new Complex(1, 0),
                (new Complex(1, 0)).reciprocal()
        );
        assertEquals(
                new Complex(0, -1),
                (new Complex(0, 1)).reciprocal()
        );

        assertEquals(
                new Complex(0.5, -0.5),
                (new Complex(1, 1)).reciprocal().round(3)
        );
        assertEquals(
                new Complex(0.12, -0.16),
                (new Complex(3, 4)).reciprocal().round(3)
        );

        assertEquals(
                new Complex(0.5, 0.5),
                (new Complex(1, -1)).reciprocal()
        );
        assertEquals(
                new Complex(0.12, 0.16),
                (new Complex(3, -4)).reciprocal()
        );
    }

    @Test
    void divides() {
        assertEquals(
                new Complex(1, 1),
                (new Complex(5, 5)).divides(new Complex(5, 0)).round(3)
        );
        assertEquals(
                new Complex(0.44, 0.08),
                (new Complex(1, 2)).divides(new Complex(3, 4)).round(3)
        );
    }

    // Miscellaneous methods
    @Test
    void exp() {
        assertEquals(
                new Complex(1, 0),
                Complex.exp(new Complex(0, 0)).round(3)
        );
        assertEquals(
                new Complex(10, 0),
                Complex.exp(new Complex(Math.log(10), 0)).round(3)
        );
        assertEquals(
                new Complex(1.23, 0),
                Complex.exp(new Complex(Math.log(1.23), 0)).round(3)
        );

        assertEquals(
                new Complex(0, 1),
                Complex.exp(new Complex(0, Math.PI / 2)).round(3)
        );
        assertEquals(
                new Complex(-1, 0),
                Complex.exp(new Complex(0, Math.PI)).round(3)
        );
        assertEquals(
                new Complex(0, -1),
                Complex.exp(new Complex(0, 3 * Math.PI / 2)).round(3)
        );
        assertEquals(
                new Complex(1, 0),
                Complex.exp(new Complex(0, 2 * Math.PI)).round(3)
        );

        assertEquals(
                new Complex(0, 2),
                Complex.exp(new Complex(Math.log(2), Math.PI / 2)).round(3)
        );
        assertEquals(
                new Complex(0, -2),
                Complex.exp(new Complex(Math.log(2), -Math.PI / 2)).round(3)
        );
        assertEquals(
                new Complex(-2, 0),
                Complex.exp(new Complex(Math.log(2), Math.PI)).round(3)
        );
    }

    @Test
    void fromMagnitudeAndPhase() {
        assertEquals(
                new Complex(2),
                Complex.fromMagnitudeAndPhase(2, 0).round(7)
        );
        assertEquals(
                new Complex(0, 2),
                Complex.fromMagnitudeAndPhase(2, Math.PI / 2).round(7)
        );
        assertEquals(
                new Complex(-2),
                Complex.fromMagnitudeAndPhase(2, Math.PI).round(7)
        );
        assertEquals(
                new Complex(0, -2),
                Complex.fromMagnitudeAndPhase(2, 3 * Math.PI / 2).round(7)
        );
    }

    // Overridden methods
    @Test
    void testEquality() {
        // Define five complex number objects to test equality
        Complex complex1 = new Complex(1.2, 3.4);
        Complex complex2 = new Complex(1.2, 3.4);
        Complex complex3 = new Complex(5.67, 8.9);
        Complex complex4 = new Complex(1.2, 8.9);
        Complex complex5 = new Complex(5.67, 3.4);

        // Define other objects to test comparison
        String otherTypedVar = "hello";

        // Test equality comparisons
        assertEquals(complex1, complex1);

        assertNotEquals(complex2, null);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(complex3, otherTypedVar);  // Not redundant to test the equality method

        assertEquals(complex1, complex2);
        assertNotEquals(complex2, complex3);
        assertNotEquals(complex1, complex4);
        assertNotEquals(complex2, complex5);
    }

    @Test
    void testHashCode() {
        // Define complex number objects to test equality
        Complex complex1 = new Complex(1, 2.3);
        Complex complex2 = new Complex(4.56, 7.89);

        assertEquals(-462158911, complex1.hashCode());
        assertEquals(-36961305, complex2.hashCode());
    }

    @Test
    void testToString() {
        assertEquals("12.0", (new Complex(12, 0)).toString());
        assertEquals("10.3", (new Complex(10.3, 0)).toString());

        assertEquals("12.0j", (new Complex(0, 12)).toString());
        assertEquals("10.3j", (new Complex(0, 10.3)).toString());

        assertEquals("12.0 + 34.0j", (new Complex(12, 34)).toString());
        assertEquals("1.23 + 4.56j", (new Complex(1.23, 4.56)).toString());

        assertEquals("12.0 - 34.0j", (new Complex(12, -34)).toString());
        assertEquals("1.23 - 4.56j", (new Complex(1.23, -4.56)).toString());
    }
}