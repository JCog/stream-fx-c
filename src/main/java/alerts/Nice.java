package alerts;

import utilities.AudioFile;

public class Nice extends AlertBase {
    private static final String CLIP_FILENAME = "res/attack_fx_c.wav";

    private final AudioFile clip;

    public Nice() {
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
