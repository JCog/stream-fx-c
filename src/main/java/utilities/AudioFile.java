package utilities;

import javafx.scene.media.AudioClip;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AudioFile {
    private final AudioClip clip;
    private final long duration; // milliseconds

    public AudioFile(String path) {
        File file = new File(path);
        AudioFormat format;
        try {
            format = AudioSystem.getAudioInputStream(file).getFormat();
        } catch (UnsupportedAudioFileException | IOException e) {
            System.out.println("Error loading " + path + ": " + e.getMessage());
            duration = 0;
            clip = null;
            return;
        }
        duration = (long) (file.length() / (format.getFrameSize() * format.getFrameRate()) * 1000);
        clip = new AudioClip(file.toURI().toString());
    }

    public AlertFuture playClip() {
        AlertFuture future = new AlertFuture();
        Controller.getScheduler().schedule(future::complete, duration, TimeUnit.MILLISECONDS);
        clip.play();
        return future;
    }

    // ranges from 0 to 1
    public void setVolume(double volume) {
        clip.setVolume(volume);
    }
}
