package dev.jcog.streamfxc.interfaces.obs;

import dev.jcog.streamfxc.interfaces.OBS;
import dev.jcog.streamfxc.misc.Controller;
import dev.jcog.streamfxc.util.AlertFuture;

public class Filter {
    private static final OBS obs = Controller.getObs();

    private final String sourceName;
    private final String filterName;

    public Filter(String sourceName, String filterName) {
        this.sourceName = sourceName;
        this.filterName = filterName;
    }

    public boolean isEnabled() {
        return obs.getSourceFilterEnabled(sourceName, filterName);
    }

    public void enable() {
        obs.setSourceFilterEnabled(sourceName, filterName, true);
    }

    public void disable() {
        obs.setSourceFilterEnabled(sourceName, filterName, false);
    }

    public AlertFuture setOpacity(float opacity) {
        return obs.setOpacity(sourceName, filterName, opacity, 0);
    }

    public AlertFuture setOpacity(float opacity, int frames) {
        return obs.setOpacity(sourceName, filterName, opacity, frames);
    }
}
