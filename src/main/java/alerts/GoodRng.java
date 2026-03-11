package alerts;

import com.github.twitch4j.eventsub.events.CustomRewardRedemptionAddEvent;
import utilities.AudioClip;
import utilities.TwitchEventListener;

public class GoodRng implements TwitchEventListener {
    private static final String CLIP_FILENAME = "res/close_call.wav";
    private static final String REWARD_NAME = "Give streamer good RNG";

    private final AudioClip clip;

    public GoodRng() {
        clip = new AudioClip(CLIP_FILENAME);
    }
    
    @Override
    public void onChannelPointsRedemption(CustomRewardRedemptionAddEvent channelPointsEvent) {
        if (channelPointsEvent.getReward().getTitle().equals(REWARD_NAME)) {
            clip.playClip();
        }
    }
}
