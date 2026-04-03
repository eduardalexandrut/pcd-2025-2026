package pcd.poool;

import pcd.poool.controller.Controller;
import pcd.poool.controller.ControllerImpl;
import pcd.poool.model.*;
import pcd.poool.view.View;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Poool {

    public static void main(String[] args) {
        // Setup the World
        Board board = new Board(800, 600);
        PhysicsImpl physics = new PhysicsImpl(board);

        // Add some random target balls
        Random rand = new Random();
        for (int i = 0; i < 25; i++) {
            physics.addBall(new Ball(
                    new P2d(rand.nextInt(700) + 50, rand.nextInt(500) + 50),
                    15, 1.0,
                    new V2d(rand.nextInt(300) - 50, rand.nextInt(300) - 50)
            ));
        }

        // Start the Engines
        ControllerImpl controller = new ControllerImpl(physics);
        controller.start();

        PhysicsThread physicsThread = new PhysicsThread(physics);
        physicsThread.start();

        // Open the Window
        SwingUtilities.invokeLater(() -> {
            View view = new View(physics, controller, 800, 600);
            view.setVisible(true);
        });
    }
}
