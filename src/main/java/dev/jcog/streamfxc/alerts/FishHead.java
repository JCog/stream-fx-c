package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.interfaces.obs.Source;
import dev.jcog.streamfxc.util.AudioFile;

import java.util.List;
import java.util.Random;

public class FishHead extends Alert {
    private static final String ID = "Fish Head";

    private final Source sourceFish = new Source("Alerts - Fish Head", "Fish Head");
    private final List<AudioFile> clips = AudioFile.getAudioFilesInDir("res/fish");
    private final Random random = new Random();

    public FishHead() {
        super(ID);
        for (AudioFile clip : clips) {
            clip.setVolume(0.5d);
        }
    }

    @Override
    protected void onTrigger() {
        sourceFish.moveAbsolute(0, 0);
        sourceFish.enable();
        clips.get(random.nextInt(clips.size())).playClip().block();

        sourceFish.moveAbsolute(400, 0, 60).block();

        sourceFish.disable();
        waitFromNow(500);
    }
}
