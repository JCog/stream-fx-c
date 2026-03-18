package alerts;

import interfaces.OBS;
import utilities.AudioFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FishHead extends Alert {
    private static final String SCENE_NAME = "Alerts";
    private static final String SOURCE_NAME = "Fish Head";
    private static final String CLIP_NAME_FORMAT = "res/fish/fish%d.wav";
    private static final int CLIP_COUNT = 12;

    private final OBS obs;
    private final List<AudioFile> clips;
    private final Random random;

    public FishHead(OBS obs) {
        this.obs = obs;
        random = new Random();
        clips = new ArrayList<>();
        for (int i = 0; i < CLIP_COUNT; i++) {
            AudioFile clip = new AudioFile(String.format(CLIP_NAME_FORMAT, i));
            clip.setVolume(0.5d);
            clips.add(clip);
        }
    }

    @Override
    protected void onTrigger() {
        Number sourceId = obs.getSourceId(SCENE_NAME, SOURCE_NAME);
        obs.moveSource(SCENE_NAME, sourceId, 0, 0, 0, false);
        obs.setSourceEnabled(SCENE_NAME, sourceId, true);
        clips.get(random.nextInt(CLIP_COUNT)).playClip().block();

        obs.moveSource(SCENE_NAME, sourceId, 400, 0, 60, false).block();

        obs.setSourceEnabled(SCENE_NAME, sourceId, false);
        wait(500);
    }

    @Override
    protected void onFinished() {

    }
}
