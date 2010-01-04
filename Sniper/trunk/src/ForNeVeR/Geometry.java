package ForNeVeR;

/**
 * Class with static methods for various geometric manipulations.
 * @author ForNeVeR
 */
class Geometry
{
    /**
     * Calculates distance between two 2D points.
     * @param x1 X coordinate of first point.
     * @param y1 Y coordinate of first point.
     * @param x2 X coordinate of second point.
     * @param y2 Y coordinate of second point.
     * @return Distance between points.
     */
    public static double distanceBetween(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

    /**
     * Calculates distance between two 2D points.
     * @param p1 Point object representing the first point.
     * @param x2 X coordinate of second point.
     * @param y2 Y coordinate of second point.
     * @return Distance between points.
     */
    public static double distanceBetween(Point p1, double x2, double y2)
    {
        return distanceBetween(p1.x, p1.y, x2, y2);
    }
    
    /**
     * Normalizes angle (i.e. converts it to range [-PI; PI]).
     * @param angle Angle in radians to be normalized.
     * @return Normalized angle in range [-PI; PI].
     */
    public static double normalRelativeAngle(double angle)
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
}
