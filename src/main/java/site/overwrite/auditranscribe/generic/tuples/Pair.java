/*
 * Pair.java
 * Description: An implementation of a 2-tuple (pair).
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

/**
 * An implementation of a 2-tuple (pair).
 *
 * @param value0 The first value.
 * @param value1 The second value.
 * @param <A>    The first type.
 * @param <B>    The second type.
 */
public record Pair<A, B>(A value0, B value1) {
}
