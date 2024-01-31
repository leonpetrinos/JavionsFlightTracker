# JavionsFlightTracker

## Overview
Javions is a flight tracking application developed during the second semester at EPFL University. This project involves decoding ADS-B messages from aircraft and presenting the gathered data on a world map using a JavaFX application.

## JavaFX Application
The Javions application provides a user-friendly graphical interface for visualizing air traffic around the world. It leverages JavaFX to create an interactive map with various features.

### Key Features
- Aircraft Description: Information about each aircraft, including its model, call sign, ICAO address, etc...
- Speed: Real-time speed data of each aircraft 
- Position: Real-time geographical coordinates of each aircraft displayed on the map.
- Altitude: Altitude information, visually represented through the *plasma color gradient*.
- Trajectory: Clicking on an aircraft reveals its trajectory, enhancing situational awareness.

### Preview
<p align="center">
  <img src="resources/JavionRecording.gif" alt="JavionsGif">
</p>

#### Initial Lag Explanation

- **Laggy Start**: It's normal if the program experiences initial lagginess. This occurs as the application fetches tiles constituting the world map from [OpenStreetMap](https://www.openstreetmap.org/#map=5/55.216/-106.348).

- **Tile Cache Mechanism**: As you explore different areas and zoom levels, the program stores these tiles in a folder named `tile-cache` within your project directory.

- **Performance Optimization**: Subsequent visits to previously explored zones fetch tiles directly from the local cache instead of the URL. This significantly improves performance, resulting in a smoother and less laggy experience.

## Operation Modes
The Flight Tracker offers versatility through two distinctive modes to accommodate various setups and preferences:

1. File Mode: In this mode, the application decodes messages sourced from a `.bin` file. This option proves beneficial for offline analysis or when you want to scrutinize previously collected flight data.

2. Live Mode: Javions seamlessly transitions into live mode when an [Airspy R2 device](https://airspy.com/airspy-r2/) is connected. The application decodes messages in real-time, delivering flight data for an immersive and current experience.

### Using the File Mode (on IntelliJ IDE)
Note that JavaFX must configured in your IDE as well as the correct version of Java. Take a look at the Prerequisites section for this. If this is done, you can proceed.
To use the file mode, I have provided a few `.bin` files in the resources folder. To run the application on one of these files, follow these steps: 
1. In the `Run` menu, choose `Edit Configurations`.
2. Go to your configuration for running the Main (this should have been created in the prerequisites section).
3. In the `Program Arguments`, paste the absolute path of the `.bin` file you want to run.
4. By running the program, the application should now work.

### Using the Live Mode (on IntelliJ IDE)
Note that JavaFX must configured in your IDE as well as the correct version of Java. Take a look at the Prerequisites section for this. If this is done, you can proceed.
To use the live mode, you must also have the Airspy R2 device of course. 
- #### Airspy Host Installation
Install [the project airspyone_host](https://github.com/airspy/airspyone_host). This is easier to do with a package manager. Unfortunately I don't know which package manager(s) offer anything for AirSpy on Windows or Linux. On MacOs, make sure you install [Homebrew](https://brew.sh/). If this is done, run the command:
```bash
brew install airspy
```
- #### Verify you can start the Javions program from the shell
Navigate to the project directory using the terminal and execute the following command to run Javions with provided sample messages:
```bash
java --enable-preview -cp out/production/Javions/ --module-path ${JFX_PATH?} --add-modules javafx.controls ch.epfl.javions.gui.Main messages_20230318_0915.bin
```
where ${JFX_PATH?} must be replaced by the path to the JavaFX `lib` subfolder. Note that the bin file at the end of the command can be changed to satisfy your needs (note that this is equivalent to the File Mode).

If all of this works, you should be able to connect the Airspy device. 
- #### Running the program with the Airspy device
Once you have connected the Airspy to your computer via USB and you have navigated to the project directory using the terminal, run the following command: 
```bash
airspy_rx -r - -f 1090 -t 5 -g 17 | java --enable-preview -cp out/production/Javions/ --module-path ${JFX_PATH?} --add-modules javafx.controls ch.epfl.javions.gui.Main
```
where again, ${JFX_PATH?} must be replaced by the path to the JavaFX `lib` subfolder.
- #### Screenshot of the program running with the Airspy connected 
<p align="center">
  <img src="resources/javions_live_mode.png" alt="JavionsPic">
</p>

## Prerequisites

### Java Version:

You must have Java 17 installed to run this application. Take a look at the [Java Download Page](https://www.oracle.com/java/technologies/javase-downloads.html) for information on installation.

### JavaFX Version and Configuration in IntelliJ:

This application runs on JavaFX version 20. To install JavaFX, visit the [OpenJFX download page](https://gluonhq.com/products/javafx/). Once JavaFX is installed, configure it correctly in your IDE. If you're using IntelliJ, follow these steps:

1. Unzip the JavaFX archive to create a folder named `javafx-sdk-20` with subfolders `legal` and `lib`.
2. In IntelliJ, open settings (`Appearance and Behavior` > `Path Variables`).
3. Click on the `+` button. Under **Name**, write `JFX_PATH` and under **Value**, write the path to the `lib` subfolder of the unzipped JavaFX archive.
4. Open IntelliJ IDEA and go to the `File` menu.
5. Choose `New Project Setup` and then select `Structure`.
6. In the left sidebar, click on `Global Libraries` under the `Platform Settings` section.
7. In the central section, click the `+` button at the top and select `Java` from the `New Global Library` menu.
8. Navigate to the `lib` folder created during the archive decompression of OpenJFX.
9. Select all files in the `lib` folder and click `Open`.
10. Change the name of the library to "OpenJFX 20" by modifying the field next to the `Name:` label.
11. Click the `+` button under the `Name:` label.
12. Select the `src.zip` file located in the parent folder of the `lib` folder and click `Open`.
14. Click `OK` in the dialog box that opens.

### Using JavaFX in a Project:

Now that the JavaFX library is correctly installed in your IDE, here is the way to use it in your project: 

1. In the `File` menu, select `Project Structure`.
2. Click on `Modules` in the `Project Settings` section.
3. Select the `Dependencies` tab.
4. Click on the `+` button, then choose `Library` from the menu that opens.
5. In the window that opens, select `OpenJFX 20`, then click on `Add Selected`.

If you encounter issues, try the following:

1. In the `Run` menu, choose `Edit Configurations`.
2. Click the `+` button and select `Application`.
3. On the right hand side, click on `Modify options`, then choose `Add VM options`.
4. In the field entitled `VM options`, add the following line (replace `$JFX_PATH$` with your JavaFX Path): `--module-path $JFX_PATH$ --add-modules javafx.controls`

This setup ensures that the OpenJFX 20 library is properly configured in IntelliJ IDEA, allowing you to seamlessly integrate it into your JavaFX project.
