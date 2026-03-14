package alerts;

import utilities.AudioClip;

public class GoodRng extends AlertBase {
    private static final String CLIP_FILENAME = "res/close_call.wav";

    private final AudioClip clip;

    public GoodRng() {
        clip = new AudioClip(CLIP_FILENAME);
    }

    @Override
    protected void trigger() {
        clip.playClip();
    }
}
