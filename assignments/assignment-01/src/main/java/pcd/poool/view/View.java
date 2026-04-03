package pcd.poool.view;

import pcd.poool.controller.Controller;
import pcd.poool.model.Physics;

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

        public void paint(Graphics g){
            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2.clearRect(0,0,this.getWidth(),this.getHeight());

            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(1));
            g2.drawLine(ox,0,ox,oy*2);
            g2.drawLine(0,oy,ox*2,oy);
            g2.setColor(Color.BLACK);

            g2.setStroke(new BasicStroke(1));
//            for (var b: model.getBalls()) {
//                var p = b.pos();
//                int x0 = (int)(ox + p.x()*delta);
//                int y0 = (int)(oy - p.y()*delta);
//                int radiusX = (int)(b.radius()*delta);
//                int radiusY = (int)(b.radius()*delta);
//                g2.drawOval(x0 - radiusX,y0 - radiusY,radiusX*2,radiusY*2);
//            }
//
//            g2.setStroke(new BasicStroke(3));
//            var pb = model.getPlayerBall();
//            if (pb != null) {
//                var p1 = pb.pos();
//                int x0 = (int)(ox + p1.x()*delta);
//                int y0 = (int)(oy - p1.y()*delta);
//                int radiusX = (int)(pb.radius()*delta);
//                int radiusY = (int)(pb.radius()*delta);
//                g2.drawOval(x0 - radiusX,y0 - radiusY,radiusX*2,radiusY*2);
//            }
//
//            g2.setStroke(new BasicStroke(1));
//            g2.drawString("Num small balls: " + model.getBalls().size(), 20, 40);
//            g2.drawString("Frame per sec: " + model.getFramePerSec(), 20, 60);
//
//            sync.notifyFrameRendered();

        }

    }
}
