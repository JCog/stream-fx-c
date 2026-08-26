package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.util.AlertFuture;
import dev.jcog.streamfxc.util.AudioFile;

import java.util.List;

public class MiiChannel extends Alert {
    private static final String ID = "Mii Channel";

    private final List<AudioFile> clips = AudioFile.getAudioFilesInDir("res/mii");

    private int nextClip;

    public MiiChannel() {
        super(ID);
        nextClip = 0;
    }

    @Override
    protected void onTrigger() {
        AlertFuture future = clips.get(nextClip).playClip();
        nextClip++;
        nextClip %= clips.size();
        future.block();
    }
}
