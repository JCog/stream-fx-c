package dev.jcog.streamfxc.interfaces.obs;

import dev.jcog.streamfxc.interfaces.OBS;
import dev.jcog.streamfxc.misc.Controller;

public class AudioSource {
    private static final OBS obs = Controller.getObs();

    private final String sourceName;

    public AudioSource(String sourceName) {
        this.sourceName = sourceName;
    }

    public Boolean isMuted() {
        return obs.getAudioSourceMuted(sourceName);
    }

    public void mute() {
        obs.setAudioSourceMuted(sourceName, true);
    }

    public void unmute() {
        obs.setAudioSourceMuted(sourceName, false);
    }
}
