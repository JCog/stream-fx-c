package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.interfaces.obs.Source;
import dev.jcog.streamfxc.util.AudioFile;

import java.util.List;
import java.util.Random;

public class FishHead extends Alert {
    private static final String ID = "Fish Head";

    private final Source sourceFish = new Source("Alerts", "Fish Head");

    private final List<AudioFile> clips;
    private final Random random;

    public FishHead(String clipDir) {
        super(ID);
        random = new Random();
        clips = AudioFile.getAudioFilesInDir(clipDir);
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
