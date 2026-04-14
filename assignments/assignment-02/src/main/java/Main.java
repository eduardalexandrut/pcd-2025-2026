import controller.Controller;
import view.View;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        final Controller controller = new Controller();

        SwingUtilities.invokeLater(() -> {
            final View view = new View(controller);
            view.setVisible(true);

        });
    }
}
