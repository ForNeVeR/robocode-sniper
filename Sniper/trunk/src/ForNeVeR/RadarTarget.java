package ForNeVeR;

import static ForNeVeR.Geometry.*;

/**
 * Represents radar target. Coordinates stored by absolute, not relative
 * values.
 * @author ForNeVeR
 */
class RadarTarget {
    String name;
    long time; // when target was last seen
    Point coords;
    double heading;
    double velocity;

    /**
     * Creates a radar target with specified parameters.
     * @param name
     * @param time
     * @param coords
     * @param heading
     * @param velocity
     */
    public RadarTarget(String name, long time, Point coords,
            double heading, double velocity) {
        this.name = new String(name);
        this.time = time;
        this.coords = coords;
        this.heading = heading;
        this.velocity = velocity;
    }

    /**
     * Calculates estimated target position at some moment of time.
     * @param atTime Time when target position must be calculated.
     * @return RadarTarget object with coordinates at given time.
     */
    public Point estimatePositionAt(long atTime) {
        return movePointByVector(coords, velocity * (atTime - time), heading);
    }
}