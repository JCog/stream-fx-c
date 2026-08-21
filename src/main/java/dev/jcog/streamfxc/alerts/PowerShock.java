package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.interfaces.OBS;
import dev.jcog.streamfxc.misc.Controller;
import dev.jcog.streamfxc.util.AudioFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PowerShock extends Alert {
    private static final Logger log = LoggerFactory.getLogger(PowerShock.class);
    private static final String ID = "Power Shock";
    private static final String SCENE_ALERTS = "Alerts";
    private static final String SCENE_DSLR_COMMON = "Common - DSLR";

    private static final String SOURCE_WATT_SUCCESS = "Watt Success";
    private static final String SOURCE_WATT_FAILURE = "Watt Failure";
    private static final String SOURCE_DSLR_BASE = "DSLR";
    private static final String SOURCE_DSLR_SHAKE = "DSLR (chroma key)";
    private static final String SOURCE_ICON_SHOCKED = "Shocked Icon";

    private static final String FILTER_SHOCK = "Power Shock";
    private static final String FILTER_FREEZE = "Freeze";
    private static final String FILTER_ICON_CC = "Color Correction";

    private static final float WATT_HOME_X = -210f;
    private static final float WATT_HOME_Y = 780f;
    private static final long SHOCK_START = 4200;
    private static final long SUCCESS_LENGTH = 10000 + SHOCK_START;

    private final OBS obs;
    private final AudioFile finishClip;
    private final Random random;

    public PowerShock(OBS obs, String finishFilename) {
        super(ID);
        this.obs = obs;
        finishClip = new AudioFile(finishFilename);
        random = new Random();
    }

    @Override
    protected void onTrigger() {
        boolean success = random.nextBoolean();
        Number sourceWatt;
        if (success) {
            log.info("success");
            sourceWatt = obs.getSourceId(SCENE_ALERTS, SOURCE_WATT_SUCCESS);
        } else {
            log.info("failure");
            sourceWatt = obs.getSourceId(SCENE_ALERTS, SOURCE_WATT_FAILURE);
        }
        obs.moveSource(SCENE_ALERTS, sourceWatt, WATT_HOME_X, WATT_HOME_Y, 0, false);
        obs.setSourceEnabled(SCENE_ALERTS, sourceWatt, true);
        obs.moveSource(SCENE_ALERTS, sourceWatt, 0, 780, 60, false);

        waitUntil(SHOCK_START);
        if (success) {
            obs.setSourceEnabled(SCENE_ALERTS, SOURCE_ICON_SHOCKED, true);
            obs.setOpacity(SOURCE_ICON_SHOCKED, FILTER_ICON_CC, 1f, 30);

            obs.setSourceFilterEnabled(SOURCE_DSLR_BASE, FILTER_FREEZE, true);
            Controller.getScheduler().schedule(this::flashCameraLoop, 0, TimeUnit.MILLISECONDS);
            Controller.getScheduler().schedule(this::shakeCameraLoop, 0, TimeUnit.MILLISECONDS);
        }

        waitUntil(7000);
        obs.moveSource(SCENE_ALERTS, sourceWatt, WATT_HOME_X, WATT_HOME_Y, 60, false);

        waitUntil(9500);
        obs.setSourceEnabled(SCENE_ALERTS, sourceWatt, false);
        waitFromNow(2000);

        if (success) {
            waitUntil(SUCCESS_LENGTH);
            obs.setSourceFilterEnabled(SOURCE_DSLR_BASE, FILTER_FREEZE, false);
            finishClip.playClip();
            obs.setOpacity(SOURCE_ICON_SHOCKED, FILTER_ICON_CC, 0f, 30).block();
            obs.setSourceEnabled(SCENE_ALERTS, SOURCE_ICON_SHOCKED, false);
        }
    }

    private void oneShake() {
        Number sourceCamera = obs.getSourceId(SCENE_DSLR_COMMON, SOURCE_DSLR_SHAKE);
        obs.moveSource(SCENE_DSLR_COMMON, sourceCamera, 0, 3, 0, true);
        waitFromNow(33);
        obs.moveSource(SCENE_DSLR_COMMON, sourceCamera, 0, -6, 0, true);
        waitFromNow(33);
        obs.moveSource(SCENE_DSLR_COMMON, sourceCamera, 0, 3, 0, true);
    }

    private void shakeCameraLoop() {
        long length = 3500;
        while (elapsedMillis() < SUCCESS_LENGTH - length) {
            long start = elapsedMillis();
            oneShake();
            waitFromNow(67);
            oneShake();
            waitFromNow(133);
            oneShake();
            waitUntil(start + length);
        }
    }

    private void flash() {
        obs.setSourceFilterEnabled(SOURCE_DSLR_BASE, FILTER_SHOCK, true);
        waitFromNow(33);
        obs.setSourceFilterEnabled(SOURCE_DSLR_BASE, FILTER_SHOCK, false);
        waitFromNow(33);
    }

    private void flashCameraLoop() {
        long length = 2000;
        while (elapsedMillis() < SUCCESS_LENGTH - length) {
            long start = elapsedMillis();
            flash();
            flash();
            waitUntil(start + length);
        }
    }
}
