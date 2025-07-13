package gusresearch.safety;

import android.content.Context;
import android.media.AudioRecord;
import android.util.Log;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier.AudioClassifierOptions;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SoundClassifier {

    private static final String MODEL_FILE = "model/yamnet.tflite";
    private static final String TAG = "SoundClassifier";

    private AudioClassifier audioClassifier;
    private AudioRecord audioRecord;
    private ScheduledExecutorService executorService;
    private TensorAudio tensorAudio;

    public interface SoundDetectionListener {
        void onSoundDetected(String soundLabel, float confidence);
    }

    private SoundDetectionListener listener;

    public SoundClassifier(Context context, SoundDetectionListener listener) {
        this.listener = listener;
        try {
            AudioClassifierOptions options = AudioClassifierOptions.builder()
                    .setMaxResults(1)
                    .build();
            audioClassifier = AudioClassifier.createFromFileAndOptions(context, MODEL_FILE, options);
            tensorAudio = audioClassifier.createInputTensorAudio();

        } catch (IOException e) {
            Log.e(TAG, "Error initializing audio classifier: " + e.getMessage());
        }
    }

    public void startListening() {
        if (audioClassifier == null) {
            Log.e(TAG, "Audio classifier not initialized.");
            return;
        }

        audioRecord = audioClassifier.createAudioRecord();

        audioRecord.startRecording();

        executorService = Executors.newSingleThreadScheduledExecutor();
       /* executorService.scheduleAtFixedRate(() -> {
            int numberOfSamples = tensorAudio.load(audioRecord);
            List<Category> output = audioClassifier.classify(tensorAudio);

            if (!output.isEmpty()) {
                Category topCategory = output.get(0);
                String label = topCategory.getLabel();
                float confidence = topCategory.getScore();
                Log.d(TAG, "Detected: " + label + " with confidence: " + confidence);
                if (listener != null) {
                    listener.onSoundDetected(label, confidence);
                }
            }
        }, 0, audioClassifier.getRequiredInputBufferSizeMs(), TimeUnit.MILLISECONDS);*/

        executorService.scheduleAtFixedRate(() -> {
            int numberOfSamples = tensorAudio.load(audioRecord);

            // classify returns List<Classifications>, not List<Category>
            List<Classifications> results = audioClassifier.classify(tensorAudio);

            if (!results.isEmpty() && !results.get(0).getCategories().isEmpty()) {
                Category topCategory = results.get(0).getCategories().get(0);
                String label = topCategory.getLabel();
                float confidence = topCategory.getScore();
                Log.d(TAG, "Detected: " + label + " with confidence: " + confidence);
                if (listener != null) {
                    listener.onSoundDetected(label, confidence);
                }
            }
        }, 0, 500, TimeUnit.MILLISECONDS); // Replace 500 with a suitable fixed rate (ms)

    }

    public void stopListening() {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }
}

