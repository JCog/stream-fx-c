import alerts.AttackFxC;
import alerts.GoodRng;
import alerts.ToadScream;
import interfaces.OBS;
import interfaces.TwitchApi;
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

        obs = new OBS(host, port, password);
        twitchApi = new TwitchApi(channel, authToken, clientId);
        TwitchEventListener[] listeners = {
                new AttackFxC(),
                new GoodRng(),
                new ToadScream(),
        };
        for (TwitchEventListener listener : listeners) {
            twitchApi.registerEventListener(listener);
        }
    }
}
