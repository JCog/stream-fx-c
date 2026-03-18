package dev.jcog.streamfxc.interfaces;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.api.IEventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.eventsub.events.ChannelCheerEvent;
import com.github.twitch4j.eventsub.events.CustomRewardRedemptionAddEvent;
import com.github.twitch4j.eventsub.socket.IEventSubSocket;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.jetbrains.annotations.Nullable;
import dev.jcog.streamfxc.util.TwitchEventListener;

import java.util.Collections;

import static java.lang.System.out;

public class TwitchApi {
    private final String authToken;
    private final TwitchClient twitchClient;
    private final User user;

    public TwitchApi(String channel, String authToken, String clientId) {
        this.authToken = authToken;


        OAuth2Credential oauth = new OAuth2Credential("twitch", authToken);
        twitchClient = TwitchClientBuilder.builder()
                .withClientId(clientId)
                .withDefaultAuthToken(oauth)
                .withEnableHelix(true)
                .withEnableEventSocket(true)
                .build();

        User tempUser = null;
        try {
            tempUser = getUserByUsername(channel);
        } catch (HystrixRuntimeException ignored) {
            out.println("Error retrieving Twitch channel information");
        }
        if (tempUser == null) {
            System.exit(1);
        }
        user = tempUser;

        IEventSubSocket eventSocket = twitchClient.getEventSocket();
        eventSocket.register(SubscriptionTypes.CHANNEL_CHEER.prepareSubscription(
                b -> b.broadcasterUserId(user.getId()).build(), null
        ));
        eventSocket.register(SubscriptionTypes.CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_ADD.prepareSubscription(
                b -> b.broadcasterUserId(user.getId()).build(), null
        ));
    }

    public void registerRewardListener(TwitchEventListener eventListener) {
        IEventManager eventSubEvents = twitchClient.getEventSocket().getEventManager();
        eventSubEvents.onEvent(CustomRewardRedemptionAddEvent.class, eventListener::onChannelPointsRedemption);
    }

    public void registerBitsListener(TwitchEventListener eventListener) {
        IEventManager eventSubEvents = twitchClient.getEventSocket().getEventManager();
        eventSubEvents.onEvent(ChannelCheerEvent.class, eventListener::onCheer);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Nullable
    public User getUserByUsername(String username) throws HystrixRuntimeException {
        UserList userList = twitchClient.getHelix().getUsers(
                authToken,
                null,
                Collections.singletonList(username)
        ).execute();
        if (userList.getUsers().isEmpty()) {
            return null;
        }
        return userList.getUsers().getFirst();
    }
}
