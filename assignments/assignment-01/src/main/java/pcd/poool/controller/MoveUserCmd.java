package pcd.poool.controller;

import pcd.poool.model.P2d;
import pcd.poool.model.Physics;
import pcd.sketch02.model.Counter;

public class MoveUserCmd implements Cmd {
    private String key;

    public MoveUserCmd(String key) {
        this.key = key;
    }

    @Override
    public void execute(Physics model) {
        System.out.println("[Controller Thread] Processing move: " + key);

        // Map the key to a direction
        double dx = 0, dy = 0;
        switch (key.toUpperCase()) {
            case "W" -> dy = -1;
            case "S" -> dy = 1;
            case "A" -> dx = -1;
            case "D" -> dx = 1;
        }

        model.updateUserBall(new P2d(dx, dy));
    }
}
