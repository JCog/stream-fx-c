package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.interfaces.obs.Source;
import dev.jcog.streamfxc.util.AudioFile;

import java.util.Random;

public class LilOinks extends Alert {
    private static final String ID = "Li'l Oinks";
    private static final String SCENE_ID = "Alerts - Oinks";

    private static final int HOME_X = -128;
    private static final int HOLD_X = 64;
    private static final int DEST_X = 1920;
    private static final int OINK_Y = 970;

    private final Source sourceBlack = new Source(SCENE_ID, "Oinks - Black");
    private final Source sourceFlower = new Source(SCENE_ID, "Oinks - Flower");
    private final Source sourceGold = new Source(SCENE_ID, "Oinks - Gold");
    private final Source sourceMushroom = new Source(SCENE_ID, "Oinks - Mushroom");
    private final Source sourcePink = new Source(SCENE_ID, "Oinks - Pink");
    private final Source sourceQuestionMark = new Source(SCENE_ID, "Oinks - Question Mark");
    private final Source sourceSilver = new Source(SCENE_ID, "Oinks - Silver");
    private final Source sourceStar = new Source(SCENE_ID, "Oinks - Star");
    private final Source sourceTiger = new Source(SCENE_ID, "Oinks - Tiger");
    private final Source sourceWhite = new Source(SCENE_ID, "Oinks - White");

    private final AudioFile audioTornadoJump = new AudioFile("res/tornado_jump.wav");
    private final Random random;

    public LilOinks() {
        super(ID);
        random = new Random();
    }

    @Override
    protected void onTrigger() {
        int rand = random.nextInt(101);
        Source oink;
        if (rand < 3) {
            oink = sourceGold;
        } else if (rand < 12) {
            oink = sourceSilver;
        } else if (rand < 19) {
            oink = sourceMushroom;
        } else if (rand < 26) {
            oink = sourceFlower;
        } else if (rand < 33) {
            oink = sourceStar;
        } else if (rand < 40) {
            oink = sourceQuestionMark;
        } else if (rand < 55) {
            oink = sourceBlack;
        } else if (rand < 70) {
            oink = sourceWhite;
        } else if (rand < 85) {
            oink = sourcePink;
        } else {
            oink = sourceTiger;
        }

        oink.moveAbsolute(HOME_X, OINK_Y);
        oink.enable();
        audioTornadoJump.playClip().block();
        waitFromNow(200);
        oink.moveAbsolute(HOLD_X, OINK_Y, 90).block();
        waitFromNow(2000);
        oink.moveAbsolute(DEST_X, OINK_Y, 15 * 60).block();
        oink.disable();
        waitFromNow(500);
    }
}
