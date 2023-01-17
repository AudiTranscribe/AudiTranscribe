/*
 * SceneSwitchingData.java
 * Description: Data that is used for the scene switcher.
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

package app.auditranscribe.fxml.views.main.scene_switching;

import java.io.File;

/**
 * Data that is used for the scene switcher.
 */
public class SceneSwitchingData {
    public String projectName;
    public File file;  // Can either be an audio file or an AUDT file

    public boolean isProjectSetup = false;  // False by default

    public boolean estimateBPM;
    public double manualBPM;

    public boolean estimateMusicKey;
    public String musicKeyString;
}
