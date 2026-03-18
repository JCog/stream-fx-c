package alerts;

import utilities.AudioFile;

public class BadRng extends AlertBase {
    private static final String CLIP_FILENAME = "res/bandit_fail.wav";

    private final AudioFile clip;

    public BadRng() {
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
