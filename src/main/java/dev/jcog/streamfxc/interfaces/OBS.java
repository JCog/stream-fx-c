package dev.jcog.streamfxc.interfaces;

import dev.jcog.streamfxc.misc.Controller;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.event.scenes.CurrentProgramSceneChangedEvent;
import io.obswebsocket.community.client.message.response.filters.GetSourceFilterResponse;
import io.obswebsocket.community.client.message.response.inputs.GetInputMuteResponse;
import io.obswebsocket.community.client.message.response.sceneitems.GetSceneItemEnabledResponse;
import io.obswebsocket.community.client.message.response.sceneitems.GetSceneItemIdResponse;
import io.obswebsocket.community.client.message.response.sceneitems.GetSceneItemTransformResponse;
import io.obswebsocket.community.client.model.SceneItem;
import dev.jcog.streamfxc.util.AlertFuture;
import dev.jcog.streamfxc.util.AlertTask;

import java.util.*;
import java.util.concurrent.*;

public class OBS {
    private static final int TIMEOUT = 1000;
    private static final int FRAMERATE = 60;

    private final OBSRemoteController obsRemote;
    private final Map<String, Map<String, Number>> sourceIdCache;

    private boolean ready = false;
    private String sceneCurrent = null;
    private String scenePrev = null;

    public OBS(String host, int port, String password) {
        sourceIdCache = new HashMap<>();
        obsRemote = OBSRemoteController.builder()
                .host(host)
                .port(port)
                .password(password)
                .lifecycle()
                    .onReady(this::init)
                    .and()
                .registerEventListener(CurrentProgramSceneChangedEvent.class, this::onSceneChanged)
                .build();
        obsRemote.connect();
    }

    public void init() {
        ready = true;
        obsRemote.getCurrentProgramScene(getCurrentProgramSceneResponse -> {
            if (getCurrentProgramSceneResponse.isSuccessful()) {
                sceneCurrent = getCurrentProgramSceneResponse.getCurrentProgramSceneName();
            }
        });
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
            return null;
        }

        if (!response.isSuccessful()) {
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

    public Boolean getSourceEnabled(String sceneName, Number sourceId) {
        CompletableFuture<GetSceneItemEnabledResponse> future = CompletableFuture.supplyAsync(
                () -> obsRemote.getSceneItemEnabled(sceneName, sourceId, TIMEOUT)
        );

        GetSceneItemEnabledResponse response;
        try {
            response = future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }

        if (!response.isSuccessful()) {
            return null;
        }
        return response.getSceneItemEnabled();
    }

    public void setSourceEnabled(String sceneName, String sourceName, boolean enabled) {
        Number sourceId = getSourceId(sceneName, sourceName);
        if (sourceId == null) {
            return;
        }

        obsRemote.setSceneItemEnabled(sceneName, sourceId, enabled, TIMEOUT);
    }

    public void setSourceEnabled(String sceneName, Number sourceId, boolean enabled) {
        obsRemote.setSceneItemEnabled(sceneName, sourceId, enabled, TIMEOUT);
    }

    public void toggleSourceEnabled(String sceneName, String sourceName) {
        Number sourceId = getSourceId(sceneName, sourceName);
        if (sourceId == null) {
            return;
        }
        Boolean enabled = getSourceEnabled(sceneName, sourceId);
        if (enabled == null) {
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
            return null;
        }

        if (!response.isSuccessful()) {
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
            return;
        }
        setAudioSourceMuted(sourceName, !muted);
    }

    public Boolean getSourceFilterEnabled(String sourceName, String filterName) {
        CompletableFuture<GetSourceFilterResponse> future = CompletableFuture.supplyAsync(
                () -> obsRemote.getSourceFilter(sourceName, filterName, TIMEOUT)
        );

        GetSourceFilterResponse response;
        try {
            response = future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }

        if (!response.isSuccessful()) {
            return null;
        }
        return response.getFilterEnabled();
    }

    public void setSourceFilterEnabled(String sourceName, String filterName, boolean enabled) {
        obsRemote.setSourceFilterEnabled(sourceName, filterName, enabled, TIMEOUT);
    }

    public SceneItem.Transform getSourceTransform(String sceneName, Number sourceId) {
        CompletableFuture<GetSceneItemTransformResponse> future = CompletableFuture.supplyAsync(
                () -> obsRemote.getSceneItemTransform(sceneName, sourceId, TIMEOUT)
        );

        GetSceneItemTransformResponse response;
        try {
            response = future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }

        if (!response.isSuccessful()) {
            return null;
        }
        return response.getSceneItemTransform();
    }

    public void setSourceTransform(String sceneName, Number sourceId, SceneItem.Transform transform) {
        obsRemote.setSceneItemTransform(sceneName, sourceId, transform, TIMEOUT);
    }

    public AlertFuture moveSource(
            String sceneName,
            Number sourceId,
            float x,
            float y,
            int frames,
            boolean relative
    ) {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void onSceneChanged(CurrentProgramSceneChangedEvent event) {
        scenePrev = sceneCurrent;
        sceneCurrent = event.getSceneName();
    }
}
