package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.interfaces.obs.Filter;
import dev.jcog.streamfxc.util.AudioFile;

public class Helium extends Alert {
    private static final String ID = "Helium";
    private static final int INTERVAL_LENGTH = 60 * 1000;

    private final Filter filterHelium = new Filter("Mic", "Helium");

    private final AudioFile startClip = new AudioFile("res/balloon_inflate.wav");
    private final AudioFile finishClip = new AudioFile("res/ding.wav");

    public Helium() {
        super(ID);
    }
    
    @Override
    protected void onTrigger() {
        startClip.playClip();
        filterHelium.enable();
        waitFromNow(INTERVAL_LENGTH);
    }

    @Override
    protected void onFinished() {
        finishClip.playClip();
        filterHelium.disable();
    }
}
