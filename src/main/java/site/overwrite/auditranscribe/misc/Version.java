/*
 * Version.java
 * Description: Class that handles semantic version parsing and comparison.
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

package site.overwrite.auditranscribe.misc;

/**
 * Class that handles semantic version parsing and comparison.
 */
public class Version implements Comparable<Version> {
    // Attributes
    public final String version;
    public final int[] numbers;

    /**
     * Initializes a new <code>Version</code> object.
     *
     * @param version Version string to parse.
     */
    public Version(String version) {
        this.version = version;

        String[] split = version.split("-")[0].split("\\.");
        numbers = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            numbers[i] = Integer.parseInt(split[i]);
        }
    }

    @Override
    public int compareTo(Version another) {
        // Get the longer of the two versions
        int maxLength = Math.max(numbers.length, another.numbers.length);

        // Compare version parts pairwise
        for (int i = 0; i < maxLength; i++) {
            int left = i < numbers.length ? numbers[i] : 0;
            int right = i < another.numbers.length ? another.numbers[i] : 0;

            if (left != right) {
                return left < right ? -1 : 1;
            }
        }
        return 0;
    }
}
