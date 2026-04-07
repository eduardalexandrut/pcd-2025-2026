package pcd.poool.model;

public class PhysicsWorker extends Thread {
    private final Physics physics;
    private final int startRow, endRow;
    private final long dt;

    public PhysicsWorker(Physics physics, int startRow, int endRow, long dt) {
        this.physics = physics;
        this.startRow = startRow;
        this.endRow = endRow;
        this.dt = dt;
    }

    @Override
    public void run() {
        // Phase 1: resolve collisions for my rows
        for (int r = startRow; r <= endRow; r++) {
            physics.resolveRowCollisions(r);
            // Signal all cells in row r AND row r+1 boundary that I'm done touching them
            physics.signalCollisionsDoneForRow(r);
        }

        // Phase 2: update movement — each cell blocks until all collision workers are done
        for (int r = startRow; r <= endRow; r++) {
            physics.updateRowMovement(r, dt);
        }

    }
}
