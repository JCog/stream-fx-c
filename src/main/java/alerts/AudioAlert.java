package alerts;

import utilities.AudioFile;

public class AudioAlert extends Alert {
    private final AudioFile clip;

    public AudioAlert(String filePath) {
        clip = new AudioFile(filePath);
    }

    public AudioAlert(String filePath, double volume) {
        clip = new AudioFile(filePath);
        clip.setVolume(volume);
    }
    
    @Override
    protected void onTrigger() {
        clip.playClip();
    }

    @Override
    protected void onFinished() {

    }
}
