package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.util.AudioFile;

import java.util.List;
import java.util.Random;

public class RolloMeow extends Alert {
    private static final String ID = "Rollo Meow";

    private final List<AudioFile> clips;
    private final Random random;

    public RolloMeow(String clipDir) {
        super(ID);
        random = new Random();
        clips = AudioFile.getAudioFilesInDir(clipDir);
    }

    @Override
    protected void onTrigger() {
        clips.get(random.nextInt(clips.size())).playClip();
    }
}
