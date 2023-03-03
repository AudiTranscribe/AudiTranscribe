package app.auditranscribe.fxml.plotting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlottingHelpersTest {
    @Test
    void freqToHeight() {
        assertEquals(
                334.914,
                PlottingHelpers.freqToHeight(123, 10, 20000, 500),
                1e-3
        );
        assertEquals(
                153.6,
                PlottingHelpers.freqToHeight(4096, 32, 32768, 512),
                1e-3
        );
        assertEquals(
                59.39195,
                PlottingHelpers.freqToHeight(12345, 1, 22050, 1024),
                1e-3
        );
    }

    @Test
    void heightToFreq() {
        assertEquals(
                123,
                PlottingHelpers.heightToFreq(334.914, 10, 20000, 500),
                1e-3
        );
        assertEquals(
                4096,
                PlottingHelpers.heightToFreq(153.6, 32, 32768, 512),
                1e-3
        );
        assertEquals(
                12345,
                PlottingHelpers.heightToFreq(59.39195, 1, 22050, 1024),
                1e-3
        );
    }

    @Test
    void noteNumToHeight() {
        assertEquals(
                48.739,
                PlottingHelpers.noteNumToHeight(61, 0, 119, 100),
                1e-3
        );
        assertEquals(
                400,
                PlottingHelpers.noteNumToHeight(2, 0, 10, 500),
                1e-3
        );
        assertEquals(
                208.333,
                PlottingHelpers.noteNumToHeight(100, 5, 125, 1000),
                1e-3
        );
    }

    @Test
    void heightToNoteNum() {
        assertEquals(
                77,
                PlottingHelpers.heightToNoteNum(123, 0, 200, 200),
                1e-5
        );
        assertEquals(
                200,
                PlottingHelpers.heightToNoteNum(0, 0, 200, 200),
                1e-5
        );
        assertEquals(
                0,
                PlottingHelpers.heightToNoteNum(200, 0, 200, 200),
                1e-5
        );
    }

    @Test
    void getHeightDifference() {
        assertEquals(
                1,
                PlottingHelpers.getHeightDifference(120, 0, 120),
                1e-5
        );
        assertEquals(
                5. / 3,
                PlottingHelpers.getHeightDifference(200, 0, 120),
                1e-5
        );

        assertEquals(
                1,
                PlottingHelpers.getHeightDifference(120, 100, 220),
                1e-5
        );
        assertEquals(
                0.60060,
                PlottingHelpers.getHeightDifference(200, 123, 456),
                1e-5
        );
    }
}