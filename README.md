![AudiTranscribe Banner](Designs/banner/banner.png "AudiTranscribe")

[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B32213%2Fgithub.com%2FAudiTranscribe%2FAudiTranscribe.svg?type=shield)](https://app.fossa.com/projects/custom%2B32213%2Fgithub.com%2FAudiTranscribe%2FAudiTranscribe?ref=badge_shield)
[![Run Tests On Push To Main](https://github.com/AudiTranscribe/AudiTranscribe/actions/workflows/run-tests-on-push-to-main.yml/badge.svg)](https://github.com/AudiTranscribe/AudiTranscribe/actions/workflows/run-tests-on-push-to-main.yml)
[![Run Tests On Push To Staging](https://github.com/AudiTranscribe/AudiTranscribe/actions/workflows/run-tests-on-push-to-staging.yml/badge.svg)](https://github.com/AudiTranscribe/AudiTranscribe/actions/workflows/run-tests-on-push-to-staging.yml)
[![CodeCov](https://codecov.io/gh/AudiTranscribe/AudiTranscribe/branch/main/graph/badge.svg?token=1WQO7ZGKVJ)](https://codecov.io/gh/AudiTranscribe/AudiTranscribe)
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4.svg)](.github/CODE_OF_CONDUCT.md)

An open source music transcription application.

# Features

AudiTranscribe was built to assist you in transcribing music pieces.

- Convert supported audio files into spectrograms for easier transcription.
- Play notes alongside the music piece to get a "feel" of what notes are being played by clicking on the appropriate row
  on the spectrogram.
- Get a sense of how the notes are arranged in the song.

# Why make AudiTranscribe?

Transcribing music by ear is hard. Tiny details in music pieces may be left out when transcribing by ear, and it takes
practice to properly transcribe notes from a song. AudiTranscribe was created to ease this process and allow the average
person to find out the notes of their favourite songs.

Also, professional music transcription services cost *a lot*, especially if you plan to use the transcription service
a few times over a year. In that case, the cost of these services (which range from $40 to $120) are not worth it.
AudiTranscribe is meant to be an Open-Source and free alternative.

# Limitations

The software is not perfect. There are a few limitations with the software in its current state:

- The spectrogram may make it hard to distinguish between different notes.
- The application is janky and incomplete.
- The application's functionality and features are (largely) undocumented and missing tutorials.

# Dependencies

- FFmpeg: A complete, cross-platform solution to record, convert and stream audio and video. AudiTranscribe uses it to
  help process different audio files.
- Java 17.0.2: The language used to write the application. In the future, standalone builds would be provided, so this
  dependency is only temporarily required.

**For Developers**: Extra dependencies are required for packaging.

- **Windows**: You will need to install the [WiX Toolset 3.0](https://wixtoolset.org/) or later in order
  to generate the AudiTranscribe executable.
- **macOS**: You will need the Xcode Command Line Tools to be installed.

# Installation

## Using An Installer

Currently, the only installers available are for Windows and macOS.

### Windows

1. Head to the latest releases section.
    - If there are no current releases, you may want to choose a pre-release version instead.
2. Under the downloads section, download the Windows installer.
3. Unzip the installer package. The package should contain one `.exe` file.
4. Run the application.
    - It is highly likely that an alert like "Windows protected your PC" would pop up. This is normal, and is due to
      AudiTranscribe not being a recognized app yet.
5. Follow the installation instructions.
6. The application should be installed once completed.

### macOS

1. Head to the latest releases section.
    - If there are no current releases, you may want to choose a pre-release version instead.
2. Under the downloads section, download the macOS file.
3. Unzip the installer package. The package should contain one `.dmg` file.
4. The `.dmg` file would likely be quarantined by Apple due to it lacking a proper signing key. To fix this, run the
   following command, which removes all attributes from the `.dmg` file and makes it no longer quarantined.

```bash
sudo xattr -cr path/to/the/dmg/file
```

5. Open the `.dmg` file.
6. Drag the `AudiTranscribe.app` into the Applications folder.
7. Run the application!

## From Source

This guide will assume that [Apache Maven](https://maven.apache.org/) and [FFmpeg](https://ffmpeg.org/) are installed.

Download the latest release/pre-release from the "releases" section of the GitHub page, or download the latest files
under the `staging` branch.

To set up AudiTranscribe with maven, run the following commands:

```bash
# Install custom dependencies from the "lib" directory
mvn validate

# Install the rest of the dependencies from the maven repository
mvn -B clean:clean compiler:compile javafx:jlink

# Test installation
mvn test
```

# Security Policy

Read AudiTranscribe's security policy at [SECURITY.md](.github/SECURITY.md).

# Contributing to AudiTranscribe

Please read the [CONTRIBUTING.md](.github/CONTRIBUTING.md) file.

# Licence

This project is licensed under the [GNU General Public Licence V3](LICENSE).

[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B32213%2Fgithub.com%2FAudiTranscribe%2FAudiTranscribe.svg?type=large)](https://app.fossa.com/projects/custom%2B32213%2Fgithub.com%2FAudiTranscribe%2FAudiTranscribe?ref=badge_large)
