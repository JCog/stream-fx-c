package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.util.AudioFile;

import java.util.List;
import java.util.Random;

public class RolloMeow extends Alert {
    private static final String ID = "Rollo Meow";
    private static final String CLIP_DIR = "res/rollo";

    private final List<AudioFile> clips;
    private final Random random;

    public RolloMeow() {
        super(ID);
        random = new Random();
        clips = AudioFile.getAudioFilesInDir(CLIP_DIR);
    }

    @Override
    protected void onTrigger() {
        clips.get(random.nextInt(clips.size())).playClip();
    }
}
