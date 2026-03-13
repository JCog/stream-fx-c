package utilities;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.*;
import java.io.File;

public class AudioClip {
    private final File file;

    public AudioClip(String path) {
        file = new File(path);
    }

    public void playClip() {
        Media sound = new Media(file.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
    }
}
