package alerts;

import utilities.AudioClip;

public class ToadScream extends AlertBase {
    private static final String CLIP_FILENAME = "res/toad_scream.wav";

    private final AudioClip clip;

    public ToadScream() {
        clip = new AudioClip(CLIP_FILENAME);
    }

    @Override
    protected void trigger() {
        clip.playClip();
    }
}
