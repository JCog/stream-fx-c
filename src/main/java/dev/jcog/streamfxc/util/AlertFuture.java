package dev.jcog.streamfxc.util;

public class AlertFuture {
    private volatile boolean waiting;

    public AlertFuture() {
        waiting = true;
    }

    public void complete() {
        waiting = false;
    }

    public void block() {
        while (waiting) {
            Thread.onSpinWait();
        }
    }

    public static AlertFuture getCompletedFuture() {
        AlertFuture future = new AlertFuture();
        future.complete();
        return future;
    }
}
