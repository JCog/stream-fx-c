package utilities;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioClip {
    private final File file;

    public AudioClip(String path) {
        file = new File(path);
    }

    public void playClip() {
        AudioInputStream ais;
        try {
            ais = AudioSystem.getAudioInputStream(file);
        } catch (UnsupportedAudioFileException | IOException e) {
            return;
        }
        DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat());

        Clip clip;
        try {
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(ais);
        } catch (LineUnavailableException | IOException e) {
            return;
        }
        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                clip.close();
                try {
                    ais.close();
                } catch (IOException ignored) {}
            }
        });
        clip.start();
    }
}
