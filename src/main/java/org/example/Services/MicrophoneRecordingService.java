package org.example.Services;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class MicrophoneRecordingService {

    private TargetDataLine line;
    private Thread recordingThread;
    private File outputFile;
    private volatile boolean recording = false;

    private final AudioFormat format = new AudioFormat(
            16000.0f,   // sample rate
            16,         // sample size in bits
            1,          // mono
            true,       // signed
            false       // little endian
    );

    public boolean isRecording() {
        return recording;
    }

    public File startRecording() throws LineUnavailableException, IOException {
        if (recording) {
            throw new IllegalStateException("Recording is already in progress.");
        }

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Microphone line not supported.");
        }

        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        outputFile = File.createTempFile("ecotrack_recording_", ".wav");
        recording = true;

        recordingThread = new Thread(() -> {
            try (AudioInputStream ais = new AudioInputStream(line)) {
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        recordingThread.setDaemon(true);
        recordingThread.start();

        return outputFile;
    }

    public File stopRecording() {
        if (!recording) {
            return outputFile;
        }

        recording = false;

        if (line != null) {
            line.stop();
            line.close();
        }

        return outputFile;
    }
}