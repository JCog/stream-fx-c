package alerts;

import interfaces.OBS;
import utilities.AudioFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FishHead extends AlertBase {
    private static final String SCENE_NAME = "Alerts";
    private static final String SOURCE_NAME = "Fish Head";
    private static final String CLIP_NAME_FORMAT = "res/fish/fish%d.wav";
    private static final int CLIP_COUNT = 12;

    private final OBS obs;
    private final List<AudioFile> clips;
    private final Random random;

    private int queuedTriggers;
    private boolean playing;

    public FishHead(OBS obs) {
        this.obs = obs;
        random = new Random();
        queuedTriggers = 0;
        playing = false;
        clips = new ArrayList<>();
        for (int i = 0; i < CLIP_COUNT; i++) {
            clips.add(new AudioFile(String.format(CLIP_NAME_FORMAT, i)));
        }
    }

    @Override
    protected void trigger() {
        if (playing) {
            queuedTriggers++;
            return;
        }
        playing = true;
        playNext();
    }

    private void playNext() {
        Number sourceId = obs.getSourceId(SCENE_NAME, SOURCE_NAME);
        obs.moveSource(SCENE_NAME, sourceId, 0, 0, 0, false, null);
        obs.setSourceEnabled(SCENE_NAME, sourceId, true);
        clips.get(random.nextInt(CLIP_COUNT)).playClip(this::hideFish);
    }

    private void hideFish() {
        Number sourceId = obs.getSourceId(SCENE_NAME, SOURCE_NAME);
        obs.moveSource(SCENE_NAME, sourceId, 400, 0, 60, false, this::cleanup);
    }

    private void cleanup() {
        Number sourceId = obs.getSourceId(SCENE_NAME, SOURCE_NAME);
        obs.setSourceEnabled(SCENE_NAME, sourceId, false);
        wait(500);
        if (queuedTriggers > 0) {
            queuedTriggers--;
            playNext();
        } else {
            playing = false;
        }
    }
}
