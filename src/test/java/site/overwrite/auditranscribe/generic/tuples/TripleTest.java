/*
 * TripleTest.java
 * Description: Test `Triple.java`.
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

package site.overwrite.auditranscribe.generic.tuples;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.generic.tuples.Triple;

import static org.junit.jupiter.api.Assertions.*;

class TripleTest {
    // Define triples to test
    Triple<Integer, String, Double> triple1 = new Triple<>(1, "two", 3.4);
    Triple<Boolean, Boolean, Boolean> triple2 = new Triple<>(true, false, false);

    @Test
    void value0() {
        assertEquals(1, triple1.value0());
        assertEquals(true, triple2.value0());
    }

    @Test
    void value1() {
        assertEquals("two", triple1.value1());
        assertEquals(false, triple2.value1());
    }

    @Test
    void value2() {
        assertEquals(3.4, triple1.value2(), 1e-5);
        assertEquals(false, triple2.value2());
    }
}