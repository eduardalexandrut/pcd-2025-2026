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

                if ("WASD".contains(key)) {
                    controller.processInput(key);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                String key = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();

                if ("WASD".contains(key)) {
                    controller.processInput("STOP_ " + key);
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


            // Draw balls
            for (BallState b : controller.getStateSnapshot()) {
                this.drawBall(b, g2, null);
            }

            // Draw User's ball
            BallState userBall = controller.getUserBallState();
            // Overdraw the user ball with its specific label
            if (userBall != null) {
                this.drawBall(userBall, g2, "H");
            }

            // Draw NPC's ball
            BallState npcBall = controller.getNPCBallState();
            if (npcBall != null) {
                this.drawBall(npcBall, g2, "B");
            }

        }

        /**
         * Draws a ball with a white fill, black border, and an optional label.
         */
        private void drawBall(BallState ball, Graphics2D g2, String label) {
            int x = (int) ball.pos().x();
            int y = (int) ball.pos().y();
            int r = (int) ball.radius();

            // --- A. Draw the Fill (White) ---
            g2.setColor(Color.WHITE);
            g2.fillOval(x - r, y - r, r * 2, r * 2);

            // --- B. Draw the Border (Black) ---
            g2.setColor(Color.BLACK);
            // Use a slightly thicker stroke for visibility
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(x - r, y - r, r * 2, r * 2);

            // --- C. Draw the Label (If provided) ---
            if (label != null && !label.isEmpty()) {
                g2.setColor(Color.BLACK); // Label color

                // Set a bold, readable font. Adjust size based on ball radius.
                int fontSize = (int) (r * 1.5); // Example scaling
                g2.setFont(new Font("Arial", Font.BOLD, fontSize));

                // Center the text perfectly
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(label);
                int textHeight = fm.getAscent(); // Height from baseline up

                // We center the text relative to the ball's center (x, y)
                int labelX = x - (textWidth / 2);
                // Descent is the part below the baseline (like in 'g' or 'y')
                // We add half the ascent to center the bulk of the letter
                int labelY = y + (textHeight / 2) - fm.getDescent() / 2;

                g2.drawString(label, labelX, labelY);
            }
        }

    }
}
