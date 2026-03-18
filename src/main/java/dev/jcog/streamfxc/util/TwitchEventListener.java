package dev.jcog.streamfxc.util;

import com.github.twitch4j.eventsub.events.*;

public interface TwitchEventListener {
    default void onChannelPointsRedemption(CustomRewardRedemptionAddEvent channelPointsEvent) {}
    default void onCheer(ChannelCheerEvent cheerEvent) {}
}
