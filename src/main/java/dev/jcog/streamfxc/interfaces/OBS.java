package dev.jcog.streamfxc.interfaces;

import com.google.gson.JsonObject;
import dev.jcog.streamfxc.misc.Controller;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.OBSRemoteControllerBuilder;
import io.obswebsocket.community.client.message.event.scenes.CurrentProgramSceneChangedEvent;
import io.obswebsocket.community.client.message.response.filters.GetSourceFilterResponse;
import io.obswebsocket.community.client.message.response.inputs.GetInputMuteResponse;
import io.obswebsocket.community.client.message.response.sceneitems.GetSceneItemEnabledResponse;
import io.obswebsocket.community.client.message.response.sceneitems.GetSceneItemIdResponse;
import io.obswebsocket.community.client.message.response.sceneitems.GetSceneItemTransformResponse;
import io.obswebsocket.community.client.message.response.scenes.GetCurrentProgramSceneResponse;
import io.obswebsocket.community.client.model.SceneItem;
import dev.jcog.streamfxc.util.AlertFuture;
import dev.jcog.streamfxc.util.AlertTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class OBS {
    private static final Logger log = LoggerFactory.getLogger(OBS.class);
    private static final int TIMEOUT = 1000;
    private static final int FRAMERATE = 60;

    private final Map<String, Map<String, Number>> sourceIdCache;

    private OBSRemoteControllerBuilder builder;
    private OBSRemoteController obsRemote;
    private boolean ready = false;

    public OBS(String host, int port, String password) {
        sourceIdCache = new HashMap<>();
        builder =  OBSRemoteController.builder()
                .host(host)
                .port(port)
                .password(password)
                .lifecycle()
                    .onReady(this::onReady)
                    .and();
    }

    private void onReady() {
        ready = true;
    }

    public boolean isReady() {
        return ready;
    }

    public void registerSceneChangeEvent(Consumer<CurrentProgramSceneChangedEvent> consumer) {
        builder = builder.registerEventListener(CurrentProgramSceneChangedEvent.class, consumer);
    }

    public void init() {
        obsRemote = builder.build();
        obsRemote.connect();
    }

    public String getCurrentScene() {
        CompletableFuture<GetCurrentProgramSceneResponse> future = CompletableFuture.supplyAsync(
                () -> obsRemote.getCurrentProgramScene(TIMEOUT)
        );
        GetCurrentProgramSceneResponse response;
        try {
            response = future.get();
        } catch (Exception e) {
            log.error("exception fetching current scene");
            return null;
        }

        if (response == null || !response.isSuccessful()) {
            log.error("unable to fetch current scene");
            return null;
        }

        return response.getCurrentProgramSceneName();
    }

    public void close() {
        // can't quite figure out if this is the right way to do this, but it seems to work fine?
        obsRemote.stop();
    }

    public void changeScenes(String sceneName) {
        obsRemote.setCurrentProgramScene(sceneName, TIMEOUT);
    }

    public Number getSourceId(String sceneName, String sourceName) {
        // return cached sourceId if it exists
        Map<String, Number> sceneMap = sourceIdCache.get(sceneName);
        if (sceneMap == null) {
            sceneMap = new HashMap<>();
            sourceIdCache.put(sceneName, sceneMap);
        } else {
            Number sourceId = sceneMap.get(sourceName);
            if (sourceId != null) {
                return sourceId;
            }
        }

        // fetch sourceId from OBS otherwise
        CompletableFuture<GetSceneItemIdResponse> future = CompletableFuture.supplyAsync(
                () -> obsRemote.getSceneItemId(sceneName, sourceName, 0, TIMEOUT)
        );
        GetSceneItemIdResponse response;
        try {
            response = future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("exception fetching source \"{}, {}\"", sceneName, sourceName);
            return null;
        }

        if (!response.isSuccessful()) {
            log.error("unable to fetch source \"{}, {}\"", sceneName, sourceName);
            return null;
        }
        // cache sourceId
        sceneMap.put(sourceName, response.getSceneItemId());
        return response.getSceneItemId();
    }

    public Boolean getSourceEnabled(String sceneName, String sourceName) {
        Number sourceId = getSourceId(sceneName, sourceName);
        if (sourceId == null) {
            return false;
        }

        return getSourceEnabled(sceneName, sourceId);
    }

    private Boolean getSourceEnabled(String sceneName, Number sourceId) {
        CompletableFuture<GetSceneItemEnabledResponse> future = CompletableFuture.supplyAsync(
                () -> obsRemote.getSceneItemEnabled(sceneName, sourceId, TIMEOUT)
        );

        GetSceneItemEnabledResponse response;
        try {
            response = future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("exception fetching enabled state of source \"{}, {}\"", sceneName, sourceId);
            return null;
        }

        if (!response.isSuccessful()) {
            log.error("unable to fetch enabled state of source \"{}, {}\"", sceneName, sourceId);
            return null;
        }
        return response.getSceneItemEnabled();
    }

    public void setSourceEnabled(String sceneName, String sourceName, boolean enabled) {
        Number sourceId = getSourceId(sceneName, sourceName);
        if (sourceId == null) {
            log.error("unable to get source ID for \"{}, {}\" to enable/disable", sceneName, sourceName);
            return;
        }

        obsRemote.setSceneItemEnabled(sceneName, sourceId, enabled, TIMEOUT);
    }

    public void toggleSourceEnabled(String sceneName, String sourceName) {
        Number sourceId = getSourceId(sceneName, sourceName);
        if (sourceId == null) {
            log.error("unable to get source ID for \"{}, {}\" to toggle enabled state", sceneName, sourceName);
            return;
        }
        Boolean enabled = getSourceEnabled(sceneName, sourceId);
        if (enabled == null) {
            log.error("unable to toggle unknown enabled state of \"{}, {}\"", sceneName, sourceName);
            return;
        }

        obsRemote.setSceneItemEnabled(sceneName, sourceId, !enabled, TIMEOUT);
    }

    public Boolean getAudioSourceMuted(String sourceName) {
        CompletableFuture<GetInputMuteResponse> future = CompletableFuture.supplyAsync(
                () -> obsRemote.getInputMute(sourceName, TIMEOUT)
        );

        GetInputMuteResponse response;
        try {
            response = future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("exception fetching audio source \"{}\"", sourceName);
            return null;
        }

        if (!response.isSuccessful()) {
            log.error("unable to fetch audio source \"{}\"", sourceName);
            return null;
        }
        return response.getInputMuted();
    }

    public void setAudioSourceMuted(String sourceName, boolean muted) {
        obsRemote.setInputMute(sourceName, muted, TIMEOUT);
    }

    public void toggleAudioSourceMuted(String sourceName) {
        Boolean muted = getAudioSourceMuted(sourceName);
        if (muted == null) {
            log.error("unable to toggle unknown muted state of audio source \"{}\"", sourceName);
            return;
        }
        setAudioSourceMuted(sourceName, !muted);
    }

    private GetSourceFilterResponse getSourceFilter(String sourceName, String filterName) {
        CompletableFuture<GetSourceFilterResponse> future = CompletableFuture.supplyAsync(
                () -> obsRemote.getSourceFilter(sourceName, filterName, TIMEOUT)
        );

        GetSourceFilterResponse response;
        try {
            response = future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("exception fetching source filter \"{}, {}\"", sourceName, filterName);
            return null;
        }

        if (!response.isSuccessful()) {
            log.error("unable to fetch source filter \"{}, {}\"", sourceName, filterName);
            return null;
        }
        return response;
    }

    public Boolean getSourceFilterEnabled(String sourceName, String filterName) {
        GetSourceFilterResponse sourceFilter = getSourceFilter(sourceName, filterName);
        if (sourceFilter == null) {
            log.error("unable to fetch enabled state of source filter \"{}, {}\"", sourceName, filterName);
            return null;
        }
        return sourceFilter.getFilterEnabled();
    }

    public void setSourceFilterEnabled(String sourceName, String filterName, boolean enabled) {
        obsRemote.setSourceFilterEnabled(sourceName, filterName, enabled, TIMEOUT);
    }

    private SceneItem.Transform getSourceTransform(String sceneName, Number sourceId) {
        CompletableFuture<GetSceneItemTransformResponse> future = CompletableFuture.supplyAsync(
                () -> obsRemote.getSceneItemTransform(sceneName, sourceId, TIMEOUT)
        );

        GetSceneItemTransformResponse response;
        try {
            response = future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("exception fetching source transform \"{}, {}\"", sceneName, sourceId);
            return null;
        }

        if (!response.isSuccessful()) {
            log.error("unable to fetch source transform \"{}, {}\"", sceneName, sourceId);
            return null;
        }
        return response.getSceneItemTransform();
    }

    private void setSourceTransform(String sceneName, Number sourceId, SceneItem.Transform transform) {
        obsRemote.setSceneItemTransform(sceneName, sourceId, transform, TIMEOUT);
    }

    public AlertFuture moveSource(
            String sceneName,
            String sourceName,
            float x,
            float y,
            int frames,
            boolean relative
    ) {
        Number sourceId = getSourceId(sceneName, sourceName);
        SceneItem.Transform sourceTransform = getSourceTransform(sceneName, sourceId);
        Float startX = sourceTransform.getPositionX();
        Float startY = sourceTransform.getPositionY();
        float endX = relative ? startX + x : x;
        float endY = relative ? startY + y : y;

        // move instantly, run callback, return
        if (frames == 0) {
            sourceTransform.setPositionX(endX);
            sourceTransform.setPositionY(endY);
            setSourceTransform(sceneName, sourceId, sourceTransform);
            return AlertFuture.getCompletedFuture();
        }

        // move over time
        float interX = (endX - startX) / frames;
        float interY = (endY - startY) / frames;
        Queue<Float> queueX = new ArrayDeque<>();
        Queue<Float> queueY = new ArrayDeque<>();
        for (int i = 1; i < frames; i++) {
            queueX.add(startX + interX * i);
            queueY.add(startY + interY * i);
        }
        queueX.add(endX);
        queueY.add(endY);

        AlertFuture future = new AlertFuture();
        Controller.getScheduler().scheduleAtFixedRate(new AlertTask() {
            @Override
            public void runTask() {
                sourceTransform.setPositionX(queueX.poll());
                sourceTransform.setPositionY(queueY.poll());
                setSourceTransform(sceneName, sourceId, sourceTransform);
                if (queueX.isEmpty()) {
                    this.cancel();
                    future.complete();
                }
            }
        }, 0, 1000 / FRAMERATE, TimeUnit.MILLISECONDS);
        return future;
    }

    // degrees, can be kind of buggy with bigger ranges
    public AlertFuture rotateSource(String sceneName, String sourceName, float rotation, int frames, boolean relative) {
        Number sourceId = getSourceId(sceneName, sourceName);
        SceneItem.Transform sourceTransform = getSourceTransform(sceneName, sourceId);
        if (sourceTransform == null) {
            log.error("unable to rotate source \"{}, {}\", null transform data", sceneName, sourceName);
            return AlertFuture.getCompletedFuture();
        }
        Float start = sourceTransform.getRotation();
        float end = relative ? start + rotation : rotation;

        // rotate instantly, run callback, return
        if (frames == 0) {
            sourceTransform.setRotation(end);
            setSourceTransform(sceneName, sourceId, sourceTransform);
            return AlertFuture.getCompletedFuture();
        }

        // rotate over time
        float inter = (end - start) / frames;
        Queue<Float> queue = new ArrayDeque<>();
        for (int i = 1; i < frames; i++) {
            queue.add(start + inter * i);
        }
        queue.add(end);

        AlertFuture future = new AlertFuture();
        Controller.getScheduler().scheduleAtFixedRate(new AlertTask() {
            @Override
            public void runTask() {
                sourceTransform.setRotation(queue.poll());
                setSourceTransform(sceneName, sourceId, sourceTransform);
                if (queue.isEmpty()) {
                    this.cancel();
                    future.complete();
                }
            }
        }, 0, 1000 / FRAMERATE, TimeUnit.MILLISECONDS);
        return future;
    }

    public Float getSourceFilterOpacity(String sourceName, String filterName) {
        GetSourceFilterResponse sourceFilter = getSourceFilter(sourceName, filterName);
        if (sourceFilter == null) {
            log.error("unable to fetch opacity of source filter \"{}, {}\"", sourceName, filterName);
            return null;
        }
        return sourceFilter.getFilterSettings().get("opacity").getAsFloat();
    }

    // 0 to 1.0f
    public AlertFuture setOpacity(String sourceName, String filterName, float opacity, int frames) {
        JsonObject settings = new JsonObject();

        // set instantly, run callback, return
        if (frames == 0) {
            settings.addProperty("opacity", opacity);
            obsRemote.setSourceFilterSettings(sourceName, filterName, settings, true, TIMEOUT);
            return AlertFuture.getCompletedFuture();
        }

        // change over time
        Float start = getSourceFilterOpacity(sourceName, filterName);
        float inter = (opacity - start) / frames;
        Queue<Float> queue = new ArrayDeque<>();
        for (int i = 1; i < frames; i++) {
            queue.add(start + inter * i);
        }
        queue.add(opacity);

        AlertFuture future = new AlertFuture();
        Controller.getScheduler().scheduleAtFixedRate(new AlertTask() {
            @Override
            public void runTask() {
                settings.addProperty("opacity", queue.poll());
                obsRemote.setSourceFilterSettings(sourceName, filterName, settings, true, TIMEOUT);
                if (queue.isEmpty()) {
                    this.cancel();
                    future.complete();
                }
            }
        }, 0, 1000 / FRAMERATE, TimeUnit.MILLISECONDS);
        return future;
    }
}
