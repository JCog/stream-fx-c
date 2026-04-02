package dev.jcog.streamfxc.interfaces;

import dev.jcog.streamfxc.alerts.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Controller {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);

    private final OBS obs;
    private final TwitchApi twitchApi;

    public Controller() {
        String host = System.getenv("OBS_HOST");
        int port = Integer.parseInt(System.getenv("OBS_PORT"));
        String password = System.getenv("OBS_PASSWORD");
        String channel = System.getenv("TWITCH_CHANNEL");
        String authToken = System.getenv("TWITCH_AUTH_TOKEN");
        String clientId = System.getenv("TWITCH_CLIENT_ID");

        obs = new OBS(host, port, password);
        twitchApi = new TwitchApi(channel, authToken, clientId);
    }

    public void createAlerts() {
        Alert[] alertList = {
                new AudioAlert("res/bandit_fail.wav").setRewardTrigger("Give streamer bad RNG"),
                new AudioAlert("res/close_call.wav").setRewardTrigger("Give streamer good RNG"),
                new AudioAlert("res/attack_fx_c.wav").setRewardTrigger("Nice"),
                new AudioAlert("res/toad_scream.wav", 0.5d).setRewardTrigger("Toad Scream"),

                new FishHead(obs).setRewardTrigger("Fish Announcer"),
                new MiiChannel().setRewardTrigger("Mii Channel Theme").setBitTrigger(5),

                new Helium(obs).setBitTrigger(150).setQueue("Mic"),
                new MuteMic(obs).setBitTrigger(140).setQueue("Mic"),
        };
        for (Alert alert : alertList) {
            if (alert.getRewardName() != null) {
                twitchApi.registerRewardListener(alert);
            }
            if (alert.getBitAmount() != null) {
                twitchApi.registerBitsListener(alert);
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
