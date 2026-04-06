package pcd.poool.controller;

import pcd.poool.model.Ball;
import pcd.poool.model.BallState;
import pcd.poool.model.Hole;
import pcd.poool.model.P2d;

import java.util.List;

public interface Controller {

    void processInput(String key);

    /**
     * @return the current state of the User's ball.
     */
    BallState getUserBallState();

    /**
     * @return the current state of the NPC's ball.
     */
    BallState getNPCBallState();

    /**
     * @return a snapshot DTO of all the balls' state.
     */
    List<BallState> getStateSnapshot();

    String getCurrentFPS();

    Hole getLeftHole();

    Hole getRightHole();
}
