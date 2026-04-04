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
        PhysicsImpl physics = new PhysicsImpl(board, 5, 5);


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
