![AudiTranscribe Banner](Designs/banner/banner.png "AudiTranscribe Banner")

[![Latest GitHub Release](https://img.shields.io/github/v/release/AudiTranscribe/AudiTranscribe)](https://github.com/AudiTranscribe/AudiTranscribe/releases/latest)
[![Licence](https://img.shields.io/github/license/AudiTranscribe/AudiTranscribe)](https://github.com/AudiTranscribe/AudiTranscribe/blob/main/LICENSE)
[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B32213%2Fgithub.com%2FAudiTranscribe%2FAudiTranscribe.svg?type=shield)](https://app.fossa.com/projects/custom%2B32213%2Fgithub.com%2FAudiTranscribe%2FAudiTranscribe?ref=badge_shield)
[![CodeCov](https://codecov.io/gh/AudiTranscribe/AudiTranscribe/branch/main/graph/badge.svg?token=1WQO7ZGKVJ)](https://codecov.io/gh/AudiTranscribe/AudiTranscribe)
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4.svg)](.github/CODE_OF_CONDUCT.md)

An open-source music transcription application.

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

# Dependencies

The only dependency needed is [FFmpeg](https://ffmpeg.org/): a complete, cross-platform solution to record, convert and
stream audio and video. AudiTranscribe uses it to help process different audio files.

**Note**:

- For Windows and macOS, AudiTranscribe will attempt to automatically install FFmpeg. You do **not** need to manually
  install FFmpeg unless specified.
- For Linux and others, **manual installation of FFmpeg is required**.

## For Developers

If you are planning to develop for AudiTranscribe, please ensure that **Java 17** is installed on your machine.

In addition, there are some additional dependencies to install for specific platforms:

- **Windows**: The [**WiX Toolset 3**](https://wixtoolset.org/) is needed to generate the AudiTranscribe executable for
  Windows.
- **macOS**: On macOS, the **Xcode Command Line Tools** needs to be installed to make AudiTranscribe run in development.
  You can install the XCode Command Line Tools by running:
  ```
  xcode-select --install
  ```
  in a terminal.
- **Linux**: Please ensure that `jlink` is properly installed on your machine by running:
  ```
  jlink --version
  ```
  If an error appears, please install `jlink` by running:
  ```
  sudo apt install openjdk-17-jdk-headless
  ```
  or a similar command on your Linux machine.

# Installation

Please read the [Installation Guide](docs/setup/installing-auditranscribe.md).

# Security Policy

Read AudiTranscribe's security policy at [SECURITY.md](.github/SECURITY.md).

# Contributing to AudiTranscribe

Please read the [CONTRIBUTING.md](.github/CONTRIBUTING.md) file.

# Licences

Full licence information, including dependencies' licences, can be found [here](https://auditranscribe.app/licences).

This project is licensed under the [GNU General Public Licence V3](LICENSE).

[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B32213%2Fgithub.com%2FAudiTranscribe%2FAudiTranscribe.svg?type=large)](https://app.fossa.com/projects/custom%2B32213%2Fgithub.com%2FAudiTranscribe%2FAudiTranscribe?ref=badge_large)
