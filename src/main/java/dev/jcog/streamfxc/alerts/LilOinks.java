package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.interfaces.obs.Source;
import dev.jcog.streamfxc.util.AudioFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class LilOinks extends Alert {
    private static final Logger log = LoggerFactory.getLogger(LilOinks.class);
    private static final String ID = "Li'l Oinks";
    private static final String SCENE_ID = "Alerts - Oinks";
    private static final String SOURCE_ID_START = "Oinks - ";

    private static final int HOME_X = -128;
    private static final int HOLD_X = 64;
    private static final int DEST_X = 1920;
    private static final int OINK_Y = 970;

    private final Source sourceBlack = getOinkSource("Black");
    private final Source sourceFlower = getOinkSource("Flower");
    private final Source sourceGold = getOinkSource("Gold");
    private final Source sourceMushroom = getOinkSource("Mushroom");
    private final Source sourcePink = getOinkSource("Pink");
    private final Source sourceQuestionMark = getOinkSource("Question Mark");
    private final Source sourceSilver = getOinkSource("Silver");
    private final Source sourceStar = getOinkSource("Star");
    private final Source sourceTiger = getOinkSource("Tiger");
    private final Source sourceWhite = getOinkSource("White");

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
        log.info("\"{}\" selected", oink.getSourceName().substring(SOURCE_ID_START.length()));

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

    private Source getOinkSource(String type) {
        return new Source(SCENE_ID, SOURCE_ID_START + type);
    }
}
