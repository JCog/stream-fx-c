package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.interfaces.obs.AudioSource;
import dev.jcog.streamfxc.interfaces.obs.Source;
import dev.jcog.streamfxc.util.AudioFile;

public class MuteMic extends Alert {
    private static final String ID = "Mute Mic";
    private static final int INTERVAL_LENGTH = 60 * 1000;

    private static boolean active = false;

    private final AudioSource sourceMic = new AudioSource("Mic");
    private final Source sourceMuteIcon = new Source("Common - DSLR", "Mute Icon");
    private final AudioFile startAudio = new AudioFile("res/i_cant_hear_you.wav");
    private final AudioFile finishAudio = new AudioFile("res/ding.wav");

    public MuteMic() {
        super(ID);
    }
    
    @Override
    protected void onTrigger() {
        if (!active) {
            startAudio.playClip();
            sourceMic.mute();
            sourceMuteIcon.enable();
            active = true;
        }
        waitFromNow(INTERVAL_LENGTH);
    }

    @Override
    protected void onFinished() {
        finishAudio.playClip();
        sourceMic.unmute();
        sourceMuteIcon.disable();
        active = false;
    }
}
