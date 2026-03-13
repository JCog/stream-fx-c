package utilities;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.*;
import java.io.File;

public class AudioClip {
    private final Media sound;

    public AudioClip(String path) {
        sound = new Media(new File(path).toURI().toString());
    }

    public void playClip() {
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
    }

    public void playClip(Runnable stopCallback) {
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.setOnEndOfMedia(stopCallback);
        mediaPlayer.play();
    }
}
