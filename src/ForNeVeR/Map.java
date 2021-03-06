package ForNeVeR;

import robocode.ScannedRobotEvent;
import robocode.Robot;
import java.util.ArrayList;
import static ForNeVeR.Geometry.*;

/**
 * Representation of map, containing all the targets, found by radar scanning /
 * other methods.
 * @author ForNeVeR
 */
class Map {
    public ArrayList<Target> targets;
    public ArrayList<Bullet> bullets;

    /**
     * Creates a radar map.
     */
    public Map() {
        targets = new ArrayList<Target>();
    }

    /**
     * Adds new target to the map or updates existing.
     * @param robot Reference to your robot object.
     * @param event ScannedRobotEvent for determining target position.
     */
    public void setTarget(Robot robot, ScannedRobotEvent event) {
        String targetName = event.getName();
        TargetPosition targetPosition = new TargetPosition(robot, event);

        for (int i = 0; i < targets.size(); i++) {
            Target target = targets.get(i);
            if (target.name.equals(targetName)) {
                target.addPosition(targetPosition);
                return;
            }
        }

        targets.add(new Target(targetName, targetPosition));
    }

    /**
     * Adds bullet to radar map.
     * @param bullet Reference to Bullet object to add.
     */
    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }


    /**
     * Gets nearest target to specifical coordinates.
     * @param coords Absolute coordinates of point, to which nearest target to
     * be found.
     * @return RadarTarget object, contains target nearest to coordinates x
     * and y. If there are no targets on map, returns null.
     */
    public Target getNearestTarget(Point coords) {
        if (targets.isEmpty()) {
            return null;
        }

        double min_distance = distanceBetween(coords,
                targets.get(0).lastCoords());
        int index = 0;
        for (int i = 1; i < targets.size(); i++) {
            double distance = distanceBetween(coords,
                    targets.get(i).lastCoords());
            if (distance < min_distance) {
                min_distance = distance;
                index = i;
            }
        }

        return targets.get(index);
    }

    /**
     * Deletes target from map (possibly due to target death).
     * @param targetName Name of target to delete.
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