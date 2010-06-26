package ForNeVeR;

import java.util.ArrayList;
import static ForNeVeR.Geometry.*;

/**
 * Representation of map, containing all the targets, found by radar scanning /
 * other methods.
 * @author ForNeVeR
 */
class RadarMap {
    public ArrayList<RadarTarget> targets;

    /**
     * Creates a radar map.
     */
    public RadarMap() {
        targets = new ArrayList<RadarTarget>();
    }

    /**
     * Adds new target to the map or updates existing.
     * @param name - name of target.
     * @param time - time when target was seen.
     * @param coords - absolute coordinates of target.
     * @param heading - absolute heading of target in radians.
     * @param velocity - velocity of target.
     */
    public void setTarget(String name, long time, Point coords, double heading,
            double velocity) {
        for (int i = 0; i < targets.size(); i++) {
            if (targets.get(i).name.equals(name)) {
                targets.set(i, new RadarTarget(name, time, coords, heading,
                        velocity));
                return;
            }
        }
        targets.add(new RadarTarget(name, time, coords, heading, velocity));
    }

    /**
     * Gets nearest target to specifical coordinates.
     * @param coords - absolute coordinates of point, to which nearest target
     * to be found.
     * @return RadarTarget object, contains target nearest to coordinates x
     * and y. If there are no targets on map, returns null.
     */
    public RadarTarget getNearestTarget(Point coords) {
        if (targets.isEmpty()) {
            return null;
        }

        double min_distance = distanceBetween(coords, targets.get(0).coords);
        int index = 0;

        for (int i = 1; i < targets.size(); i++) {
            double distance = distanceBetween(coords, targets.get(i).coords);
            if (distance < min_distance) {
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
    public void removeTarget(String targetName) {
        for (int i = 0; i < targets.size(); i++) {
            if (targets.get(i).name.equals(targetName)) {
                targets.remove(i);
                return;
            }
        }
    }
}