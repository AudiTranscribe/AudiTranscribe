/*
 * IdentityOperator.java
 * Description: An operator that just returns whatever is passed into the input buffer.
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
 * An operator that just returns whatever is passed into the input buffer.
 */
@ExcludeFromGeneratedCoverageReport
public class IdentityOperator extends TimeStretchOperator {
    /**
     * Initializes a new <code>IdentityOperator</code>.
     */
    public IdentityOperator() {
        super(1);
    }

    // Public methods
    @Override
    public double[] process() throws InterruptedException {
        double[] output = new double[inputBuffer.size()];
        for (int i = 0; i < output.length; i++) {
            output[i] = inputBuffer.take();
        }
        return output;
    }
}
