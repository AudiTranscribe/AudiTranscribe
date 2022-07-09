/*
 * FFmpegDownloadData.java
 *
 * Created on 2022-07-08
 * Updated on 2022-07-09
 *
 * Description: Data object that stores the FFmpeg download data.
 */

package site.overwrite.auditranscribe.setup_wizard.helpers.data_encapsulators;

/**
 * Data object that stores the FFmpeg download data.
 */
public class FFmpegDownloadData {
    public String url;
    public String signature;
    public String outputFolder;
    public boolean needSetExecutable;
}
