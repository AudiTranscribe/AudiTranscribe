/*
 * TimeStretchOperator.java
 * Description: An abstract operator that handles time stretching operations.
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

package app.auditranscribe.audio.operators;

import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;

/**
 * An abstract operator that handles time stretching operations.
 */
@ExcludeFromGeneratedCoverageReport
public abstract class TimeStretchOperator extends Operator {
    // Attributes
    protected double stretchFactor;

    /**
     * Initializes a new <code>TimeStretchOperator</code>.
     *
     * @param stretchFactor Initial time stretch factor.
     */
    public TimeStretchOperator(double stretchFactor) {
        this.stretchFactor = stretchFactor;
    }

    // Getter/setter methods
    public double getStretchFactor() {
        return stretchFactor;
    }

    public void setStretchFactor(double stretchFactor) {
        this.stretchFactor = stretchFactor;
    }
}
