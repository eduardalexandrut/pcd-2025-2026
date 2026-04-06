package pcd.poool.model;

public class PhysicsWorker extends Thread {
    private final Physics physics;
    private final PhaseLatch barrier;
    private final int startRow, endRow;
    private final long dt;

    public PhysicsWorker(Physics physics, PhaseLatch barrier, int startRow, int endRow, long dt) {
        this.physics = physics;
        this.barrier = barrier;
        this.startRow = startRow;
        this.endRow = endRow;
        this.dt = dt;
    }

    @Override
    public void run() {
        for (int r = startRow; r <= endRow; r++) {
            this.physics.resolveRowCollisions(r);
        }

        barrier.awaitPhase();

        for (int r = startRow; r <= endRow; r++) {
            this.physics.updateRowMovement(r, dt);
        }
    }
}
