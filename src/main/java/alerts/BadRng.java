package alerts;

import com.github.twitch4j.eventsub.events.CustomRewardRedemptionAddEvent;
import utilities.AudioClip;
import utilities.TwitchEventListener;

public class BadRng implements TwitchEventListener {
    private static final String CLIP_FILENAME = "res/bandit_fail.wav";
    private static final String REWARD_NAME = "Give streamer bad RNG";

    private final AudioClip clip;

    public BadRng() {
        clip = new AudioClip(CLIP_FILENAME);
    }
    
    @Override
    public void onChannelPointsRedemption(CustomRewardRedemptionAddEvent channelPointsEvent) {
        if (channelPointsEvent.getReward().getTitle().equals(REWARD_NAME)) {
            clip.playClip();
        }
    }
}
