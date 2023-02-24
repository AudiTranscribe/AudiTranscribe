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
import app.auditranscribe.fxml.views.main.SceneSwitcher;

/**
 * Abstract controller for views that are involved with the scene switcher.
 */
public abstract class SwitchableViewController extends AbstractViewController {
    // Attributes
    SceneSwitcher.State state;
    SceneSwitcher.Data data = new SceneSwitcher.Data();

    // Getter/setter methods
    public abstract SceneSwitcher.State getSceneSwitchingState();

    public SceneSwitcher.Data getSceneSwitchingData() {
        return data;
    }
}
