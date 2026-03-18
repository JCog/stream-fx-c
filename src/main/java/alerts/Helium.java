package alerts;

import interfaces.OBS;
import utilities.AlertTask;
import utilities.AudioFile;
import utilities.Controller;

import java.util.concurrent.TimeUnit;

public class Helium extends AlertBase {
    private static final String SOURCE_NAME = "Mic";
    private static final String FILTER_NAME = "Helium";
    private static final String DING_FILENAME = "res/ding.wav";
    private static final int INTERVAL_LENGTH = 60 * 1000;

    private final OBS obs;
    private final AudioFile ding;

    private boolean active;
    private int queuedTriggers;

    public Helium(OBS obs) {
        this.obs = obs;
        ding = new AudioFile(DING_FILENAME);
        active = false;
        queuedTriggers = 0;
    }
    
    @Override
    protected void trigger() {
        queuedTriggers++;
        if (active) {
            return;
        }

        obs.setSourceFilterEnabled(SOURCE_NAME, FILTER_NAME, true);
        active = true;

        Controller.getScheduler().scheduleAtFixedRate(new AlertTask() {
            @Override
            public void runTask() {
                queuedTriggers--;
                if (queuedTriggers == 0) {
                    this.cancel();
                    ding.playClip();
                    obs.setSourceFilterEnabled(SOURCE_NAME, FILTER_NAME, false);
                    active = false;
                }
            }
        }, INTERVAL_LENGTH, INTERVAL_LENGTH, TimeUnit.MILLISECONDS);
    }
}
