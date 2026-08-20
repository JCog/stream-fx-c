package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.interfaces.OBS;
import dev.jcog.streamfxc.util.AudioFile;

public class MuteMic extends Alert {
    private static final String ID = "Mute Mic";
    private static final String MIC_NAME = "Mic";
    private static final String SCENE_NAME = "Common - DSLR";
    private static final String SOURCE_NAME = "Mute Icon";
    private static final int INTERVAL_LENGTH = 60 * 1000;

    private static boolean active = false;

    private final OBS obs;
    private final AudioFile startAudio;
    private final AudioFile finishAudio;

    public MuteMic(OBS obs, String startFilename, String finishFilename) {
        super(ID);
        this.obs = obs;
        startAudio = new AudioFile(startFilename);
        finishAudio = new AudioFile(finishFilename);
    }

    @Override
    public String getId() {
        return ID;
    }
    
    @Override
    protected void onTrigger() {
        if (!active) {
            startAudio.playClip();
            obs.setAudioSourceMuted(MIC_NAME, true);
            obs.setSourceEnabled(SCENE_NAME, SOURCE_NAME, true);
            active = true;
        }
        waitFromNow(INTERVAL_LENGTH);
    }

    @Override
    protected void onFinished() {
        finishAudio.playClip();
        obs.setAudioSourceMuted(MIC_NAME, false);
        obs.setSourceEnabled(SCENE_NAME, SOURCE_NAME, false);
        active = false;
    }
}
