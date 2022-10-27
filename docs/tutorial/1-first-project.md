# 1. First Project

This page will guide you through setting up a new AudiTranscribe project.

Once the AudiTranscribe setup process is complete, AudiTranscribe will bring you to the main page. The main page should
show a list of projects.

{% hint style="info" %}
If this is your first time, there will not be any projects that are shown.<br>
<img alt="No Projects" src="img/1-first-project/no-projects.jpg" width=300>
{% endhint %}

The following steps will guide you through how to create a transcription. You can
use [this example file](misc/Example.mp3) if you do not have a file to try out the transcription.

1. Click on "New Project from Audio File".
2. AudiTranscribe will show a popup. This is the **project setup view**.<br>
   <img alt="Project Setup View" src="img/1-first-project/project-setup.jpg" width=300><br>
3. Enter any name that you want for the project. We recommend the name `Example Project` for your first project.
4. For the audio file, click on "Select File". A popup will appear asking you to select a file.<br>
   <img alt="Select Audio File" src="img/1-first-project/select-audio-file.jpg" width=300><br>
   Navigate to your audio file (or the example audio file, which is named `Example.mp3`) and select it. The field for
   the audio file should automatically update with the path to the audio file.
5. Choose whether you want the Beats Per Minute (BPM) and the music key to be estimated automatically. For this example,
   we will **leave the settings as is**.
    - For your own projects, however, you may specify the BPM and music key manually.
6. Once you are comfortable with the settings, click on "Create".
7. The application will now attempt to generate a spectrogram for that audio file and estimate the required things.<br>
   <img alt="Generating Spectrogram" src="img/1-first-project/generating-spectrogram.jpg" width=300><br>
   Wait for all processes to complete. You can tell that it is done when the progress bar in the top-right corner
   disappears.

Now you have a spectrogram! To save your progress, either:

- click on the floppy disk icon near the top-left corner of the screen;
- save using the menu option of File > Save (or File > Save As...); or
- use the keyboard shortcut Ctrl + S (or ⌘ + S on macOS).

Congratulations! You have just created your first AudiTranscribe file! Move on to
the [User Interface](2-user-interface.md) tutorial to learn how to understand the buttons on screen.
