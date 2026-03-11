package alerts;

import com.github.twitch4j.eventsub.events.CustomRewardRedemptionAddEvent;
import utilities.AudioClip;
import utilities.TwitchEventListener;

public class ToadScream implements TwitchEventListener {
    private static final String CLIP_FILENAME = "res/toad_scream.wav";
    private static final String REWARD_NAME = "Toad Scream";

    private final AudioClip clip;

    public ToadScream() {
        clip = new AudioClip(CLIP_FILENAME);
    }
    
    @Override
    public void onChannelPointsRedemption(CustomRewardRedemptionAddEvent channelPointsEvent) {
        if (channelPointsEvent.getReward().getTitle().equals(REWARD_NAME)) {
            clip.playClip();
        }
    }
}
