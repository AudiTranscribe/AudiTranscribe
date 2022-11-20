/*
 * FilterTest.java
 * Description: Test the audio filters.
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

package app.auditranscribe.audio;

import app.auditranscribe.audio.exceptions.FilterNotFoundException;
import app.auditranscribe.audio.filters.AbstractFilter;
import app.auditranscribe.audio.filters.KaiserBest;
import app.auditranscribe.audio.filters.KaiserFast;
import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import site.overwrite.auditranscribe.audio.filters.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FilterTest {
    @Test
    @Order(1)
    void abstractFilterTest() {
        // Create invalid classes for testing
        class MyInvalidTestFilter1 extends AbstractFilter {
            public MyInvalidTestFilter1() throws IOException {
                defineAttributes("nonexistent-json-file.json");
            }
        }

        class MyInvalidTestFilter2 extends AbstractFilter {
            public MyInvalidTestFilter2() throws IOException {
                defineAttributes(IOMethods.joinPaths("testing-files", "text", "README.txt"));
            }
        }

        // Assert that this will cause an exception
        assertThrows(IOException.class, MyInvalidTestFilter1::new);
        assertThrows(IOException.class, MyInvalidTestFilter2::new);
    }

    @Test
    @Order(2)
    void kaiserBestNormalTest() {
        // Create a new kaiser best filter
        KaiserBest kaiserBest = new KaiserBest();

        // Check attributes
        assertEquals(512, kaiserBest.getPrecision());
        assertEquals(0.9475937167399596, kaiserBest.getRolloff(), 1e-6);

        double[] halfWindow = kaiserBest.getHalfWindow();
        assertEquals(0.9475937167399596, halfWindow[0], 1e-6);
        assertEquals(0.8687706122525141, halfWindow[123], 1e-6);
        assertEquals(0.16804652520040697, halfWindow[456], 1e-6);
        assertEquals(-0.20410492924006962, halfWindow[789], 1e-6);
        assertEquals(-0.006147657848870016, halfWindow[8192], 1e-6);
        assertEquals(0.002128069210833461, halfWindow[12345], 1e-6);
        assertEquals(0, halfWindow[32768], 1e-6);
    }

    @Test
    @Order(2)
    void kaiserFastTest() {
        // Create a new kaiser best filter
        KaiserFast kaiserFast = new KaiserFast();

        // Check attributes
        assertEquals(512, kaiserFast.getPrecision());
        assertEquals(0.85, kaiserFast.getRolloff(), 1e-6);

        double[] halfWindow = kaiserFast.getHalfWindow();
        assertEquals(0.85, halfWindow[0], 1e-6);
        assertEquals(0.7921688478295915, halfWindow[123], 1e-6);
        assertEquals(0.2440165355807558, halfWindow[456], 1e-6);
        assertEquals(-0.1645266447169643, halfWindow[789], 1e-6);
        assertEquals(-2.628865215979547e-05, halfWindow[8192], 1e-6);
    }

    @Test
    @Order(3)
    void kaiserBestExceptionTest() throws IOException {
        // Test exceptions
        IOMethods.moveFile(
                IOMethods.joinPaths(
                        IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                        "filter-data", "kaiser-best.json"
                ),
                IOMethods.joinPaths(
                        IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                        "kaiser-best.json"
                )
        );
        try {
            assertThrowsExactly(FilterNotFoundException.class, KaiserBest::new);
        } finally {
            IOMethods.moveFile(
                    IOMethods.joinPaths(
                            IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                            "kaiser-best.json"
                    ),
                    IOMethods.joinPaths(
                            IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                            "filter-data", "kaiser-best.json"
                    )
            );
        }
    }

    @Test
    @Order(3)
    void kaiserFastExceptionTest() throws IOException {
        // Test exceptions
        IOMethods.moveFile(
                IOMethods.joinPaths(
                        IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                        "filter-data", "kaiser-fast.json"
                ),
                IOMethods.joinPaths(
                        IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                        "kaiser-fast.json"
                )
        );
        try {
            assertThrowsExactly(FilterNotFoundException.class, KaiserFast::new);
        } finally {
            IOMethods.moveFile(
                    IOMethods.joinPaths(
                            IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                            "kaiser-fast.json"
                    ),
                    IOMethods.joinPaths(
                            IOConstants.TARGET_FOLDER_ABSOLUTE_PATH, IOConstants.RESOURCES_FOLDER_PATH,
                            "filter-data", "kaiser-fast.json"
                    )
            );
        }
    }
}
