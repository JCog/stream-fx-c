package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.util.AudioFile;

import java.util.List;
import java.util.Random;

public class RolloMeow extends Alert {
    private static final String ID = "Rollo Meow";

    private final List<AudioFile> clips = AudioFile.getAudioFilesInDir("res/rollo");;
    private final Random random = new Random();

    public RolloMeow() {
        super(ID);
    }

    @Override
    protected void onTrigger() {
        clips.get(random.nextInt(clips.size())).playClip();
    }
}
