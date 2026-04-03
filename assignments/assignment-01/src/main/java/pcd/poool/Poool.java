package pcd.poool;

import pcd.poool.controller.Controller;
import pcd.poool.controller.ControllerImpl;
import pcd.poool.model.Physics;
import pcd.poool.model.PhysicsImpl;
import pcd.poool.view.View;

public class Poool {

    public static void main(String[] args) {
        Physics model = new PhysicsImpl();
        ControllerImpl controller = new ControllerImpl(model);

        View view = new View(model, controller, 800, 600);

        controller.start();
        view.setVisible(true);

        System.out.println("System initialized. Press WASD in the window.");
    }
}
