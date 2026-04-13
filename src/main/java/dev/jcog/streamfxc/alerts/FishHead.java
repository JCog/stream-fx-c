package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.interfaces.OBS;
import dev.jcog.streamfxc.util.AudioFile;

import java.util.List;
import java.util.Random;

public class FishHead extends Alert {
    private static final String ID = "Fish Head";
    private static final String SCENE_NAME = "Alerts";
    private static final String SOURCE_NAME = "Fish Head";
    private static final String CLIP_FOLDER = "res/fish";

    private final OBS obs;
    private final List<AudioFile> clips;
    private final Random random;

    public FishHead(OBS obs) {
        super(ID);
        this.obs = obs;
        random = new Random();
        clips = AudioFile.getAudioFilesInDir(CLIP_FOLDER);
        for (AudioFile clip : clips) {
            clip.setVolume(0.5d);
        }
    }

    @Override
    protected void onTrigger() {
        Number sourceId = obs.getSourceId(SCENE_NAME, SOURCE_NAME);
        obs.moveSource(SCENE_NAME, sourceId, 0, 0, 0, false);
        obs.setSourceEnabled(SCENE_NAME, sourceId, true);
        clips.get(random.nextInt(clips.size())).playClip().block();

        obs.moveSource(SCENE_NAME, sourceId, 400, 0, 60, false).block();

        obs.setSourceEnabled(SCENE_NAME, sourceId, false);
        wait(500);
    }
}
