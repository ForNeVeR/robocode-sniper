package ForNeVeR;

/**
 * Represents radar target. Coordinates stored by absolute, not relative
 * values.
 * @author ForNeVeR
 */
class RadarTarget
{
    String name;
    long time; // when target was last seen
    double x, y;
    double heading;
    double velocity;

    public RadarTarget(String name, long time, double x, double y,
            double heading, double velocity)
    {
        this.name = new String(name);
        this.time = time;
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.velocity = velocity;
    }

    /**
     * Calculates estimated target position at some moment of time.
     * @param atTime Time when target position must be calculated.
     * @return RadarTarget object with coordinates at given time.
     */
    public RadarTarget estimatePositionAt(long atTime)
    {
        double newX = x + velocity * Math.sin(heading) * (atTime - time);
        double newY = y + velocity * Math.cos(heading) * (atTime - time);

        return new RadarTarget(name, atTime, newX, newY, heading, velocity);
    }
}