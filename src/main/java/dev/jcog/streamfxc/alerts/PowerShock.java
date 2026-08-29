package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.interfaces.obs.AudioSource;
import dev.jcog.streamfxc.interfaces.obs.Filter;
import dev.jcog.streamfxc.interfaces.obs.Source;
import dev.jcog.streamfxc.misc.Controller;
import dev.jcog.streamfxc.util.AudioFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PowerShock extends Alert {
    private static final Logger log = LoggerFactory.getLogger(PowerShock.class);
    private static final String ID = "Power Shock";

    private static final String AUDIO_MIC = "Mic";

    private static final String SCENE_ALERTS = "Alerts - Power Shock";
    private static final String SCENE_DSLR_COMMON = "Common - DSLR";

    private static final float WATT_HOME_X = -210f;
    private static final float WATT_HOME_Y = 780f;
    private static final float DINK_HOME_X = 150f;
    private static final float DINK_HOME_Y = 915f;
    private static final float NICE_HOME_X = 50f;
    private static final float NICE_HOME_Y = 800f;
    private static final long SHOCK_START = 4200;
    private static final long SUCCESS_LENGTH_BITS = 30 * 1000 + SHOCK_START;
    private static final long SUCCESS_LENGTH_OTHER = 10 * 1000 + SHOCK_START;

    private final AudioSource audioMic = new AudioSource(AUDIO_MIC);
    private final Source sourceWattSuccess = new Source(SCENE_ALERTS, "Watt Success");
    private final Source sourceWattFailure = new Source(SCENE_ALERTS, "Watt Failure");
    private final Source sourceDslr = new Source(SCENE_DSLR_COMMON, "DSLR (chroma key)");
    private final Source sourceShockedIcon = new Source(SCENE_ALERTS, "Shocked Icon");
    private final Source sourceDinkIcon = new Source(SCENE_ALERTS, "Dink Icon");
    private final Source sourceNiceIcon = new Source(SCENE_ALERTS, "Nice Icon");
    private final Filter filterShockedIconCC = sourceShockedIcon.getFilter("Color Correction");
    private final Filter filterDinkIconCC = sourceDinkIcon.getFilter("Color Correction");
    private final Filter filterDslrShock = new Filter("DSLR", "Power Shock");
    private final Filter filterDslrFreeze = new Filter("DSLR", "Freeze");
    private final AudioFile finishClip = new AudioFile("res/ding.wav");
    private final Random random = new Random();

    private int failureStreak;

    public PowerShock() {
        super(ID);
        failureStreak = 0;
    }

    @Override
    protected void onTrigger() {
        boolean success = failureStreak >= 2 || random.nextFloat() < 0.6f;
        Source sourceWatt;
        long successLength;
        if (success) {
            failureStreak = 0;
            sourceWatt = sourceWattSuccess;
            successLength = getTriggerSource() == TriggerSource.BITS ? SUCCESS_LENGTH_BITS : SUCCESS_LENGTH_OTHER;
        } else {
            failureStreak++;
            sourceWatt = sourceWattFailure;
            successLength = 0;
        }
        sourceWatt.moveAbsolute(WATT_HOME_X, WATT_HOME_Y);
        sourceWatt.enable();
        sourceWatt.moveAbsolute(0, 780, 60);

        waitUntil(SHOCK_START);
        if (success) {
            log.info("success");
            audioMic.mute();

            // shocked icon
            sourceShockedIcon.enable();
            filterShockedIconCC.setOpacity(1f, 30);

            // NICE text
            sourceNiceIcon.moveAbsolute(NICE_HOME_X, NICE_HOME_Y);
            sourceNiceIcon.enable();
            sourceNiceIcon.moveRelative(-30, -100, 20);

            // camera shake/flash
            filterDslrFreeze.enable();
            Controller.getScheduler().schedule(() -> flashCameraLoop(successLength), 0, TimeUnit.MILLISECONDS);
            Controller.getScheduler().schedule(() -> shakeCameraLoop(successLength), 0, TimeUnit.MILLISECONDS);
        } else {
            log.info("failure");

            // dink icon
            sourceDinkIcon.moveAbsolute(DINK_HOME_X, DINK_HOME_Y);
            filterDinkIconCC.setOpacity(1f);
            sourceDinkIcon.enable();

            sourceDinkIcon.moveRelative(300, -100, 25).block();
            filterDinkIconCC.setOpacity(0, 30).block();
            sourceDinkIcon.disable();
        }

        waitUntil(7000);
        sourceWatt.moveAbsolute(WATT_HOME_X, WATT_HOME_Y, 60);

        waitUntil(9500);
        sourceWatt.disable();
        waitFromNow(2000);

        if (success) {
            waitUntil(successLength);
            filterDslrFreeze.disable();
            audioMic.unmute();
            finishClip.playClip();
            filterShockedIconCC.setOpacity(0f, 30).block();
            sourceShockedIcon.disable();
            sourceNiceIcon.disable();
        }
    }

    private void oneShake() {
        sourceDslr.moveRelative(0, 3);
        waitFromNow(33);
        sourceDslr.moveRelative(0, -6);
        waitFromNow(33);
        sourceDslr.moveRelative(0, 3);
    }

    private void shakeCameraLoop(long successLength) {
        long length = 3500;
        while (elapsedMillis() < successLength - length) {
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
        filterDslrShock.enable();
        waitFromNow(33);
        filterDslrShock.disable();
        waitFromNow(33);
    }

    private void flashCameraLoop(long successLength) {
        long length = 2000;
        while (elapsedMillis() < successLength - length) {
            long start = elapsedMillis();
            flash();
            flash();
            waitUntil(start + length);
        }
    }
}
