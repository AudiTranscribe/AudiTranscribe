/*
 * PairTest.java
 * Description: Test `Pair.java`.
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

class PairTest {
    // Define pairs to test
    Pair<Integer, String> pair1 = new Pair<>(1, "two");
    Pair<Integer, String> pair2 = new Pair<>(2, "three");
    Pair<Double, Double> pair3 = new Pair<>(1d, 2d);

    @Test
    void value0() {
        assertEquals(1, pair1.value0());
        assertEquals(2, pair2.value0());
        assertEquals(1, pair3.value0(), 1e-5);
    }

    @Test
    void value1() {
        assertEquals("two", pair1.value1());
        assertEquals("three", pair2.value1());
        assertEquals(2, pair3.value1(), 1e-5);
    }
}