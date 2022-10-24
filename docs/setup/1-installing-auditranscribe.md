# Installing AudiTranscribe

This page will guide you through the installation process for AudiTranscribe.

Currently, the only supported platforms for AudiTranscribe are Windows and macOS. We are working on a Linux release.

## Using An Installer

### Windows

1. Head to the latest releases section.
    - If there are no current releases, you may want to choose a pre-release version instead.
2. Under the downloads section, download the Windows installer.
3. Unzip the installer package. The package should contain one `.exe` file.
4. Run the application.
    - It is highly likely that an alert like "Windows protected your PC" would pop up. This is normal, and is due to
      AudiTranscribe not being a recognized app yet.
    - If you see such an alert, click on "More Info", and then click on "Run Anyway" at the bottom of the screen.
5. Follow the installation instructions.
6. The application should be installed once completed.

### macOS

1. Head to the latest releases section.
    - If there are no current releases, you may want to choose a pre-release version instead.
2. Under the downloads section, download the macOS file.
3. Unzip the installer package. The package should contain one `.dmg` file. **Do not open the `.dmg` file yet**.
4. If you were to open the `.dmg` file now, it would likely be quarantined by Apple due to it lacking a proper signing
   key. To fix this, open Terminal and run the following command. The command will remove all attributes from the `.dmg`
   file and makes it no longer quarantined. (**Note**: You may be prompted to enter your password.)

```bash
sudo xattr -cr path/to/the/dmg/file
```

5. Once the command is run, open the `.dmg` file.
6. Drag `AudiTranscribe.app` into the Applications folder.
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
