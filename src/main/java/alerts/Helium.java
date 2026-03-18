package alerts;

import interfaces.OBS;
import utilities.AudioFile;

import java.util.Timer;
import java.util.TimerTask;

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

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                queuedTriggers--;
                if (queuedTriggers == 0) {
                    timer.cancel();
                    ding.playClip();
                    obs.setSourceFilterEnabled(SOURCE_NAME, FILTER_NAME, false);
                    active = false;
                }
            }
        }, INTERVAL_LENGTH, INTERVAL_LENGTH);
    }
}
