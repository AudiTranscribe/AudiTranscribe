/*
 * SignalHelpers.java
 * Description: Helper methods used in the signal package.
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

package app.auditranscribe.signal;

/**
 * Helper methods used in the signal package.
 */
public final class SignalHelpers {
    private SignalHelpers() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Computes the alpha coefficient.
     *
     * @param binsPerOctave Number of frequency bins per octave.
     * @return The alpha coefficient.
     * @implNote Implementation of this method is from the paper by Glasberg, Brian R., and Brian
     * CJ Moore. "Derivation of auditory filter shapes from notched-noise data". Hearing research
     * 47.1-2 (1990): 103-138.
     */
    public static double computeAlpha(double binsPerOctave) {
        double r = Math.pow(2, 1 / binsPerOctave);
        return (Math.pow(r, 2) - 1) / (Math.pow(r, 2) + 1);
    }
}
