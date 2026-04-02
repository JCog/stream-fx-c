package dev.jcog.streamfxc.misc;

import dev.jcog.streamfxc.alerts.*;
import dev.jcog.streamfxc.interfaces.OBS;
import dev.jcog.streamfxc.interfaces.TwitchApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);

    private final OBS obs;
    private final TwitchApi twitchApi;
    private final List<Alert> alertList;

    public Controller() {
        String host = System.getenv("OBS_HOST");
        int port = Integer.parseInt(System.getenv("OBS_PORT"));
        String password = System.getenv("OBS_PASSWORD");
        String channel = System.getenv("TWITCH_CHANNEL");
        String authToken = System.getenv("TWITCH_AUTH_TOKEN");
        String clientId = System.getenv("TWITCH_CLIENT_ID");

        obs = new OBS(host, port, password);
        twitchApi = new TwitchApi(channel, authToken, clientId);

        alertList = Arrays.asList(
                new AudioAlert("Bad RNG", "res/bandit_fail.wav").setRewardTrigger("Give streamer bad RNG"),
                new AudioAlert("Good RNG", "res/close_call.wav").setRewardTrigger("Give streamer good RNG"),
                new AudioAlert("Nice", "res/attack_fx_c.wav").setRewardTrigger("Nice"),
                new AudioAlert("Toad Scream", "res/toad_scream.wav", 0.5d).setRewardTrigger("Toad Scream"),

                new FishHead(obs).setRewardTrigger("Fish Announcer"),
                new MiiChannel().setRewardTrigger("Mii Channel Theme").setBitTrigger(5),

                new Helium(obs).setBitTrigger(150).setQueue("Mic"),
                new MuteMic(obs).setBitTrigger(140).setQueue("Mic")
        );

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
    }

    public void listen() {
        Console console = System.console();
        if (console == null) {
            log.warn("Console not available");
            return;
        }

        while (true) {
            String line = console.readLine();
            if (line.equals("quit")) {
                closeAll();
                return;
            }

            if (line.equals("alerts")) {
                for (int i = 0; i < alertList.size(); i++) {
                    System.out.printf("%d. %s%n", i, alertList.get(i).getId());
                }
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

    public void closeAll() {
        scheduler.shutdown();
        twitchApi.close();
        obs.close();
    }

    public static ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}
