package alerts;

import utilities.AudioClip;

public class Nice extends AlertBase {
    private static final String CLIP_FILENAME = "res/attack_fx_c.wav";

    private final AudioClip clip;

    public Nice() {
        clip = new AudioClip(CLIP_FILENAME);
    }
    
    @Override
    protected void trigger() {
        clip.playClip();
    }
}
