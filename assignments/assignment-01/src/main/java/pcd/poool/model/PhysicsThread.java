package pcd.poool.model;

public class PhysicsThread extends Thread {
    private final Physics model;
    private final long period = 20; // 20ms = 50 FPS

    public PhysicsThread(Physics model) {
        this.model = model;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            long start = System.currentTimeMillis();

            // 1. Update the math (Move balls, check collisions)
            model.computeState(period);

            // 2. Regulate the speed so it doesn't run too fast
            long used = System.currentTimeMillis() - start;
            long sleep = Math.max(0, period - used);

            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
