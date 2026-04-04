package pcd.poool.view;

import pcd.poool.controller.Controller;
import pcd.poool.model.BallState;
import pcd.poool.model.Physics;
import pcd.poool.model.UserBall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class View extends JFrame {
    private final Physics model;
    private final Controller controller;
    private final VisualiserPanel panel;


    public View(Physics model, Controller controller, int w, int h){
        this.model = model;
        this.controller = controller;
        setTitle("Poool");
        setSize(w,h + 25);
        setResizable(false);
        panel = new VisualiserPanel(w,h);
        getContentPane().add(panel);

        javax.swing.Timer timer = new javax.swing.Timer(16, e -> {
            panel.repaint(); // This tells Swing to call paintComponent
        });
        timer.start();

        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent ev){
                System.exit(-1);
            }
            public void windowClosed(WindowEvent ev){
                System.exit(-1);
            }
        });

        // Add the KeyListener
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String key = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();
                System.out.println("[UI Thread] Key pressed: " + key);

                if ("WASD".contains(key)) {
                    controller.processInput(key);
                }
            }
        });


        this.setFocusable(true);
        this.requestFocusInWindow();

    }

    public void render(){

    }

    public class VisualiserPanel extends JPanel {
        private int ox;
        private int oy;
        private int delta;

        public VisualiserPanel(int w, int h){
            setSize(w,h + 25);
            ox = w/2;
            oy = h/2;
            delta = Math.min(ox, oy);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // This handles the clearRect and background
            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // --- 1. DRAW THE CROSSHAIR (from your old paint method) ---
            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(1));
            g2.drawLine(ox, 0, ox, oy * 2);
            g2.drawLine(0, oy, ox * 2, oy);

            // --- 2. DRAW THE BALLS ---
            g2.setColor(Color.BLUE);
            for (BallState b : controller.getStateSnapshot()) {
                int x = (int) b.pos().x();
                int y = (int) b.pos().y();
                int r = (int) b.radius();

                g2.fillOval(x - r, y - r, r * 2, r * 2);
            }

            // Draw the Users' ball
            BallState userBall = controller.getUserBallState();
            int x = (int) userBall.pos().x();
            int y = (int) userBall.pos().y();
            int r = (int) userBall.radius();

            g2.fillOval(x - r, y - r, r * 2, r * 2);

        }

    }
}
