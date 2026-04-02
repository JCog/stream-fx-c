package dev.jcog.streamfxc;

import dev.jcog.streamfxc.misc.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Controller controller = new Controller();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stream FX C stopping...");
            controller.closeAll();
            log.info("Stream FX C stopped.");
        }));

        boolean quit = controller.listen();
        if (quit) {
            System.exit(0);
        }
    }
}
