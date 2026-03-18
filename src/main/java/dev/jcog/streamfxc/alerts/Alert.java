package dev.jcog.streamfxc.alerts;

import com.github.twitch4j.eventsub.events.ChannelCheerEvent;
import com.github.twitch4j.eventsub.events.CustomRewardRedemptionAddEvent;
import dev.jcog.streamfxc.interfaces.Controller;
import dev.jcog.streamfxc.util.TwitchEventListener;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public abstract class Alert implements TwitchEventListener {
    private static final Map<String, Queue<Alert>> QUEUE_MAP = new HashMap<>();

    /* all identical alerts are queued amongst themselves unless overridden by setQueue(). onTriggered() is called once
    * for each time an alert is triggered, but onFinished() is only called once either the queue is empty or the next
    * alert in the queue is of a different type. */
    private static void queueAlert(Alert alert) {
        String queueName = alert.queueName == null ? alert.getClass().toString() : alert.queueName;
        Queue<Alert> queue = QUEUE_MAP.computeIfAbsent(queueName, k -> new ArrayDeque<>());

        boolean active = !queue.isEmpty();
        queue.add(alert);
        if (active) {
            return;
        }
        Controller.getScheduler().schedule(() -> {
            while (!queue.isEmpty()) {
                Alert currentAlert = queue.peek();
                currentAlert.onTrigger();
                queue.poll();
                if (queue.isEmpty() || queue.peek().getClass() != currentAlert.getClass()) {
                    currentAlert.onFinished();
                }
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String rewardName = null;
    private Integer bitAmount = null;
    private String queueName = null;

    public Alert setRewardTrigger(String rewardName) {
        this.rewardName = rewardName;
        return this;
    }

    public Alert setBitTrigger(int bitAmount) {
        this.bitAmount = bitAmount;
        return this;
    }

    public Alert setQueue(String queueName) {
        this.queueName = queueName;
        return this;
    }

    public String getRewardName() {
        return rewardName;
    }

    public Integer getBitAmount() {
        return bitAmount;
    }

    @Override
    public void onChannelPointsRedemption(CustomRewardRedemptionAddEvent channelPointsEvent) {
        if (channelPointsEvent.getReward().getTitle().equals(rewardName)) {
            queueAlert(this);
        }
    }

    @Override
    public void onCheer(ChannelCheerEvent cheerEvent) {
        if (cheerEvent.getBits().equals(bitAmount)) {
            queueAlert(this);
        }
    }

    protected abstract void onTrigger();

    protected abstract void onFinished();

    protected void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }

    protected static void print(String log) {
        System.out.println(Instant.now() + " " + log);
    }
}
