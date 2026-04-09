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
        Board board = new Board(1200, 1200);
        Physics physics = new SequentialPhysics(board, 71, 71);


        // Start the Engines
        ControllerImpl controller = new ControllerImpl(physics);
        controller.start();

        // Open the Window
        SwingUtilities.invokeLater(() -> {
            View view = new View(physics, controller, 1200, 1200);
            view.setVisible(true);

            PhysicsThread physicsThread = new PhysicsThread(physics, view);
            physicsThread.start();
        });
    }
}
