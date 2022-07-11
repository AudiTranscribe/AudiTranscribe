/*
 * UnchangingDataPropertiesObject501.java
 *
 * Created on 2022-07-11
 * Updated on 2022-07-11
 *
 * Description: Data object that stores the unchanging data's properties.
 */

package site.overwrite.auditranscribe.io.audt_file.data_encapsulators.v501;

import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.UnchangingDataPropertiesObject;

/**
 * Data object that stores the unchanging data's properties.
 */
public class UnchangingDataPropertiesObject501 extends UnchangingDataPropertiesObject {
    /**
     * Initialization method for the unchanging data properties object.
     *
     * @param numSkippableBytes The number of skippable bytes.
     */
    public UnchangingDataPropertiesObject501(int numSkippableBytes) {
        this.numSkippableBytes = numSkippableBytes;
    }
}
