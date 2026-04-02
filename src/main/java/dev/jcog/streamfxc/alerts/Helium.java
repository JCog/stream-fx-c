package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.interfaces.OBS;
import dev.jcog.streamfxc.util.AudioFile;

public class Helium extends Alert {
    private static final String ID = "Helium";
    private static final String SOURCE_NAME = "Mic";
    private static final String FILTER_NAME = "Helium";
    private static final String DING_FILENAME = "res/ding.wav";
    private static final int INTERVAL_LENGTH = 60 * 1000;

    private final OBS obs;
    private final AudioFile ding;

    public Helium(OBS obs) {
        this.obs = obs;
        ding = new AudioFile(DING_FILENAME);
    }

    @Override
    public String getId() {
        return ID;
    }
    
    @Override
    protected void onTrigger() {
        obs.setSourceFilterEnabled(SOURCE_NAME, FILTER_NAME, true);
        wait(INTERVAL_LENGTH);
    }

    @Override
    protected void onFinished() {
        ding.playClip();
        obs.setSourceFilterEnabled(SOURCE_NAME, FILTER_NAME, false);
    }
}
