package alerts;

import utilities.AudioClip;

public class BadRng extends AlertBase {
    private static final String CLIP_FILENAME = "res/bandit_fail.wav";

    private final AudioClip clip;

    public BadRng() {
        clip = new AudioClip(CLIP_FILENAME);
    }
    
    @Override
    protected void trigger() {
        clip.playClip();
    }
}
