package controller;

import lib.FSReport;
import model.Model;
import model.ModelListener;
import view.View;

import javax.swing.*;
import java.nio.file.Path;

public class Controller {
    private Model model;
    private View view;

    public Controller(Model model) {
        this.model = model;
    }

    public void setView(View view) {
        this.view = view;
    }

    public void start(final String folder, final int nb, final long maxSize) {
        this.model.setListener(new ModelListener() {
            @Override
            public void onUpdate(FSReport report) {
                SwingUtilities.invokeLater(() -> view.render(report));
            }

            @Override
            public void onComplete(FSReport report) {
                SwingUtilities.invokeLater(() -> view.render(report));
            }
        });
        this.model.start(Path.of(folder), maxSize, nb);
    }

    public void stop() {
        this.model.stop();
    }
}
