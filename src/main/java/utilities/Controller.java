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
        TwitchEventListener[] listeners = {
                new BadRng().setRewardTrigger("Give streamer bad RNG"),
                new FishHead(obs).setRewardTrigger("Fish Announcer"),
                new GoodRng().setRewardTrigger("Give streamer good RNG"),
                new Helium(obs).setBitTrigger(150).setQueue("Mic"),
                new MiiChannel().setRewardTrigger("Mii Channel Theme").setBitTrigger(5),
                new MuteMic(obs).setBitTrigger(140).setQueue("Mic"),
                new Nice().setRewardTrigger("Nice"),
                new ToadScream().setRewardTrigger("Toad Scream"),
        };
        for (TwitchEventListener listener : listeners) {
            twitchApi.registerEventListener(listener);
        }
    }

    public static ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}
