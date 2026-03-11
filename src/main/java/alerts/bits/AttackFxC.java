package alerts.bits;

import com.github.twitch4j.eventsub.events.ChannelCheerEvent;
import utilities.AudioClip;
import utilities.TwitchEventListener;

public class AttackFxC implements TwitchEventListener {
    private static final String CLIP_FILENAME = "res/attack_fx_c.wav";
    private static final int BIT_AMOUNT = 69;

    private final AudioClip clip;

    public AttackFxC() {
        clip = new AudioClip(CLIP_FILENAME);
    }
    
    @Override
    public void onCheer(ChannelCheerEvent cheerEvent) {
        if (cheerEvent.getBits() == BIT_AMOUNT) {
            clip.playClip();
        }
    }
}
