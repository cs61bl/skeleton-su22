/**
 * Timer.java implements a simple stopwatch/timer type class based on Java
 * wall-clock time. All times are given in units of msec.
 */
public class Timer {

    private boolean running;
    private long tStart;
    private long tFinish;
    private long tAccum;

    /** Initializes Timer to 0 msec */
    public Timer() {
        reset();
    }

    /** Starts the timer. Accumulates time across multiple calls to start. */
    public void start() {
        running = true;
        tStart = System.currentTimeMillis();
        tFinish = tStart;
    }

    /**
     * Stops the timer. Returns the time elapsed since the last matching call
     * to start(), or 0 if no such matching call was made.
     */
    public long stop() {
        tFinish = System.currentTimeMillis();
        if (running) {
            running = false;
            long diff = tFinish - tStart;
            tAccum += diff;
            return diff;
        }
        return 0;
    }

    /**
     * If running, returns the time since last call to start(). Else, returns
     * total elapsed time
     */
    public long elapsed() {
        if (running) {
            return System.currentTimeMillis() - tStart;
        }
        return tAccum;
    }

    /** Stops timing if currently running and resets accumulated time to 0. */
    public void reset() {
        running = false;
        tStart = tFinish = 0;
        tAccum = 0;
    }
}