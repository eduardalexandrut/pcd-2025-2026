package pcd.poool.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PhysicsImpl implements Physics{
    private final Board board;
    private Cell[][] cells;
    private UserBall userBall;
    private final int rows, cols;
    private final double cellWidth, cellHeight;

    public PhysicsImpl(Board board, int rows, int cols) {
        this.board = board;
        this.cells = new Cell[rows][cols];

        this.rows = rows;
        this.cols = cols;

        this.cellWidth = board.getWidth() / cols;
        this.cellHeight = board.getHeight() / rows;

        int cellId = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.cells[i][j] = new Cell(cellId);
                cellId = cellId + 1;
            }
        }

        this.syncBoard(board);

        this.userBall = new UserBall(
                new P2d(300, 300),
                30,
                10.0,
                new V2d(10, 10)
        );
    }

    @Override
    public void computeState(long dt) {
        int nThreads = 1;//Runtime.getRuntime().availableProcessors();
        final PhaseLatch barrier = new PhaseLatch(nThreads);
        final PhysicsWorker[] workers = new PhysicsWorker[nThreads];

        final int rowsPerThread = this.rows / nThreads;

        for (int i = 0; i < nThreads; i++) {
            int start = i * rowsPerThread;
            int end = (i == nThreads - 1) ? rows - 1 : (start + rowsPerThread - 1);

            workers[i] = new PhysicsWorker(this, barrier, start, end, dt);
            workers[i].start();
        }

        for (PhysicsWorker w : workers) {
            try {
                w.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        this.resetCells();

    }

    private void transferToCorrectCell(Ball b) {
        // 1. Calculate the indices based on the ball's current position
        int r = (int) (b.getPos().y() / this.cellHeight);
        int c = (int) (b.getPos().x() / this.cellWidth);

        // 2. Safety Clamp: Ensure the ball doesn't fly off the array indices
        // (e.g., if it hits a boundary exactly or slightly exceeds it)
        r = Math.max(0, Math.min(r, rows - 1));
        c = Math.max(0, Math.min(c, cols - 1));

        // 3. Call the Monitor method of the target cell
        // This method handles its own locking internally.
        this.cells[r][c].addBall(b);
    }

    @Override
    public void updateRowMovement(int r, long dt) {
        for (int c = 0; c < cols; c++) {
            List<Ball> leavers = cells[r][c].updateMovement(dt, board, cellWidth, cellHeight, r, c);

            for (Ball b : leavers) {
                transferToCorrectCell(b);
            }
        }
    }

    public void resolveRowCollisions(int r) {

        for (int c = 0; c < cols; c++) {

            final Cell current = this.cells[r][c];

            // Handle internal collisions
            current.resolveInternalCollisions();

            // Handle collision among balls of different cells at the borders
            int[][] directions = {{0, 1}, {1, 1}, {1, 0}, {1, -1}};

            for (int[] d:  directions) {
                int nr =  r + d[0];
                int nc = c + d[1];

                if (isValid(nr, nc)) {
                    multiLockResolver(r, c, nr, nc);
                }
            }

            // Handle collision with User ball
            current.lock();
            try {
                for (Ball b : current.getBalls()) {
                    // Check and resolve the collision "instantly" in this frame
                    Ball.resolveCollision(this.userBall, b);
                }
            } finally {
                current.unlock();
            }
        }
    }

    private void multiLockResolver(int r, int c, int nr, int nc) {
        final Cell cell1 = this.cells[r][c];
        final Cell cell2 = this.cells[nr][nc];

        final Cell first = cell1.getId() > cell2.getId() ? cell1 : cell2;
        final Cell second = cell2.getId() > cell1.getId() ? cell2 : cell1;

        first.lock();
        second.lock();

        try {
            for (Ball ballA : cell1.getBalls()) {
                for (Ball ballB : cell2.getBalls()) {
                    Ball.resolveCollision(ballA, ballB);
                }
            }
        } finally {
            second.unlock();
            first.unlock();
        }
    }


    private boolean isValid(int r, int c) {
        return r >= 0 && r < this.rows && c >= 0 && c < this.cols;
    }


    @Override
    public void updateUserBall(P2d position) {

    }

    @Override
    public void updateNPCBall(Ball ball) {

    }

    @Override
    public BallState getUserBallState() {
        return BallState.fromBall(this.userBall);
    }

    @Override
    public BallState getNPCBallState() {
        return null;
    }

    @Override
    public List<BallState> getStateSnapshot() {
        List<BallState> snapshot = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = cells[i][j];

                cell.lock();
                try {
                    for (Ball b : cell.getBalls()) {
                        snapshot.add(BallState.fromBall(b));
                    }
                } finally {
                    cell.unlock();
                }
            }
        }
        return snapshot;
    }


    private void syncBoard(final Board board) {

        for (final Ball ball : board.getBalls()) {
            int r = (int) (ball.getPos().y() / this.cellHeight);
            int c = (int)  (ball.getPos().x() / this.cellWidth);

            // Boundary check
            r = Math.max(0, Math.min(r, rows - 1));
            c = Math.max(0, Math.min(c, cols - 1));

            this.cells[r][c].addBall(ball);
        }
    }


    private void resetCells() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c].reset();
            }
        }
    }
}
