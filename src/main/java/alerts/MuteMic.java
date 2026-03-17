package alerts;

import interfaces.OBS;
import utilities.AudioClip;
import utilities.Controller;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MuteMic extends AlertBase {
    private static final String MIC_NAME = "Mic";
    private static final String SCENE_NAME = "Common - DSLR";
    private static final String SOURCE_NAME = "Mute Icon";
    private static final String DING_FILENAME = "res/ding.wav";
    private static final int INTERVAL_LENGTH = 60 * 1000;

    private final OBS obs;
    private final AudioClip ding;

    private boolean active;
    private int queuedTriggers;

    public MuteMic(OBS obs) {
        this.obs = obs;
        ding = new AudioClip(DING_FILENAME);
        active = false;
        queuedTriggers = 0;
    }
    
    @Override
    protected void trigger() {
        queuedTriggers++;
        if (active) {
            return;
        }

        obs.setAudioSourceMuted(MIC_NAME, true);
        obs.setSourceEnabled(SCENE_NAME, SOURCE_NAME, true);
        active = true;

        Controller.getScheduler().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                queuedTriggers--;
                if (queuedTriggers == 0) {
                    this.cancel();
                    ding.playClip();
                    obs.setAudioSourceMuted(MIC_NAME, false);
                    obs.setSourceEnabled(SCENE_NAME, SOURCE_NAME, false);
                    active = false;
                }
            }
        }, INTERVAL_LENGTH, INTERVAL_LENGTH, TimeUnit.MILLISECONDS);
    }
}
