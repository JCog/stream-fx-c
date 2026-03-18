package alerts;

import com.github.twitch4j.eventsub.events.ChannelCheerEvent;
import com.github.twitch4j.eventsub.events.CustomRewardRedemptionAddEvent;
import utilities.Controller;
import utilities.TwitchEventListener;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public abstract class AlertBase implements TwitchEventListener {
    private String rewardName = null;
    private Integer bitAmount = null;

    public AlertBase setRewardTrigger(String rewardName) {
        this.rewardName = rewardName;
        return this;
    }

    public AlertBase setBitTrigger(int bitAmount) {
        this.bitAmount = bitAmount;
        return this;
    }

    @Override
    public void onChannelPointsRedemption(CustomRewardRedemptionAddEvent channelPointsEvent) {
        if (channelPointsEvent.getReward().getTitle().equals(rewardName)) {
            Controller.getScheduler().schedule(this::trigger, 0, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onCheer(ChannelCheerEvent cheerEvent) {
        if (cheerEvent.getBits().equals(bitAmount)) {
            Controller.getScheduler().schedule(this::trigger, 0, TimeUnit.MILLISECONDS);
        }
    }

    protected abstract void trigger();

    protected void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }

    protected void print(String log) {
        System.out.println(Instant.now() + " " + log);
    }
}
