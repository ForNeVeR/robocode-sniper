package ForNeVeR;

/**
 * Class represents 2D point with double precision coordinates.
 * @author ForNeVeR
 */
public class Point {
    public double x, y;

    /**
     * Creates a point with coordinates (0, 0).
     */
    public Point() {
        x = y = 0;
    }

    /**
     * Creates a point with coordinates (x, y).
     * @param x
     * @param y
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
}