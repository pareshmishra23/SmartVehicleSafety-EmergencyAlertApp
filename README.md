# Smart Vehicle Safety & Emergency Alert App

This application, developed under GUS Research Lab, leverages sensor fusion, onboard AI (TinyML), and real-time monitoring to detect accidents, animal threats, and trigger emergency alerts. It is optimized for offline environments on regular smartphones.

## Features

1.  **Real-Time Speed & Vibration Monitoring**: Uses GPS, accelerometer, and gyroscope to track speed and detect sudden shocks, tilts, or rollovers. Logs the past 5 minutes of activity.
2.  **Sound Detection with On-Device AI (TinyML)**: Utilizes the microphone and an onboard TensorFlow Lite model (YAMNet or custom) to detect sounds like car crashes, tire screeches, dog barks, animal growls/hisses, and human screams. Triggers danger alarms, vibrates the phone, and displays on-screen alerts.
3.  **Accident Detection & Auto Alert**: Detects accidents based on a combination of sudden speed drops, high vibration, and suspicious sounds. Prompts the user for a response with a 15-second countdown. If no response, automatically sends SMS and makes calls to emergency contacts with GPS coordinates, timestamp, and severity level.
4.  **Emergency Contacts (NOS)**: Allows users to add and manage up to 3 emergency contacts securely stored on the device.
5.  **Dangerous Animal Detection**: Triggers a loud alarm tone, phone vibration, and a popup alert if dangerous animal sounds are detected.
6.  **GUI with Live Map Integration**: Integrates Google Maps SDK to display real-time current location, auto-updates the map, and shows accident markers if triggered. Displays a message if GPS is disabled.

## Build Instructions

1.  **Prerequisites**:
    *   Android Studio (latest version recommended)
    *   Android SDK (API Level 21 or higher)
    *   A physical Android device or emulator for testing (required for sensor and telephony features)

2.  **Clone the repository**:
    ```bash
    git clone https://github.com/GUS-Research-Lab/SmartVehicleSafety-EmergencyAlertApp.git
    cd SmartVehicleSafety-EmergencyAlertApp
    ```

3.  **Open in Android Studio**:
    *   Open Android Studio.
    *   Select `File > Open` and navigate to the cloned `SmartVehicleSafety-EmergencyAlertApp` directory.

4.  **Sync Gradle**: Android Studio should automatically sync the Gradle project. If not, click `File > Sync Project with Gradle Files`.

5.  **Add Google Maps API Key**: 
    *   Obtain a Google Maps API key from the Google Cloud Console. 
    *   Add the key to your `local.properties` file in the root of the project:
        ```properties
        MAPS_API_KEY=YOUR_API_KEY_HERE
        ```
    *   Then, in `app/src/main/AndroidManifest.xml`, add the following inside the `<application>` tag:
        ```xml
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="${MAPS_API_KEY}" />
        ```

6.  **Place TensorFlow Lite Model**: 
    *   Download the `yamnet.tflite` model (or your custom TFLite model) and place it in `app/src/main/assets/model/`.

7.  **Build and Run**: 
    *   Connect an Android device or start an emulator.
    *   Click the `Run` button (green triangle) in Android Studio to build and install the app on your device/emulator.

## Required Permissions

The application requires the following permissions, which will be requested at runtime:

*   `android.permission.RECORD_AUDIO`: For sound detection.
*   `android.permission.ACCESS_FINE_LOCATION`: For GPS speed tracking and location services.
*   `android.permission.SEND_SMS`: For sending emergency SMS messages.
*   `android.permission.CALL_PHONE`: For making emergency calls.
*   `android.permission.ACCESS_COARSE_LOCATION`: For network-based location.
*   `android.permission.FOREGROUND_SERVICE`: To run services in the foreground for continuous monitoring.

## Offline First Design

All core processing and decision-making (sensor data, AI inference) happen offline on the device. Internet connectivity is only required for sending emergency alerts (SMS/Call) and future model updates. This design prioritizes rural user-friendliness, low battery drain, and fast alerts, especially in areas with poor connectivity.

