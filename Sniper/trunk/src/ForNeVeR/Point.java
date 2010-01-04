package ForNeVeR;

/**
 * Class represents 2D point with double precision coordinates.
 * @author ForNeVeR
 */
public class Point implements Cloneable
{
    public double x, y;

    public Point()
    {
        x = y = 0;
    }

    public Point(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    @Override public Point clone()
    {
        return new Point(x, y);
    }
}
