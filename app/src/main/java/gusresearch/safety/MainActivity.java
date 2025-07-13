package gusresearch.safety;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SoundClassifier.SoundDetectionListener, AccidentDetector.AccidentListener, SensorEventListener {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GoogleMap googleMap;
    private TextView speedTextView;
    private TextView soundDetectionTextView;
    private TextView vibrationTextView;
    private List<Location> locationHistory = new ArrayList<>();
    private SoundClassifier soundClassifier;
    private AccidentDetector accidentDetector;
    private MediaPlayer mediaPlayer;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Location lastKnownLocation;
    private String lastDetectedSound;
    private EmergencyContactManager emergencyContactManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speedTextView = findViewById(R.id.speedTextView);
        soundDetectionTextView = findViewById(R.id.soundDetectionTextView);
        vibrationTextView = findViewById(R.id.vibrationTextView);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        soundClassifier = new SoundClassifier(this, this);
        accidentDetector = new AccidentDetector(this, this);
        emergencyContactManager = new EmergencyContactManager(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        checkPermissions();
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
            soundClassifier.startListening();
            accidentDetector.start();
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                startLocationUpdates();
                soundClassifier.startListening();
                accidentDetector.start();
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Toast.makeText(this, "Required permissions denied. Some features may not work.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // 5 seconds
        locationRequest.setFastestInterval(3000); // 3 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocationUI(location);
                    updateSpeed(location);
                    if (lastKnownLocation != null) {
                        accidentDetector.checkForAccident(lastKnownLocation, location, lastDetectedSound);
                    }
                    lastKnownLocation = location;
                    locationHistory.add(location);
                    // Keep only last 5 minutes of location history
                    if (locationHistory.size() > 300) { // 300 locations for 5 minutes at 1 sec interval, adjust based on actual interval
                        locationHistory.remove(0);
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void updateLocationUI(Location location) {
        if (googleMap != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
        }
    }

    private void updateSpeed(Location location) {
        if (location.hasSpeed()) {
            float speedMps = location.getSpeed(); // Speed in meters/second
            float speedKmh = speedMps * 3.6f; // Convert to km/h
            speedTextView.setText(String.format("Speed: %.2f km/h", speedKmh));
        } else {
            speedTextView.setText("Speed: N/A");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onSoundDetected(String soundLabel, float confidence) {
        runOnUiThread(() -> {
            soundDetectionTextView.setText(String.format("Detected: %s (%.2f)", soundLabel, confidence));
            lastDetectedSound = soundLabel;
            if (soundLabel.equals("dog_bark") || soundLabel.equals("animal_growl") || soundLabel.equals("human_scream")) { // Example dangerous sounds
                triggerDangerAlarm(soundLabel);
            }
        });
    }

    private void triggerDangerAlarm(String soundType) {
        // Vibrate phone
        // Display on-screen alert
        // Play loud alarm tone

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound); // You need to add an alarm_sound.mp3 in res/raw
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        new AlertDialog.Builder(this)
                .setTitle("Danger Alert: " + soundType + " Detected!")
                .setMessage("Please be cautious.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mediaPlayer != null) {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onAccidentDetected() {
        // This is called after 15 seconds if user doesn\'t respond
        Toast.makeText(this, "Accident confirmed! Sending emergency alerts.", Toast.LENGTH_LONG).show();
        sendEmergencyAlerts();
    }

    @Override
    public void onUserPrompt() {
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Accident Detected!")
                    .setMessage("Are you okay? Countdown to emergency alert: 15 seconds.")
                    .setPositiveButton("I\\'m OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            accidentDetector.cancelCountdown();
                            Toast.makeText(MainActivity.this, "Glad you\\'re safe!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setCancelable(false)
                    .show();
        });
    }

    private void sendEmergencyAlerts() {
        List<EmergencyContactManager.Contact> contacts = emergencyContactManager.getContacts();
        if (contacts.isEmpty()) {
            Toast.makeText(this, "No emergency contacts found. Please add contacts in settings.", Toast.LENGTH_LONG).show();
            return;
        }

        String locationLink = "";
        if (lastKnownLocation != null) {
            locationLink = String.format(Locale.US, "https://maps.google.com/?q=%f,%f", lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        }

        String timestamp = new SimpleDateFormat("dd MMMM yyyy, HH:mm a", Locale.US).format(new Date());
        String severity = "High"; // For now, assuming high severity on auto-alert

        String smsMessage = String.format(
                "🚨 Accident Detected!\nName: [User\\'s Name]\nLocation: %s\nTime: %s\nSeverity: %s\n\nPlease assist immediately.",
                locationLink, timestamp, severity);

        SmsManager smsManager = SmsManager.getDefault();
        for (EmergencyContactManager.Contact contact : contacts) {
            try {
                smsManager.sendTextMessage(contact.phoneNumber, null, smsMessage, null, null);
                Toast.makeText(this, "SMS sent to " + contact.name, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to send SMS to " + contact.name + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

            // Make a call to the first contact (or all, depending on desired behavior)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + contact.phoneNumber));
                startActivity(callIntent);
                Toast.makeText(this, "Calling " + contact.name, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Call permission not granted.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x * x + y * y + z * z);
            vibrationTextView.setText(String.format("Vibration: %.2f m/s^2", acceleration));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
        soundClassifier.startListening();
        accidentDetector.start();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        soundClassifier.stopListening();
        accidentDetector.stop();
        sensorManager.unregisterListener(this);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

