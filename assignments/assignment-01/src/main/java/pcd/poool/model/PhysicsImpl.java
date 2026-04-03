package pcd.poool.model;

import java.util.List;

public class PhysicsImpl implements Physics{
    @Override
    public void computeState(long dt) {

    }

    @Override
    public void updateUserBall(P2d position) {

    }

    @Override
    public void updateNPCBall(Ball ball) {

    }

    @Override
    public BallState getUserBallState() {
        return null;
    }

    @Override
    public BallState getNPCBallState() {
        return null;
    }

    @Override
    public List<BallState> getStateSnapshot() {
        return List.of();
    }
}
