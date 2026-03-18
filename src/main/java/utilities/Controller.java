package utilities;

import alerts.*;
import interfaces.OBS;
import interfaces.TwitchApi;
import javafx.application.Platform;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Controller {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);

    public Controller() {
        String host = System.getenv("OBS_HOST");
        int port = Integer.parseInt(System.getenv("OBS_PORT"));
        String password = System.getenv("OBS_PASSWORD");
        String channel = System.getenv("TWITCH_CHANNEL");
        String authToken = System.getenv("TWITCH_AUTH_TOKEN");
        String clientId = System.getenv("TWITCH_CLIENT_ID");

        Platform.startup(() -> {});
        OBS obs = new OBS(host, port, password);
        TwitchApi twitchApi = new TwitchApi(channel, authToken, clientId);
        Alert[] alertList = {
                new AudioAlert("res/bandit_fail.wav").setRewardTrigger("Give streamer bad RNG"),
                new AudioAlert("res/close_call.wav").setRewardTrigger("Give streamer good RNG"),
                new AudioAlert("res/attack_fx_c.wav").setRewardTrigger("Nice"),
                new AudioAlert("res/toad_scream.wav").setRewardTrigger("Toad Scream"),

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

    public static ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}
