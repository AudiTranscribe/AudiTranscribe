/*
 * QuadrupleTest.java
 * Description: Test `Quadruple.java`.
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

package app.auditranscribe.generic.tuples;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuadrupleTest {
    // Define quadruples to test
    Quadruple<Integer, Double, Boolean, String> quadruple1 = new Quadruple<>(1, 2.3, false, "five");
    Quadruple<String, String, Integer, Boolean> quadruple2 = new Quadruple<>("alpha", "beta", 3, true);

    @Test
    void value0() {
        assertEquals(1, quadruple1.value0());
        assertEquals("alpha", quadruple2.value0());
    }

    @Test
    void value1() {
        assertEquals(2.3, quadruple1.value1(), 1e-5);
        assertEquals("beta", quadruple2.value1());
    }

    @Test
    void value2() {
        assertEquals(false, quadruple1.value2());
        assertEquals(3, quadruple2.value2());
    }

    @Test
    void value3() {
        assertEquals("five", quadruple1.value3());
        assertEquals(true, quadruple2.value3());
    }
}