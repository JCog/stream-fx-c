package interfaces;

import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.event.scenes.CurrentProgramSceneChangedEvent;
import io.obswebsocket.community.client.message.response.inputs.GetInputMuteResponse;
import io.obswebsocket.community.client.message.response.sceneitems.GetSceneItemEnabledResponse;
import io.obswebsocket.community.client.message.response.sceneitems.GetSceneItemIdResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class OBS {
    private static final int TIMEOUT = 1000;
    private final OBSRemoteController obsRemote;

    private boolean ready = false;
    private String sceneCurrent = null;
    private String scenePrev = null;

    public OBS(String host, int port, String password) {
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

    public boolean isReady() {
        return ready;
    }

    public void init() {
        ready = true;
        obsRemote.getCurrentProgramScene(getCurrentProgramSceneResponse -> {
            if (getCurrentProgramSceneResponse.isSuccessful()) {
                sceneCurrent = getCurrentProgramSceneResponse.getCurrentProgramSceneName();
            }
        });
    }

    public void changeScenes(String sceneName) {
        obsRemote.setCurrentProgramScene(sceneName, TIMEOUT);
    }

    public Number getSourceId(String sceneName, String sourceName) {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void onSceneChanged(CurrentProgramSceneChangedEvent event) {
        System.out.println(event.getSceneName());
        scenePrev = sceneCurrent;
        sceneCurrent = event.getSceneName();
    }
}
