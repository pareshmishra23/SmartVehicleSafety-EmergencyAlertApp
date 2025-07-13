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


# 🚗 Smart Vehicle Safety & Emergency Alert App  
**Developed by GUS Research Lab**

A lightweight, offline-first Android application that leverages onboard AI (TinyML), sensor fusion, and real-time monitoring to detect vehicular accidents, animal threats, and trigger emergency alerts with GPS coordinates.

---

## 🔍 Features

- **🛰️ Real-Time Speed & Vibration Monitoring**  
  Uses GPS, accelerometer, and gyroscope to detect:
  - Sudden shocks, rollovers, or falls  
  - Logs last 5 minutes of motion data

- **🎤 Sound Detection with On-Device AI**  
  Uses microphone + TensorFlow Lite (YAMNet/custom) to classify:
  - Car crashes, tire screeches  
  - Dog barks, wild animal growls, human screams  
  - Triggers alarms, phone vibration, and alerts on screen

- **🆘 Accident Detection & Auto Alert System**  
  - 15-second countdown for user response  
  - If no response, sends **SMS + emergency call**  
  - Message includes **location, timestamp, and severity**

- **📞 Emergency Contact Management**  
  - Add/manage up to 3 trusted contacts (stored locally)

- **🐕 Dangerous Animal Detection**  
  - Auto-triggers alarm tone and alert popup  
  - Useful in rural or forest routes

- **🗺️ GUI with Live Map Integration**  
  - Real-time map with current location  
  - Accident markers (if triggered)  
  - GPS disabled detection & fallback warning

---

## 🛠️ Build Instructions

### 📦 Prerequisites
- Android Studio (latest version)
- Android SDK (API 21+)
- Physical Android device (recommended) or emulator
- Google Maps API key
- TensorFlow Lite model (`yamnet.tflite` or custom)

---

### 🔄 Setup

```bash
git clone https://github.com/GUS-Research-Lab/SmartVehicleSafety-EmergencyAlertApp.git
cd SmartVehicleSafety-EmergencyAlertApp
Open in Android Studio

File > Open > [Project Folder]

Sync Gradle

File > Sync Project with Gradle Files

Add Google Maps API Key

Add this in local.properties:

ini
Copy
Edit
MAPS_API_KEY=YOUR_GOOGLE_MAPS_KEY
And add to AndroidManifest.xml inside <application>:

xml
Copy
Edit
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
Place TensorFlow Lite Model

Put your yamnet.tflite file in:

css
Copy
Edit
app/src/main/assets/
Build & Run

Connect a physical device or start emulator

Press ▶️ Run

✅ Required Permissions
These permissions are requested at runtime:

RECORD_AUDIO – sound classification

ACCESS_FINE_LOCATION – GPS-based speed/location

CALL_PHONE – emergency call trigger

SEND_SMS – emergency SMS to contacts

ACCESS_COARSE_LOCATION – fallback GPS

FOREGROUND_SERVICE – to run background sensors continuously

🌐 Offline-First Architecture
All core logic (AI inference, sensor analysis) works offline.
Only emergency alerts (SMS/call) require network.
Ideal for rural or low-connectivity areas.

📸 Screenshots (optional placeholders)
Alert Popup	Map View	Emergency Countdown

🤝 Contributing
Pull requests welcome! To contribute:

bash
Copy
Edit
git checkout -b feature-branch
git commit -m "Add your feature"
git push origin feature-branch
Then open a PR.

📄 License
MIT License © GUS Research Lab

## Offline First Design

All core processing and decision-making (sensor data, AI inference) happen offline on the device. Internet connectivity is only required for sending emergency alerts (SMS/Call) and future model updates. This design prioritizes rural user-friendliness, low battery drain, and fast alerts, especially in areas with poor connectivity.

