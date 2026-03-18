package alerts;

import utilities.AudioFile;

public class ToadScream extends AlertBase {
    private static final String CLIP_FILENAME = "res/toad_scream.wav";

    private final AudioFile clip;

    public ToadScream() {
        clip = new AudioFile(CLIP_FILENAME);
    }

    @Override
    protected void onTrigger() {
        clip.playClip();
    }

    @Override
    protected void onFinished() {

    }
}
