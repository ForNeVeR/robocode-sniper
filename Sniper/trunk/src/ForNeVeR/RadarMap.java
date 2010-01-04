package ForNeVeR;
import java.util.ArrayList;
import static ForNeVeR.Geometry.*;

/**
 * Representation of map, containing all the targets, found by radar scanning /
 * other methods.
 * @author ForNeVeR
 */
class RadarMap
{
    public ArrayList<RadarTarget> targets;

    public RadarMap()
    {
        targets = new ArrayList<RadarTarget>();
    }

    /**
     * Adds new radar target or updates existing.
     * @param name - name of target.
     * @param x - absolute x coord.
     * @param y - absolute y coord.
     */
    public void setTarget(String name, long time, double x, double y,
            double heading, double velocity)
    {
        for(int i = 0; i < targets.size(); i++)
        {
            if(targets.get(i).name.equals(name))
            {
                targets.set(i, new RadarTarget(name, time, x, y, heading,
                        velocity));
                return;
            }
        }
        targets.add(new RadarTarget(name, time, x, y, heading, velocity));
    }

    /**
     * Gets nearest target to specifical coordinates.
     * @param x
     * @param y
     * @return RadarTarget object, contains target nearest to coordinates x
     * and y.
     */
    public RadarTarget getNearestTarget(double x, double y)
    {
        if(targets.size() == 0)
            return null;

        double min_distance = distanceBetween(x, targets.get(0).coords.x, y,
                targets.get(0).coords.y);
        int index = 0;

        for(int i = 1; i < targets.size(); i++)
        {
            double distance = distanceBetween(x, targets.get(i).coords.x, y,
                    targets.get(i).coords.y);
            if(distance < min_distance)
            {
                min_distance = distance;
                index = i;
            }
        }

        return targets.get(index);
    }

    /**
     * Deletes target from map (possibly due to target death).
     * @param targetName - name of target to delete.
     */
    public void removeTarget(String targetName)
    {
        for(int i = 0; i < targets.size(); i++)
        {
            if(targets.get(i).name.equals(targetName))
            {
                targets.remove(i);
                return;
            }
        }
    }
}