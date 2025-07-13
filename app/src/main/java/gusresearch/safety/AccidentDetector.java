package gusresearch.safety;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class AccidentDetector implements SensorEventListener {

    private static final String TAG = "AccidentDetector";
    private static final float SHAKE_THRESHOLD = 25.0f; // m/s^2
    private static final int SPEED_DROP_THRESHOLD = 20; // km/h

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Context context;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable countdownRunnable;
    private boolean accidentDetected = false;

    public interface AccidentListener {
        void onAccidentDetected();
        void onUserPrompt();
    }

    private AccidentListener listener;

    public AccidentDetector(Context context, AccidentListener listener) {
        this.context = context;
        this.listener = listener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x * x + y * y + z * z);

            if (acceleration > SHAKE_THRESHOLD) {
                Log.d(TAG, "High vibration detected: " + acceleration);
                // This is a potential accident, now check for other conditions
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void checkForAccident(Location lastLocation, Location currentLocation, String lastSound) {
        if (accidentDetected) return;

        float speedDrop = 0;
        if (lastLocation != null && currentLocation != null) {
            float lastSpeed = lastLocation.getSpeed() * 3.6f;
            float currentSpeed = currentLocation.getSpeed() * 3.6f;
            speedDrop = lastSpeed - currentSpeed;
        }

        boolean isCrashSound = lastSound.equals("car_crash") || lastSound.equals("tire_screech");

        if (speedDrop > SPEED_DROP_THRESHOLD && isCrashSound) {
            Log.d(TAG, "Accident detected! Speed drop: " + speedDrop + ", Sound: " + lastSound);
            accidentDetected = true;
            if (listener != null) {
                listener.onUserPrompt();
            }
            startCountdown();
        }
    }

    private void startCountdown() {
        countdownRunnable = () -> {
            if (listener != null) {
                listener.onAccidentDetected();
            }
        };
        handler.postDelayed(countdownRunnable, 15000); // 15 seconds
    }

    public void cancelCountdown() {
        if (countdownRunnable != null) {
            handler.removeCallbacks(countdownRunnable);
            countdownRunnable = null;
            accidentDetected = false;
        }
    }
}

