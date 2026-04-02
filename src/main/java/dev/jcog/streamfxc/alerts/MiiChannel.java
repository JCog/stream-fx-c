package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.util.AlertFuture;
import dev.jcog.streamfxc.util.AudioFile;

import java.util.ArrayList;
import java.util.List;

public class MiiChannel extends Alert {
    private static final String ID = "Mii Channel";
    private static final String CLIP_NAME_FORMAT = "res/mii/mii%d.wav";
    private static final int CLIP_COUNT = 10;

    private final List<AudioFile> clips;

    private int nextClip;

    public MiiChannel() {
        clips = new ArrayList<>();
        for (int i = 0; i < CLIP_COUNT; i++) {
            clips.add(new AudioFile(String.format(CLIP_NAME_FORMAT, i)));
        }
        nextClip = 0;
    }

    @Override
    protected String getId() {
        return ID;
    }

    @Override
    protected void onTrigger() {
        AlertFuture future = clips.get(nextClip).playClip();
        nextClip++;
        nextClip %= CLIP_COUNT;
        future.block();
    }

    @Override
    protected void onFinished() {

    }
}
