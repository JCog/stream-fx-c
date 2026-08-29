package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.interfaces.obs.Filter;
import dev.jcog.streamfxc.interfaces.obs.Source;
import dev.jcog.streamfxc.util.AudioFile;

public class OuttaSight extends Alert {
    private static final String ID = "Outta Sight";
    private static final long LENGTH_MILLIS = 30 * 1000;
    private static final float HOME_X = -123;
    private static final float HOME_Y = 888;
    private static final float TARGET_X = 144;
    private static final float TARGET_Y = 822;

    private final Source sourceBow = new Source("Alerts - Outta Sight", "Bow");
    private final Filter filterBowCC = new Filter("Bow", "Color Correction");
    private final Filter filterOuttaSight = new Filter("DSLR (chroma key)", "Outta Sight");
    private final AudioFile audioStart = new AudioFile("res/outta_sight_start.wav");
    private final AudioFile audioEnd = new AudioFile("res/outta_sight_end.wav");

    public OuttaSight() {
        super(ID);
    }

    @Override
    protected void onTrigger() {
        sourceBow.enable();
        sourceBow.moveAbsolute(TARGET_X, TARGET_Y, 60).block();

        audioStart.playClip();
        filterOuttaSight.enable();
        filterOuttaSight.setOpacity(0.9f, 30);
        filterBowCC.setOpacity(0.15f, 30);

        waitFromNow(LENGTH_MILLIS);
        audioEnd.playClip();
        filterOuttaSight.disable();
        filterOuttaSight.setOpacity(1.0f);
        filterBowCC.setOpacity(1.0f);
        sourceBow.moveAbsolute(HOME_X, HOME_Y, 60).block();

        waitFromNow(500);
    }
}
