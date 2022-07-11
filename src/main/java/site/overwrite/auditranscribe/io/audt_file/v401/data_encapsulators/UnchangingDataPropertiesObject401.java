/*
 * UnchangingDataPropertiesObject401.java
 *
 * Created on 2022-07-11
 * Updated on 2022-07-11
 *
 * Description: Data object that stores the unchanging data's properties.
 */

package site.overwrite.auditranscribe.io.audt_file.v401.data_encapsulators;

import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.UnchangingDataPropertiesObject;

/**
 * Data object that stores the unchanging data's properties.
 */
public class UnchangingDataPropertiesObject401 extends UnchangingDataPropertiesObject {
    /**
     * Initialization method for the unchanging data properties object.
     *
     * @param numSkippableBytes The number of skippable bytes.
     */
    public UnchangingDataPropertiesObject401(int numSkippableBytes) {
        this.numSkippableBytes = numSkippableBytes;
    }
}
