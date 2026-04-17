import controller.Controller;
import lib.event_loop.FSStatLibVertx;
import lib.reactivex.FSStatLibReactive;
import lib.virtual_threads.FSStatLibVT;
import model.Model;
import view.View;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        final Model model = new Model(new FSStatLibReactive());
        final Controller controller = new Controller(model);

        SwingUtilities.invokeLater(() -> {
            final View view = new View(controller);
            view.setVisible(true);

            controller.setView(view);
        });
    }
}
