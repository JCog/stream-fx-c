package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.interfaces.OBS;
import dev.jcog.streamfxc.util.AudioFile;

public class Helium extends Alert {
    private static final String ID = "Helium";
    private static final String SOURCE_NAME = "Mic";
    private static final String FILTER_NAME = "Helium";
    private static final int INTERVAL_LENGTH = 60 * 1000;

    private final OBS obs;
    private final AudioFile finishClip;

    public Helium(OBS obs, String finishFilename) {
        super(ID);
        this.obs = obs;
        finishClip = new AudioFile(finishFilename);
    }
    
    @Override
    protected void onTrigger() {
        obs.setSourceFilterEnabled(SOURCE_NAME, FILTER_NAME, true);
        waitFromNow(INTERVAL_LENGTH);
    }

    @Override
    protected void onFinished() {
        finishClip.playClip();
        obs.setSourceFilterEnabled(SOURCE_NAME, FILTER_NAME, false);
    }
}
