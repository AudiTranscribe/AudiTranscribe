# AudiTranscribe Changelog

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

## Before 0.8.3

Note that a changelog was not maintained before version 0.8.3.
