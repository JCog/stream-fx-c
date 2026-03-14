package alerts;

import utilities.AudioClip;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class MiiChannel extends AlertBase {
    private static final String CLIP_NAME_FORMAT = "res/mii%d.wav";
    private static final int CLIP_COUNT = 10;

    private final List<AudioClip> clips;
    private final Queue<AudioClip> clipQueue;

    private int nextClip;
    private boolean stopped;

    public MiiChannel() {
        clips = new ArrayList<>();
        for (int i = 0; i < CLIP_COUNT; i++) {
            clips.add(new AudioClip(String.format(CLIP_NAME_FORMAT, i)));
        }
        clipQueue = new ArrayDeque<>();
        nextClip = 0;
        stopped = true;
    }

    @Override
    protected void trigger() {
        clipQueue.add(clips.get(nextClip++));
        nextClip %= CLIP_COUNT;
        if (stopped) {
            stopped = false;
            playNextClip();
        }
    }

    private void playNextClip() {
        AudioClip clip = clipQueue.poll();
        if (clip == null) {
            stopped = true;
            return;
        }
        clip.playClip(this::playNextClip);
    }
}
