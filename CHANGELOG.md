# AudiTranscribe Changelog

## [0.10.0](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.9.3...v0.10.0) (2023-01-20)

This release comes with some changes and a minor feature addition.

### Additions

- Added a debug mode for developers.

### Changes

- Deprecate the use of API for setup wizard (see below).

### Removals

- Removed API server dependence; now queries GitHub for the latest versions.
- Removed unused packages and imports.
- Remove unneeded schedulers in the transcription view.
    - This should help reduce lag in that view.

### Bugfixes

- Fix download data attributes.
- Fix weird sizing of preferences view buttons.
- Fix run configurations.
- Fix imports in some tests.
- Fix incorrect resources folder path.

### Technical Changes

- Renamed the main package from `site.overwrite.auditranscribe` to `app.auditranscribe`.
- Use an abstract setup wizard view class to reduce code complexity.
- Merged the `main_views` and `setup_wizard` views into one package.
    - This should help reduce clutter and make things easier to find.
- Renamed `IconHelpers` to `IconHelper`.
- Update the "stale" CI action.

## [0.9.3](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.9.2...v0.9.3) (2022-11-16)

This release is a hotfix for a critical bug.

### Bugfixes

- Fix incorrect access modifier for the icons' data.

## [0.9.2](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.9.1...v0.9.2) (2022-11-08)

This release fixes an issue with the installer.

### Bugfixes

- Fix issue with downloading resources.

## [0.9.1](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.9.0...v0.9.1) (2022-11-06)

This release quickly amends a problem with the API webserver.

### Bugfixes

- Update API webserver URL.

## [0.9.0](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.8.3...v0.9.0) (2022-11-05)

Although this release does not add any major features, it adds Linux support, enhances quality of life, improves
performance, and fixes several bugs.

### Additions

- Add button to visit app data folder.
- Add documentation for AudiTranscribe.
- Add Linux support to AudiTranscribe.
- Add more time signature options.
- Add undo/redo for note rectangles.
- Be more generous with exceptions.
- Increase time signature options.

### Bugfixes

- Cancelling saving project shows progress bar,
  closes [#28](https://github.com/AudiTranscribe/AudiTranscribe/issues/28).
- Fix an imprecision in note label alignment, closes [#22](https://github.com/AudiTranscribe/AudiTranscribe/issues/22).
- Fix issue where preferences view could be hidden.
- Fix issue with current octave rectangle leaving screen.
- Fix weird resizing of buttons.
- Fix weird resizing of hyperlinks upon hover.
- Incorrect positioning of transcription window.
- A weird window overflow was fixed.

### Performance Improvements

- Change division to multiplication in several contexts.
- Change seconds per beat calculation.
- Clean up code in some files.
- Improve performance of `ArrayUtils`, `BPMEstimationHelpers`, `Complex`, `FFT`, `FrequencyBins`,
  `PlottingStuffHandler`, and `UnitConversionUtils`.
- Update use of ceiling/floor division.

## [0.8.3](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.8.2...v0.8.3) (2022-10-23)

This release changes the default font and fixes an annoying bug.

### Changes

- Changed default font to Lato.

### Bugfixes

- Fix an incorrectly sized button on the project setup view.

## [0.8.2](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.8.1...v0.8.2) (2022-10-22)

This release comes with a few minor changes and bugfixes.

### Additions

- Added download progress to some setup views that downloads files.

### Removals

- Database updating code for Version 0.7.x was removed.

### Changes

- Changing the note delay value is now prohibited if audio is playing on the "fix note delay" setup view.
- Changed the volumes of the audio that plays on the "fix note delay" setup view.

### Bugfixes

- Fixed a minor annoyance on the "fix note delay" setup view where the playhead line was slightly misaligned.
- Fixed an issue on the "fix note delay" setup view where the playhead line did not go back to the start when the audio
  was stopped.

### Technical Changes

- The size of the audio resource WAV file has been reduced.
- The spectrogram for the "fix note delay" setup view was changed to better fit the pane that it is in.
- Enum values for `AudioProcessingMode` were renamed to be more consistent.

## [0.8.1](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.8.0...v0.8.1) (2022-10-17)

This release fixes a few critical issues and introduces a feature that was missed in the previous release.

### Additions

- Added customization of the note quantization amount to the preferences view.

### Changes

- Changed some headings on the preferences view.
- Fixed an annoying typographical inconsistency on the preferences view.

### Bugfixes

- Fixed a critical issue where the application crashes immediately on start.
    - This was due to the inability of the application to locate the icons' JSON file locally.
- Fixed an issue where the note quantization failed to properly change the width of the note rectangles.
- Fixed an issue where note quantization was still permitted even though the audio was playing.

### Technical Changes

- GitHub actions were updated.
- Transcription scene shutdown protocol was changed.

## [0.8.0](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.7.1...v0.8.0) (2022-10-16)

This update comes with some new features and several improvements to the operation of AudiTranscribe.

### New Features

- **More Playback Features**: Added audio slowdown functionality. You can now slow down the audio to transcribe the
  audio piece more clearly.
- **Note Quantization**: Added a function that allows the notes placed to be quantized. That is, you can now click a
  button to nicely organise placed notes to match specific beat multiples.
- **Accessibility Features**: We've added some accessibility features to AudiTranscribe:
    - Added a high-contrast mode to make the UI elements stand out more.
    - Changed the icon set to be more friendly and easier to understand (see next feature).
- **New Icon Set**: Icons have been changed to use [Clarity Design](https://clarity.design/icons) icons instead
  of [Ionicons](https://ionic.io/ionicons) icons. This is to make the intended purpose of the buttons easier to
  understand.
- **Make Algorithms More Efficient**: Algorithms that are used to process the spectrograms have been made to be more
  efficient. The spectrogram generation process should now be faster.

### Changed

- Added a button to the preferences view to delete old logs.
- Added tooltips to buttons on the transcription page to make their purpose clearer.
- Changed note placement protocol. The note placement and editing process should now be smoother than before.
- AudiTranscribe files created in Version 0.4.1 or earlier are no longer supported.
    - Specifically, any AudiTranscribe file with a version that is `401` or earlier is no longer supported.

### Bugfixes

- Fixed a weird issue where note rectangles' collision detection is too strict and results in rectangles being too far
  away from each other when moved.
- Fixed issues with note rectangles where resizing stopped awkwardly.
- Fixed an issue which made seeking to the end produce a nasty sound.
- Fixed some note playback issues.
- Fixed an issue which made seeking to the end *not* stop the audio.

### Technical Changes

- AudiTranscribe dependency versions were updated.
- The AudiTranscribe File Version has been updated from `0x00070001` to `0x00080001` to accommodate the changes.
- Replace PNGs with SVGs for icons. This is to increase the quality of the icons of the buttons and reduce the size of
  the application.
- The `exceptions` module was removed in favour of placing each module's exceptions directly into the module itself.

## [0.7.1](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.7.0...v0.7.1) (2022-09-23)

This release comes with some bugfixes and changes.

### Changes

- Tasks will now be performed in sequence.
    - Previously, tasks were performed via multiple threads. Now we use a single thread to perform all tasks.

### Bugfixes

- Fixed an issue where it is possible to click on a project on the main view whilst the project setup view is active,
  causing a soft lock.
- Fixed an issue where FFmpeg failed to download during setup on Windows.
- Fixed an issue where the "fix note delay" view did not show during the setup process.
- Fixed an issue where the music key estimation would take very long (or even hang the computer).

### Technical Changes

- FFmpeg releases are now downloaded from the API server.
- Audio resource that is needed to fix the note delay will also be downloaded from the API server.

## [0.7.0](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.6.2...v0.7.0) (2022-09-18)

This release adds a feature we had worked on for a long time: music key estimation. The release also adds a project
setup wizard to ease the setting up and customisation of the project.

### Features

- **Project Setup Wizard**: Attempting to create a new project now shows a setup wizard. It allows you to select what
  AudiTranscribe should do when it sets up the project.
- **Music Key Estimation**: The music's key can now be automatically estimated. Toggle the option to extract the music
  key automatically, and the program will do it.
    - We use the *Krumhansl-Schmuckler Key-Finding Algorithm* to estimate the key.
        - Read [this paper](http://davidtemperley.com/wp-content/uploads/2015/11/temperley-mp99.pdf) on how the
          key-finding algorithm works.
        - Implementation of the key-finding algorithm is based
          on [this GitHub repository](https://github.com/jackmcarthur/musical-key-finder).
- **Project Name Customisation**: Projects now have a project name for easier identification on the home page.
    - You can specify on the project setup window.
    - We are working on a process to allow you to edit project names.

### Removed

- The keyboard combinations for *new project* and *open project* were removed. The keyboard combination for saving a
  project remains Ctrl + S or ⌘ + S.

### Changed

- Changed allowed offset range from -15 to 15 seconds to -5 to 5 seconds.
- Maximum audio file length was increased from 4 minutes to 5 minutes.
- Skipping to the start or the end will automatically make you scroll to the start or end, respectively.
- Memory use statistics will now always be shown on the transcription view.
- Updates to the code of the transcription view should have reduced the memory usage during transcription.
- Hyperlink colours have been updated.
    - Light mode's hyperlink colour is now bluer and less green.
    - Dark mode's hyperlink colour is now no longer grey. It is a lovely blue colour now.
- The text of buttons on the initial view of the setup page has been shortened.

### Bugfixes

- ~~*Possible* fix of issue #7 where the playhead and the progress meter at the bottom of the screen are occasionally
  desynced.~~
    - **Update 2022-09-22**: Issue #7 still persists in Version 0.7.0.
- Fixed an issue where saving using the keyboard combination plays a sound.
- Fixed an issue where the current octave rectangle will colour the note labels slightly yellow.
- Fixed an issue where a hyperlink on the "about" page is automatically selected.
- Fixed an issue where the automatic FFmpeg download would fail for Windows.

### Technical Changes

- The AudiTranscribe File Version has been updated from `0x00050002` to `0x00070001` to accommodate the changes.
- With regards to the projects database:
    - The Projects database now has a new `Version` table that specifies the database version. It is currently set
      to `458753` (`0x00070001` in decimal).
    - The `filename` column in the `Projects` table has been renamed to `project_name`.
- The JavaTuples library is no longer a dependency. We now use a custom implementation for `Pair` (
  now `Pair`), `Triplet` (now `Triple`), `Quartet` (now `Quadruple`), and `Quintet` (now `Quintuple`) tuple classes.
- Logging format has changed. Previously, the calling class is placed at the back of the log message. Now the calling
  class is after the severity of the log message.

## [0.6.2](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.6.1...v0.6.2) (2022-09-06)

This release fixes several issues with the previous version.

### Bugfixes

- Fixed incorrect dependency version that caused the previous release to fail to create artefacts.
- Fixed incorrect release URL that makes AudiTranscribe fail to redirect users to the correct release page.

## [0.6.1](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.6.0...v0.6.1) (2022-08-30)

This update comes with a few changes and a few bug fixes.

### Changes

- Made alerts and popups use the currently selected theme.
- Reduced the maximum audio length from 15 minutes to 4 minutes.

### Bugfixes

- Added missing BPM and offset validation to the transcription view.
- Fixed incorrect beat line calculation leading to extra/missing beat lines appearing on the spectrogram.

### Technical Changes

- Changed file signatures.

## [0.6.0](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.5.2...v0.6.0) (2022-08-27)

This release adds automatic update checking.

### Additions

- **Automatic Update Checker**: AudiTranscribe will now check for new updates regularly.
- **Better Setup Wizard**: More setup parameters have been added to the setup wizard (e.g. theme select).

### Bugfixes

- Fixed project sorting on the main view.
    - Apparently, if the dates are too far apart the sorting program fails.
- Fixed an issue where FFmpeg was not downloaded automatically.

### Technical Changes

- An API web server has been created to facilitate the checking of new updates for AudiTranscribe.

## [0.5.2](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.5.1...v0.5.2) (2022-08-07)

This minor update fixes a bug where AudiTranscribe did not show an out-of-memory error to the user. It also adds memory
use statistics to the transcription page to show how much memory remains.

## [0.5.1](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.5.0...v0.5.1) (2022-08-04)

This minor release adds functionality to delete old log files.

## [0.5.0](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.4.2...v0.5.0) (2022-07-29)

This release makes AudiTranscribe slightly easier to use and integrate with other applications. Some bug fixes were also
implemented.

### Features

- **Export To MIDI**: Transcribed song portions can now be exported to MIDI.
- **Colour Scales**: More colour scales for the spectrogram were added in this update.
- **File Version Updating**: A protocol to update old files that AudiTranscribe previously used was created. You can now
  use *specific* old file versions in the current update, and the program will recognise them.
    - Note: only version `401` from Version 0.4.x is currently supported.
- **Automatic FFmpeg Installation**: The setup process for AudiTranscribe will now help automatically download and
  install FFmpeg on the system.
    - FFmpeg download and installation currently only works on macOS and Windows.
    - The FFmpeg binary is placed in the application data directory. This is the same directory where you can find the
      logs folder.
- **Note Playback Delay Offset Setup**: Added a setup procedure to fix any note playback delays encountered during the
  program's use.

### Changes

- **Setup Wizard**: The automatic FFmpeg installation and the note playback delay offset setup will occur in the
  new-and-improved setup wizard.
    - AudiTranscribe will show the setup wizard if the setup process has not begun or has yet to be completed.

### Technical Changes

- A GitHub workflow that allows for the automatic uploading of release assets was added in this update.
- This release also updated the file version naming convention to increase the number of valid file versions per update.

## [0.4.2](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.4.1...v0.4.2) (2022-07-07)

This release adds a few enhancements and fixes some issues. This will also be the first release where installers for the
AudiTranscribe application are included!

### Additions

- **More Workflow Files**: Added more GitHub workflows to run tests.

### Changes

- **Licence Reformatting**: Reformatted the GPLv3 licence a bit. **The licence has not been changed**, just some tidying
  up of the lines.

### Bugfixes

- Fix missing stage icons.
- Fix ugly menu items on Windows.

### Technical Changes

- Several dependencies are now included along with the source code of AudiTranscribe. This is to help deal with problems
  arising from automatic modules obtained from the Maven repository.

## [0.4.1](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.4.0...v0.4.1) (2022-07-04)

This release adds a few enhancements and fixes some bugs.

### Additions

- **Better Continuous Integration**: Added extra tests to increase code coverage and squash bugs.
- **More Program Files**: Added `GUIUtils`, `OSMethods`,  `OSType` etc. to better organise code.

### Changes

- **Reorganized Files**:
    - Renamed `io-testing-directory` to `testing-files`.
    - Moved the testing audio files into the `testing-files` directory.
    - Moved the non-testing audio files into an `Example Audio Files` folder.
    - Renamed `designs` to `Designs`.
- **Utility Classes**: Classes that are not to be instantiated and only contain utility methods (i.e. a Utility class)
  now have the appropriate access modifiers and constructors to mark them as such.
- **Improved Exceptions**: Exceptions now have better names.
- **Reduced Dependencies**: Removed the `commons-lang3` dependency; we rely solely on Javatuples for tuple handling.
- **Removed Unused Methods**: Removed methods that are not useful (and will not be tested).
- **Comments**: Added additional comments and docstrings to make methods easier to understand.

### Bugfixes

- Fix issue for audio files with non-exact durations.
- Fixed a bug where the audio duration and current time calculation were incorrect due to incorrect order of operations.
- Fixed issue where a short audio file could result in beat lines showing outside the spectrogram.
- Fixed issue where a short audio file could result in ellipses showing outside the spectrogram.
- Fixed bug in `ArrayUtils` where the program used an incorrect check ("verify that `size` is positive") instead of the
  correct check ("assert that the length of the data is at least the desired size").
- Fixed bug in `UnitConversionUtils` where `octaveChar` was compared with an empty string instead of `null`.
- Fixed confusing error message in `VQT`.
- Fixed typo in `FFTTest`.
- Fixed issue where path traversal on windows was not working.

### Technical Changes

- Handling of paths will now **always** use the forward slash, even on Windows.

## [0.4.0](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.3.1...v0.4.0) (2022-06-26)

This release adds a few things and fixes several bugs.

### Additions

- **Windows Support**: Support for Windows is finally here!
- **Audio Length Limiting**: The audio length will now be limited to 15 minutes. This is to prevent overtly long audio
  files from hogging all the memory on a PC.
- **Unsaved Work Prompt**: Attempting to exit the transcription view with unsaved work will now prompt saving.
- **Exceptions**: Exceptions that occur will show on the screen.
- **Logging**: We added a logging program to log all the things that AudiTranscribe does. Generated logs can be found in
  the application directory.

### Changes

- **Code Cleanup**: Older code would (hopefully) be more in-line with the most recently written code.
- **Better Logging**: Logging of the things that happen in AudiTranscribe has been improved.
- **Better Settings/Preferences View**: The preferences view now has captions for the options to allow you to understand
  better what they mean.
- **Better Exceptions**: Expanded descriptions of some exceptions for increased clarity of what they mean.
- **Better File Saving**: The file saving protocol was again updated. This time the file saving should take even less
  time as the file saving will only overwrite the data that could change.
- **View Organization**: The main view and transcription view are now handled by a single `SceneSwitcher`.

### Bugfixes

- Memory leak fixes.
- Fix the non-disabling of the note button and note slider.
    - Now will correctly disable the note volume button and slider on playing.
- Fixed bug where notes will still play even though they are supposed to be muted.
- Added missing access modifiers to some classes.
- Fixed bug where the initial BPM that was determined by the program would not be reflected on the spectrogram.
- Fixed bug where there may be a possible imprecision in `xNew` and `yNew` in the `Bilinear` class.
- Fixed bug where the note values for B# and Cb were incorrect for some keys.

## [0.3.1](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.3.0...v0.3.1) (2022-06-20)

This release adds a few enhancements and fixes some bugs.

### Additions

- **FFmpeg Setup Wizard**: Added a setup wizard for FFmpeg. This replaces the old setup process (which used ugly popups)
  with a nicer interface.
- **FFmpeg Path Changing**: Added FFmpeg path changing in the settings window.
- **Better MIDI Error Handling**: Added better MIDI error handling in the case where MIDI is unavailable.

### Changes

- **Removed FFmpeg (Java) Dependency**: Removed
  the [Java FFmpeg CLI Wrapper](https://github.com/bramp/ffmpeg-cli-wrapper) dependency in favour of direct CLI.
- **GitHub Issue Templates**: Updated the `PULL_REQUEST_TEMPLATE.md`.

### Bugfixes

- Fixed a bug where failure to initialize the application folder before application startup results in a crash.

## [0.3.0](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.2.2...v0.3.0) (2022-06-18)

This release adds many things and fixes a few long-standing bugs that were not identified sooner.

### Additions

- **More Music Keys**: Additional keys (e.g. F♯ Major, A♭ Minor) were added, and the notes in the respective scales were
  updated to reflect the keys.
- **BPM Estimation**: The program will now automatically determine the BPM of the audio.
- **Accept Other Audio Files**: AudiTranscribe now permits other audio files other than WAV to be accepted.
- **Incorporate Audio Into `.audt` File**: The audio file that you are transcribing no longer needs to be in the same
  place. You can now (re)move the file, and the `.audt` file will still work.
- **Optimise File Saving**: Prevent re-compression of spectrogram/audio data as these do not change (in fact,
  compression takes the most time for the entire file saving process).
- **Music Note Adding**: Allow the adding of notes onto the spectrogram pane and allow side-by-side playing of the
  transcribed notes.
- **Autosaving**: Automatic autosaving is now implemented.

### Changes

- **Installation Requirements**: AudiTranscribe now requires [FFmpeg](https://ffmpeg.org/) to be installed.
- **File Format**: The file format has been updated. Older `.audt` files will now
  ***no longer be accepted by the program***. However, the new files no longer require the original audio file to be in
  its original place.
- **Reduced Spectrogram Size**: The range of notes that are played by the spectrogram have been reduced from C<sub>
  0</sub> to B<sub>9</sub> to **C<sub>0</sub> to B<sub>8</sub>**.

### Bugfixes

- **Fixed Time Signature**: Incorrect beats per bar determination were corrected.
- **Fixed Weird List Scrollbars**: Scrollbars (specifically the horizontal scrollbar) would appear if there were too
  many projects. This has been corrected.
- **Fix "Stop Button" Bug**: Fix a bug where, after stopping the audio, attempting to pause and then play will bring you
  back to the beginning.
- **Fix Missing Spinner Validation**: Spinners now have validation added.

More features and fixes are in store but are not ready for this release. Here are some of them:

- **Windows Support**: Official support for Windows systems coming soon!
- **Add FFmpeg Path Changing in the Settings Window**: Modification of the FFmpeg path would be permitted in a future
  update.
- **Memory Leak Fixes**: A long-standing issue with AudiTranscribe is the amount of memory it appears to consume, and
  this is partly due to a flaw in how the application is designed. We hope to resolve memory leak issues in a future
  update.

## [0.2.2](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.2.1...v0.2.2) (2022-06-03)

This release fixes two bugs.

### Bugfixes

- Fix an apparent misalignment of the project name on the project list if the project name/path was too short.
- Fixed incorrect linear interpolation implementation ([#4](https://github.com/AudiTranscribe/AudiTranscribe/issues/4)).

## [0.2.1](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.2.0...v0.2.1) (2022-05-29)

This release adds a few enhancements and fixes some bugs.

### Additions

- **Theme Switching**: We've added a *dark mode* theme. You can access it by using the preferences view.
- **Disable Selection Of Wrong Files**: We now prevent the selection of incorrect files. This helps minimise errors in
  selecting files when creating a new project or opening an existing project.

### Changes

- **Looks**: The look of AudiTranscribe has slightly changed in both the *light mode* (default) and the *dark mode* (
  newly added).
    - One specific change is that the volume slider now has colour in it.
- **Disabling of Buttons Before Spectrogram is Ready**: We now disable the buttons of the transcription view before the
  spectrogram is ready. This helps reduce errors in positioning items on the screen.

### Bugfixes

- Showing the main view after closing the main view shows the selected project as
  white ([#3](https://github.com/AudiTranscribe/AudiTranscribe/issues/3)).
- Fixed apparent misalignment of the spinners and the choice boxes.
- Fixed volume slider updating before the spectrogram has fully loaded.
- Fixed a bug where, after making a new project or opening an existing project *from an existing project*, the previous
  project's audio could keep playing.
- Fixed a bug where audio from projects could still play if it is "2 or more levels deep".

## [0.2.0](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.1.3...v0.2.0) (2022-05-26)

This release adds many things and fixes a few annoyances from the previous Version 0.1.3 update.

### Additions

- **Menu Bar Options**: You can now open/save/create a new project using the menu bar.
- **"Save File As" button**: You can now save a copy of the `.audt` file using another name.
- **Settings Page**: Rudimentary settings page that contains some customisation for the spectrogram colour scale and the
  windowing function for the spectrogram.
- **Project List**: The project list on the main page of the AudiTranscribe app is live! You can now access recent
  projects and search for projects from the main page.
- **Note Playing**: This is similar to
  how [the Python version of this project](https://github.com/Ryan-Kan/AudiTranscribe-Python) allows for playing a note
  based on where you click on the spectrogram. You can also use the keyboard to play notes as well!
    - However, a full tutorial on how to do this is not yet live; we are working on it.

### Changes

- **File Format**: The file format has been updated. Older `.audt` files will now
  ***no longer be accepted by the program***. However, the new files should be about half as large, saving memory space.
- **Accessing the About Window**: The "About Window" can now be accessed via the menu bar under the "Help" menu.
- **Logo**: We've changed our logo to look more professional.

### Bugfixes

- Audio continues playing after closing the project ([#2](https://github.com/AudiTranscribe/AudiTranscribe/issues/2)).

More features are in store but are not ready for this release. Here are some of them:

- **Theme Switching**: Allow for changing between *light mode* and *dark mode*.
- **Preventing the Selection of Wrong Files**: Making the "new project" and "open existing project" actions only permit
  users to select the correct types of files.

## [0.1.3](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.1.2...v0.1.3) (2022-05-13)

This release fixes issue [#1](https://github.com/AudiTranscribe/AudiTranscribe/issues/1), where the app fails to go to
fullscreen on a mac.

However, this release also removes the automatic fullscreen of the application. You will now have to manually fullscreen
the application upon starting it.

## [0.1.2](https://github.com/AudiTranscribe/AudiTranscribe/compare/v0.1.1...v0.1.2) (2022-05-12)

This release adds and fixes a few things.

- Added an about window for the details about AudiTranscribe.
    - Although, it is not currently being shown. A proper place for the about window will be introduced in Version
      0.2.0.
- Updated `README.md` to make it easier to understand the project.
- Fixed two bugs.
    - Fixed "not in fullscreen state" leading to "Apple AWT Internal Exception: Java Exception" bug.
    - Fixed weird bug where the user has to double-click play if at end of the audio file.
    -

## [0.1.1](https://github.com/AudiTranscribe/AudiTranscribe/tree/v0.1.1) (2022-05-10)

This is the first release version of AudiTranscribe.

This release fixed a major issue where `pom.xml` was missing some JavaFX dependants.