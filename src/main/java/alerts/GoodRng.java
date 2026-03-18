package alerts;

import utilities.AudioFile;

public class GoodRng extends AlertBase {
    private static final String CLIP_FILENAME = "res/close_call.wav";

    private final AudioFile clip;

    public GoodRng() {
        clip = new AudioFile(CLIP_FILENAME);
    }

    @Override
    protected void trigger() {
        clip.playClip();
    }
}
