/*
 * QuintupleTest.java
 * Description: Test `Quintuple.java`.
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

package site.overwrite.auditranscribe.misc.tuples;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuintupleTest {
    // Define quintuples to test
    Quintuple<Integer, Integer, Integer, Integer, Integer> quintuple1 = new Quintuple<>(1, 2, 3, 4, 5);
    Quintuple<String, Double, String, Boolean, Double> quintuple2 = new Quintuple<>("a", 2.3, "d", false, 6.7);

    @Test
    void value0() {
        assertEquals(1, quintuple1.value0());
        assertEquals("a", quintuple2.value0());
    }

    @Test
    void value1() {
        assertEquals(2, quintuple1.value1());
        assertEquals(2.3, quintuple2.value1(), 1e-5);
    }

    @Test
    void value2() {
        assertEquals(3, quintuple1.value2());
        assertEquals("d", quintuple2.value2());
    }

    @Test
    void value3() {
        assertEquals(4, quintuple1.value3());
        assertEquals(false, quintuple2.value3());
    }

    @Test
    void value4() {
        assertEquals(5, quintuple1.value4());
        assertEquals(6.7, quintuple2.value4(), 1e-5);
    }
}