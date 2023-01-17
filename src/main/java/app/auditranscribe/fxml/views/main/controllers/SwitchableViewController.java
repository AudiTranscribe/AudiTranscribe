/*
 * SwitchableViewController.java
 * Description: Abstract controller for views that are involved with the scene switcher.
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

package app.auditranscribe.fxml.views.main.controllers;

import app.auditranscribe.fxml.views.AbstractViewController;
import app.auditranscribe.fxml.views.main.scene_switching.SceneSwitchingData;
import app.auditranscribe.fxml.views.main.scene_switching.SceneSwitchingState;

/**
 * Abstract controller for views that are involved with the scene switcher.
 */
public abstract class SwitchableViewController extends AbstractViewController {
    // Attributes
    SceneSwitchingState sceneSwitchingState;
    SceneSwitchingData sceneSwitchingData = new SceneSwitchingData();

    // Getter/setter methods
    public abstract SceneSwitchingState getSceneSwitchingState();

    public SceneSwitchingData getSceneSwitchingData() {
        return sceneSwitchingData;
    }
}
