package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.util.AudioFile;

public class AudioAlert extends Alert {
    private final String id;
    private final AudioFile clip;

    public AudioAlert(String id, String filePath) {
        this.id = id;
        clip = new AudioFile(filePath);
    }

    public AudioAlert(String id, String filePath, double volume) {
        this(id, filePath);
        clip.setVolume(volume);
    }

    @Override
    public String getId() {
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
