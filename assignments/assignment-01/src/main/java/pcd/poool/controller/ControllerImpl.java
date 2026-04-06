package pcd.poool.controller;

import pcd.poool.model.BallState;
import pcd.poool.model.Hole;
import pcd.poool.model.Physics;
import pcd.sketch02.util.BoundedBuffer;
import pcd.sketch02.util.BoundedBufferImpl;

import java.nio.Buffer;
import java.util.List;

public class ControllerImpl extends Thread implements Controller {
    private final BoundedBuffer<Cmd> cmdBuffer;
    private final Physics model;

    public ControllerImpl(Physics model) {
        this.model = model;
        this.cmdBuffer = new BoundedBufferImpl<>(100);
    }

    @Override
    public void run() {
        System.out.println("Controller Thread started...");
        while (!isInterrupted()) {
            try {
                Cmd cmd = cmdBuffer.get();
                cmd.execute(model);

            } catch (InterruptedException e) {
                System.out.println("Controller interrupted.");
                break;
            }
        }
    }

    @Override
    public void processInput(String key) {
        Cmd moveCmd = new MoveUserCmd(key);

        try {
            cmdBuffer.put(moveCmd);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BallState getUserBallState() {
        return this.model.getUserBallState();
    }

    @Override
    public BallState getNPCBallState() {
        return this.model.getNPCBallState();
    }

    @Override
    public List<BallState> getStateSnapshot() {
        return this.model.getStateSnapshot();
    }

    @Override
    public String getCurrentFPS() {
        return String.valueOf(this.model.getCurrentFPS());
    }

    @Override
    public Hole getLeftHole() {
        return this.model.getLeftHole();
    }

    @Override
    public Hole getRightHole() {
        return this.model.getRightHole();
    }
}
