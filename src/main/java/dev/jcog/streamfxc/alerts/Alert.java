package dev.jcog.streamfxc.alerts;

import com.github.twitch4j.eventsub.events.ChannelCheerEvent;
import com.github.twitch4j.eventsub.events.CustomRewardRedemptionAddEvent;
import dev.jcog.streamfxc.misc.Controller;
import dev.jcog.streamfxc.util.TwitchEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class Alert implements TwitchEventListener {
    private static final Logger log = LoggerFactory.getLogger(Alert.class);
    private static final Map<String, Queue<Alert>> QUEUE_MAP = new HashMap<>();
    private static final List<Alert> delayedAlerts = new ArrayList<>();
    private static String currentObsScene = null;

    protected enum TriggerSource {
        CHANNEL_POINTS,
        BITS,
        MANUAL,
    }

    /* all identical alerts are queued amongst themselves unless overridden by setQueue(). onTriggered() is called once
    * for each time an alert is triggered, but onFinished() is only called once either the queue is empty or the next
    * alert in the queue is of a different type. */
    private static void queueAlert(Alert alert) {
        // if an alert isn't whitelisted for the current OBS scene, delay it and recheck when the scene changes

        // this should almost definitely be done when the alert about to be triggered instead, but the logic gets messy
        // and I don't want to deal with that right now. in the meantime, simply don't change scenes while there are
        // alerts in the queue.
        if (!alert.isWhitelisted(currentObsScene)) {
            delayedAlerts.add(alert);
            return;
        }

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
                log.debug("\"{}\" triggered", currentAlert.getId());
                currentAlert.triggerTime = Instant.now();
                currentAlert.onTrigger();
                queue.poll();
                if (queue.isEmpty() || queue.peek().getClass() != currentAlert.getClass()) {
                    log.debug("\"{}\" finished", currentAlert.getId());
                    currentAlert.onFinished();
                }
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    public static void queueDelayedAlerts() {
        List<Alert> delayedCopy = List.copyOf(delayedAlerts);
        delayedAlerts.clear();
        delayedCopy.forEach(Alert::queueAlert);
    }

    public static void setCurrentObsScene(String sceneName) {
        currentObsScene = sceneName;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final String id;
    private String rewardName = null;
    private String rewardId = null;
    private Integer bitAmount = null;
    private String queueName = null;
    private Set<String> sceneWhitelist = null;

    private TriggerSource triggerSource;
    private Instant triggerTime;

    public Alert(String id) {
        this.id = id;
        this.triggerSource = null;
        this.triggerTime = null;
    }

    public Alert withRewardTrigger(String rewardName) {
        this.rewardName = rewardName;
        return this;
    }

    public void withRewardId(String rewardId) {
        this.rewardId = rewardId;
    }

    public Alert withBitTrigger(int bitAmount) {
        this.bitAmount = bitAmount;
        return this;
    }

    public Alert withQueue(String queueName) {
        this.queueName = queueName;
        return this;
    }

    // whitelisted for all scenes if not specified
    public Alert withSceneWhitelist(String ... sceneNames) {
        sceneWhitelist = new HashSet<>(List.of(sceneNames));
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getId() {
        return id;
    }

    public String getRewardName() {
        return rewardName;
    }

    public String getRewardId() {
        return rewardId;
    }

    public Integer getBitAmount() {
        return bitAmount;
    }

    public boolean isWhitelisted(String scene) {
        if (sceneWhitelist == null) {
            return true;
        }
        return sceneWhitelist.contains(scene);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onChannelPointsRedemption(CustomRewardRedemptionAddEvent channelPointsEvent) {
        if (channelPointsEvent.getReward().getTitle().equals(rewardName)) {
            log.info("\"{}\" queued by {} via \"{}\" reward", getId(), channelPointsEvent.getUserName(), rewardName);
            this.triggerSource = TriggerSource.CHANNEL_POINTS;
            queueAlert(this);
        }
    }

    @Override
    public void onCheer(ChannelCheerEvent cheerEvent) {
        if (cheerEvent.getBits().equals(bitAmount)) {
            log.info("\"{}\" queued by {} via {} bits", getId(), cheerEvent.getUserName(), bitAmount);
            this.triggerSource = TriggerSource.BITS;
            queueAlert(this);
        }
    }

    public void queueManually() {
        log.info("\"{}\" queued manually", getId());
        this.triggerSource = TriggerSource.MANUAL;
        queueAlert(this);
    }

    protected abstract void onTrigger();

    protected void onFinished() {}

    protected TriggerSource getTriggerSource() {
        return this.triggerSource;
    }

    protected void waitFromNow(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("{}: {}", getId(), e.getMessage());
        }
    }

    protected void waitUntil(long millisFromTrigger) {
        Instant target = triggerTime.plusMillis(millisFromTrigger);
        long waitTime = Duration.between(Instant.now(), target).toMillis();
        waitFromNow(waitTime);
    }

    protected long elapsedMillis() {
        return Duration.between(triggerTime, Instant.now()).toMillis();
    }
}
