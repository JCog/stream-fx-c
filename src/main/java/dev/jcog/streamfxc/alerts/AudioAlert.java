package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.util.AudioFile;

import java.nio.file.Paths;

public class AudioAlert extends Alert {
    private final String id;
    private final AudioFile clip;

    public AudioAlert(String filePath) {
        id = Paths.get(filePath).getFileName().toString();
        clip = new AudioFile(filePath);
    }

    public AudioAlert(String filePath, double volume) {
        this(filePath);
        clip.setVolume(volume);
    }

    @Override
    protected String getId() {
        return id;
    }

    @Override
    protected void onTrigger() {
        clip.playClip();
    }

    @Override
    protected void onFinished() {

    }
}
