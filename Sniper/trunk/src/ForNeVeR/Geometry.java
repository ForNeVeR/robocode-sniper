package ForNeVeR;

/**
 * Class with static methods for various geometric manipulations.
 * @author ForNeVeR
 */
class Geometry
{
    /**
     * Calculates distance between two 2D points.
     * @param p1 First point.
     * @param p2 Second point.
     * @return Distance between points.
     */
    public static double distanceBetween(Point p1, Point p2)
    {
        return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }
    
    /**
     * Normalizes angle (i.e. converts it to range [-PI; PI)).
     * @param angle Angle in radians to be normalized.
     * @return Normalized angle in range [-PI; PI).
     */
    public static double normalizeAngle(double angle)
    {
        return robocode.util.Utils.normalRelativeAngle(angle);
    }

    /**
     * Moves point by specified vector.
     * @param p Point object to be moved.
     * @param length Length of vector.
     * @param angle Angle of vector bearing in radians.
     * @return Copy of input Point object with changed coordinates.
     */
    public static Point movePointByVector(Point p, double length, double angle)
    {
        double x2 = p.x + length * Math.sin(angle);
        double y2 = p.y + length * Math.cos(angle);

        return new Point(x2, y2);
    }

    /**
     * Converts radians to degrees.
     * @param radians Angle in radians to be converted.
     * @return Angle in degrees.
     */
    public static double radiansToDegrees(double radians)
    {
        return radians * 180 / Math.PI;
    }

    /**
     * Returns absolute bearing from point p1 to point p2.
     * @param p1
     * @param p2
     * @return Absolute bearing in radians.
     */
    public static double getBearing(Point p1, Point p2)
    {
        return Math.atan2(p2.x - p1.x, p2.y - p1.y);
    }
}
