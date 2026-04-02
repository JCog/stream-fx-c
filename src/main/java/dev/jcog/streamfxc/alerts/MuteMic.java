package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.interfaces.OBS;
import dev.jcog.streamfxc.util.AudioFile;

public class MuteMic extends Alert {
    private static final String ID = "Mute Mic";
    private static final String MIC_NAME = "Mic";
    private static final String SCENE_NAME = "Common - DSLR";
    private static final String SOURCE_NAME = "Mute Icon";
    private static final String DING_FILENAME = "res/ding.wav";
    private static final int INTERVAL_LENGTH = 60 * 1000;

    private final OBS obs;
    private final AudioFile ding;

    public MuteMic(OBS obs) {
        this.obs = obs;
        ding = new AudioFile(DING_FILENAME);
    }

    @Override
    public String getId() {
        return ID;
    }
    
    @Override
    protected void onTrigger() {
        obs.setAudioSourceMuted(MIC_NAME, true);
        obs.setSourceEnabled(SCENE_NAME, SOURCE_NAME, true);
        wait(INTERVAL_LENGTH);
    }

    @Override
    protected void onFinished() {
        ding.playClip();
        obs.setAudioSourceMuted(MIC_NAME, false);
        obs.setSourceEnabled(SCENE_NAME, SOURCE_NAME, false);
    }
}
