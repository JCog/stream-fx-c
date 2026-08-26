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

    private final AudioFile startAudio;
    private final AudioFile finishAudio;

    public MuteMic(String startFilename, String finishFilename) {
        super(ID);
        startAudio = new AudioFile(startFilename);
        finishAudio = new AudioFile(finishFilename);
    }

    @Override
    public String getId() {
        return ID;
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
