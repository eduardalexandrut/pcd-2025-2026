package pcd.poool.model;

import java.util.List;

public class Board {

        private final double width;
        private final double height;

        public Board(double width, double height) {
            this.width = width;
            this.height = height;
        }

        public double getWidth() { return width; }
        public double getHeight() { return height; }

        // Returns a Boundary object that your Ball class expects
        public Boundary getBounds() {
            return new Boundary(0, 0, width, height);
        }

}
