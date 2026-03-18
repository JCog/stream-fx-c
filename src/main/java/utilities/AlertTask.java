package utilities;

public abstract class AlertTask implements Runnable {
    private boolean canceled;

    protected AlertTask() {
        canceled = false;
    }

    public abstract void runTask();

    @Override
    public void run() {
        runTask();
        if (canceled) {
            // kind of a hack, but this seems to be the only simple way to cancel a scheduled task from within that task
            // when using ScheduledExecutorService. at least like this the ugly exception is hidden away lol.
            throw new RuntimeException();
        }
    }

    public void cancel() {
        canceled = true;
    }
}
