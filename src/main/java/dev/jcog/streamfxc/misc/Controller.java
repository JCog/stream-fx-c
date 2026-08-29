package dev.jcog.streamfxc.misc;

import com.github.twitch4j.eventsub.domain.Reward;
import com.github.twitch4j.helix.domain.CustomReward;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import dev.jcog.streamfxc.alerts.*;
import dev.jcog.streamfxc.interfaces.OBS;
import dev.jcog.streamfxc.interfaces.TwitchApi;
import io.obswebsocket.community.client.message.event.scenes.CurrentProgramSceneChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);
    private static final String QUEUE_MIC = "Mic";
    private static final String[] WHITELIST_SMALL_CAMERA = {
            "Desktop",
            "Casual (SD)",
            "Casual (HD)",
            "Rando",
            "Speedrun (HD)",
            "Speedrun (SD)",
    };
    private static final OBS obs = new OBS(
            System.getenv("OBS_HOST"),
            Integer.parseInt(System.getenv("OBS_PORT")),
            System.getenv("OBS_PASSWORD")
    );

    private final TwitchApi twitchApi;
    private final List<Alert> alertList;

    public Controller() {
        String channel = System.getenv("TWITCH_CHANNEL");
        String authToken = System.getenv("TWITCH_AUTH_TOKEN");
        String clientId = System.getenv("TWITCH_CLIENT_ID");

        twitchApi = new TwitchApi(channel, authToken, clientId);

        alertList = Arrays.asList(
                // Audio
                new AudioAlert("Bad RNG", "res/bandit_fail.wav")
                        .withRewardTrigger("Give streamer bad RNG"),
                new AudioAlert("Good RNG", "res/close_call.wav")
                        .withRewardTrigger("Give streamer good RNG"),
                new AudioAlert("Nice", "res/attack_fx_c.wav")
                        .withRewardTrigger("Nice"),
                new AudioAlert("Toad Scream", "res/toad_scream.wav", 0.5d)
                        .withRewardTrigger("Toad Scream")
                        .withBitTrigger(50),

                // Mic Queue
                new Helium()
                        .withBitTrigger(150)
                        .withQueue(QUEUE_MIC),
                new MuteMic()
                        .withBitTrigger(140)
                        .withQueue(QUEUE_MIC),
                new PowerShock()
                        .withRewardTrigger("Power Shock")
                        .withBitTrigger(200)
                        .withQueue(QUEUE_MIC)
                        .withSceneWhitelist(WHITELIST_SMALL_CAMERA),

                // Misc
                new FishHead()
                        .withRewardTrigger("Realistic Fish Head")
                        .withBitTrigger(99),
                new LilOinks()
                        .withRewardTrigger("Li'l Oink")
                        .withBitTrigger(101),
                new MiiChannel()
                        .withRewardTrigger("Mii Channel Theme")
                        .withBitTrigger(5),
                new Nut()
                        .withRewardTrigger("Nut")
                        .withBitTrigger(80)
                        .withSceneWhitelist(WHITELIST_SMALL_CAMERA),
                new OuttaSight()
                        .withRewardTrigger("Outta Sight")
                        .withBitTrigger(110)
                        .withSceneWhitelist(WHITELIST_SMALL_CAMERA),
                new RolloMeow()
                        .withRewardTrigger("Rollo Meow")
                        .withBitTrigger(20)
        );

        // get alert reward IDs
        Map<String, String> rewardNamesToIds = new HashMap<>();
        List<CustomReward> customRewards;
        try {
            customRewards = twitchApi.getCustomRewards(null, true);
        } catch (HystrixRuntimeException e) {
            log.error("unable to get initial list of Channel Point Rewards from Twitch");
            throw new RuntimeException();
        }
        for (CustomReward reward : customRewards) {
            rewardNamesToIds.put(reward.getTitle(), reward.getId());
        }

        List<String> nullIds = new ArrayList<>();
        for (Alert alert : alertList) {
            if (alert.getRewardName() != null) {
                String name = alert.getRewardName();
                String id = rewardNamesToIds.get(name);
                if (id == null) {
                    nullIds.add(name);
                } else {
                    alert.withRewardId(id);
                }
            }
        }
        if (!nullIds.isEmpty()) {
            log.warn("Alerts with no manageable Channel Point Reward:\n    {}", nullIds);
        }

        // register alerts
        StringBuilder sb = new StringBuilder("Alerts:\n");
        for (int i = 0; i < alertList.size(); i++) {
            Alert alert = alertList.get(i);
            sb.append(String.format(" %d: %s", i, alert.getId()));
            if (alert.getRewardName() != null) {
                sb.append(String.format(", \"%s\" reward", alert.getRewardName()));
                twitchApi.registerRewardListener(alert);
            }
            if (alert.getBitAmount() != null) {
                sb.append(String.format(", %d bits", alert.getBitAmount()));
                twitchApi.registerBitsListener(alert);
            }
            sb.append("\n");
        }
        sb.setLength(sb.length() - 1);
        log.info(sb.toString());

        obs.registerSceneChangeEvent(this::onSceneChanged);
        obs.init();

        // I don't like using a while loop here, but trying to query anything from OBS from within any callback always
        // times out for reasons I don't currently understand
        while (!getObs().isReady()) {}
        onObsReady();
    }

    public boolean listen() {
        Console console = System.console();
        if (console == null) {
            log.warn("Console not available");
            return false;
        }

        while (true) {
            String line = console.readLine();
            if (line.equals("quit") || line.equals("q")) {
                closeAll();
                return true;
            }

            if (line.equals("alerts")) {
                for (int i = 0; i < alertList.size(); i++) {
                    console.printf("%d. %s%n", i, alertList.get(i).getId());
                }
            } else if (line.startsWith("remake")) {
                String oldTitle = console.readLine("Old title: ");
                String newTitle = console.readLine("New title: ");
                remakeChannelPointReward(oldTitle, newTitle);
            } else {
                Integer alertIdx;
                try {
                    alertIdx = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    alertIdx = null;
                }

                if (alertIdx != null && alertIdx < alertList.size()) {
                    alertList.get(alertIdx).queueManually();
                }
            }
        }
    }

    private void onObsReady() {
        // initialize enabling alerts
        String currentScene = getObs().getCurrentScene();
        List<Alert> alerts = alertList.stream().filter(a -> a.isWhitelisted(currentScene)).toList();
        setAlertsPaused(alerts, false);
    }

    private void onSceneChanged(CurrentProgramSceneChangedEvent event) {
        event.getSceneName();
        List<Alert> toUnpause = alertList.stream().filter(a -> a.isWhitelisted(event.getSceneName())).toList();
        List<Alert> toPause = alertList.stream().filter(a -> !a.isWhitelisted(event.getSceneName())).toList();
        setAlertsPaused(toUnpause, false);
        setAlertsPaused(toPause, true);
    }

    private void setAlertsPaused(List<Alert> alerts, boolean pause) {
        List<String> rewardIds = alerts.stream()
                .map(Alert::getRewardId)
                .filter(Objects::nonNull)
                .toList();
        if (rewardIds.isEmpty()) {
            return;
        }

        List<CustomReward> customRewards;
        try {
            customRewards = twitchApi.getCustomRewards(rewardIds, true).stream()
                    .filter(r -> r.isPaused() != pause)
                    .toList();
        } catch (HystrixRuntimeException e) {
            log.error("unable to get list of Channel Point Rewards from Twitch");
            return;
        }
        log.info(
                "{}pausing {}",
                pause ? "" : "un",
                customRewards.stream().map(CustomReward::getTitle).toList()
        );
        for (CustomReward reward : customRewards) {
            reward = reward.withIsPaused(pause);
            try {
                twitchApi.updateReward(reward);
            } catch (HystrixRuntimeException e) {
                log.error("unable to {}pause reward \"{}\"", pause ? "" : "un", reward.getTitle());
            }
        }
    }

    private void remakeChannelPointReward(String oldTitle, String newTitle) {
        List<CustomReward> rewards;
        try {
            rewards = twitchApi.getCustomRewards(null, false);
        } catch (HystrixRuntimeException e) {
            log.error(e.getMessage());
            return;
        }
        for (CustomReward reward : rewards) {
            if (reward.getTitle().equals(oldTitle)) {
                reward = reward.withTitle(newTitle).withIsEnabled(false);
                try {
                    twitchApi.createCustomReward(reward);
                } catch (HystrixRuntimeException e) {
                    log.error(e.getMessage());
                    return;
                }
                log.info("Successfully recreated \"{}\" as \"{}\"", oldTitle, newTitle);

                Reward.Image img = reward.getImage();
                if (img != null) {
                    log.info("images:\n{}\n{}\n{}", img.getUrl1x(), img.getUrl2x(), img.getUrl4x());
                }
                return;
            }
        }
        log.warn("Unable to find reward named \"{}\"", oldTitle);
    }

    public void closeAll() {
        setAlertsPaused(alertList, true);
        scheduler.shutdown();
        twitchApi.close();
        obs.close();
    }

    public static ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public static OBS getObs() {
        return obs;
    }
}
