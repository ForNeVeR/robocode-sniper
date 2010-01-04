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
    static double distanceBetween(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}
    
    /**
     * Normalizes angle (i.e. converts it to range [-PI; PI]).
     * @param angle Angle in radians to be normalized.
     * @return Normalized angle in range [-PI; PI].
     */
    static double normalRelativeAngle(double angle)
    {
        return robocode.util.Utils.normalRelativeAngle(angle);
    }
}
