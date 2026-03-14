import alerts.*;
import interfaces.OBS;
import interfaces.TwitchApi;
import javafx.application.Platform;
import utilities.TwitchEventListener;

public class Controller {
    private final OBS obs;
    private final TwitchApi twitchApi;

    public Controller() {
        String host = System.getenv("OBS_HOST");
        int port = Integer.parseInt(System.getenv("OBS_PORT"));
        String password = System.getenv("OBS_PASSWORD");
        String channel = System.getenv("TWITCH_CHANNEL");
        String authToken = System.getenv("TWITCH_AUTH_TOKEN");
        String clientId = System.getenv("TWITCH_CLIENT_ID");

        Platform.startup(() -> {});
        obs = new OBS(host, port, password);
        twitchApi = new TwitchApi(channel, authToken, clientId);
        TwitchEventListener[] listeners = {
                new BadRng().setRewardTrigger("Give streamer bad RNG"),
                new GoodRng().setRewardTrigger("Give streamer good RNG"),
                new MiiChannel().setRewardTrigger("Mii Channel Theme").setBitTrigger(5),
                new Nice().setRewardTrigger("Nice"),
                new ToadScream().setRewardTrigger("Toad Scream"),
        };
        for (TwitchEventListener listener : listeners) {
            twitchApi.registerEventListener(listener);
        }
    }
}
