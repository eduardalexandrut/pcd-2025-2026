package pcd.poool.model;

public class PhysicsThread extends Thread {
    private final Physics model;
    private final long period = 20; // 20ms = 50 FPS

    public PhysicsThread(Physics model) {
        this.model = model;
    }

    @Override
    public void run() {
        // Inside your Physics Thread / Simulation Loop
        long lastTime = System.nanoTime();
        double fps = 0;

        while (!isInterrupted() && this.model.getGameState() == PhysicsImpl.GameState.RUNNING) {
            long now = System.nanoTime();
            // Calculate the time elapsed in nanoseconds
            long updateTime = now - lastTime;
            lastTime = now;

            // Convert nanoseconds to seconds, then invert for FPS
            // We use a simple Alpha-Smoothing filter to stop the number from flickering
            double currentFps = 1_000_000_000.0 / updateTime;
            fps = (fps * 0.9) + (currentFps * 0.1);

            long start = System.currentTimeMillis();

            // 1. Update the math (Move balls, check collisions)
            model.computeState(period);

            checkEndGame();

            model.setFPS((int) fps);

            // 2. Regulate the speed so it doesn't run too fast
            long used = System.currentTimeMillis() - start;
            long sleep = Math.max(0, period - used);

            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                break;
            }
        }

        System.out.println("Simulation Ended. Final State: " + model.getGameState());
    }

    private void checkEndGame() {
        if (this.model.getGameState() != PhysicsImpl.GameState.RUNNING) {
            return;
        }
        if (this.model.getStateSnapshot().size() <= 2) {
            final int userScore = this.model.getUserScore();
            final int npcScore = this.model.getNPCScore();

            if (userScore > npcScore) {
                this.model.setGameState(PhysicsImpl.GameState.USER_WON);
            } else if (userScore < npcScore) {
                this.model.setGameState(PhysicsImpl.GameState.NPC_WON);
            } else {
                this.model.setGameState(PhysicsImpl.GameState.DRAW);
            }
        }
    }
}
