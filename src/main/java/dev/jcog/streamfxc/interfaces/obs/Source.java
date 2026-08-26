package dev.jcog.streamfxc.interfaces.obs;

import dev.jcog.streamfxc.interfaces.OBS;
import dev.jcog.streamfxc.misc.Controller;
import dev.jcog.streamfxc.util.AlertFuture;

public class Source {
    private static final OBS obs = Controller.getObs();

    private final String sceneName;
    private final String sourceName;

    public Source(String sceneName, String sourceName) {
        this.sceneName = sceneName;
        this.sourceName = sourceName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public boolean isEnabled() {
        return obs.getSourceEnabled(sceneName, sourceName);
    }

    public void enable() {
        obs.setSourceEnabled(sceneName, sourceName, true);
    }

    public void disable() {
        obs.setSourceEnabled(sceneName, sourceName, false);
    }

    public void toggleEnabled() {
        obs.toggleSourceEnabled(sceneName, sourceName);
    }

    public AlertFuture moveRelative(float x, float y) {
        return obs.moveSource(sceneName, sourceName, x, y, 0, true);
    }

    public AlertFuture moveRelative(float x, float y, int frames) {
        return obs.moveSource(sceneName, sourceName, x, y, frames, true);
    }

    public AlertFuture moveAbsolute(float x, float y) {
        return obs.moveSource(sceneName, sourceName, x, y, 0, false);
    }

    public AlertFuture moveAbsolute(float x, float y, int frames) {
        return obs.moveSource(sceneName, sourceName, x, y, frames, false);
    }

    public AlertFuture rotateRelative(float degrees, int frames) {
        return obs.rotateSource(sceneName, sourceName, degrees, frames, true);
    }

    public AlertFuture rotateAbsolute(float degrees, int frames) {
        return obs.rotateSource(sceneName, sourceName, degrees, frames, false);
    }

    public Filter getFilter(String filterName) {
        return new Filter(sourceName, filterName);
    }
}
