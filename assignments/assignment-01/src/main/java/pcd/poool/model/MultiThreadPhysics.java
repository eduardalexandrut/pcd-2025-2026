package pcd.poool.model;

public class MultiThreadPhysics extends AbstractPhysics implements Physics {
    public enum GameState {
        RUNNING,
        USER_WON,
        NPC_WON,
        DRAW,
        STOPPED
    }

    private SimpleBarrier barrier;
    private PhysicsWorker[] workers;

    public MultiThreadPhysics(Board board, int rows, int cols) {
        super(board, rows, cols);

        this.transferToCorrectCell(this.npcBall);
        this.transferToCorrectCell(this.userBall);

        this.syncBoard(board);

        int nThreads = Runtime.getRuntime().availableProcessors();
        this.workers = new PhysicsWorker[nThreads];
        final int rowsPerThread = this.rows / nThreads;

        this.barrier = new SimpleBarrier(nThreads + 1);

        // Init worker threads
        for (int i = 0; i < nThreads; i++) {
            int start = i * rowsPerThread;
            int end = (i == nThreads - 1) ? rows - 1 : (start + rowsPerThread - 1);

            workers[i] = new PhysicsWorker(this, start, end, barrier);
            workers[i].start();
        }

    }

    @Override
    protected void runParallelStep(long dt) {
        for (PhysicsWorker w : workers) {
            w.setDt(dt);
        }

        barrier.await();
        barrier.await();
    }

    private void syncBoard(final Board board) {
        this.npcBrain.start();

        for (final Ball ball : board.getBalls()) {
            int r = (int) (ball.getPos().y() / this.cellHeight);
            int c = (int)  (ball.getPos().x() / this.cellWidth);

            // Boundary check
            r = Math.max(0, Math.min(r, rows - 1));
            c = Math.max(0, Math.min(c, cols - 1));

            this.cells[r][c].addBall(ball);
        }
    }

}
