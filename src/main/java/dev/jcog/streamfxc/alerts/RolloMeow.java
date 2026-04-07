package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.util.AudioFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RolloMeow extends Alert {
    private static final String ID = "Rollo Meow";
    private static final String CLIP_NAME_FORMAT = "res/rollo/rollo%d.wav";
    private static final int CLIP_COUNT = 4;

    private final List<AudioFile> clips;
    private final Random random;

    public RolloMeow() {
        random = new Random();
        clips = new ArrayList<>();
        for (int i = 0; i < CLIP_COUNT; i++) {
            AudioFile clip = new AudioFile(String.format(CLIP_NAME_FORMAT, i));
            clips.add(clip);
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected void onTrigger() {
        clips.get(random.nextInt(CLIP_COUNT)).playClip();
    }

    @Override
    protected void onFinished() {

    }
}
