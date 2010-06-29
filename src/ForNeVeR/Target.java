package ForNeVeR;

import java.util.ArrayList;
import static ForNeVeR.Geometry.*;

/**
 * Represents enemy target on radar.
 * @author ForNeVeR
 */
class Target {
    public String name;
    private ArrayList<TargetPosition> positions;

    /**
     * Creates a radar target with specified parameters.
     * @param name Name of the target.
     * @param position TargetPosition objects representing the target.
     */
    public Target(String name, TargetPosition position) {
        this.name = name;
        positions = new ArrayList<TargetPosition>();
        positions.add(position);
    }

    /**
     * Adds position to target positions list. Deletes the oldest positions if
     * needed.
     * @param position Position to add.
     */
    public void addPosition(TargetPosition position) {
        if (positions.size() > 0) {
            positions.remove(0);
        }
        positions.add(position);
    }

    /**
     * Returns last known coordinates of target.
     * @return Coordinates of last position.
     */
    public Point lastCoords() {
        if (positions.isEmpty()) {
            return null;
        }

        long lastTime = positions.get(0).time;
        Point lastCoords = positions.get(0).coords;
        for (int i = 1; i < positions.size(); ++i) {
            TargetPosition position = positions.get(i);
            if (position.time > lastTime) {
                lastTime = position.time;
                lastCoords = position.coords;
            }
        }

        return lastCoords;
    }

    /**
     * Calculates estimated target position at some moment of time.
     * @param atTime Time when target position must be calculated.
     * @return Point object with coordinates at given time.
     */
    public Point estimatePositionAt(long atTime) {
        if (positions.size() > 0) {
            TargetPosition position = positions.get(0);
            return movePointByVector(position.coords, position.velocity *
                    (atTime - position.time), position.heading);
        } else {
            return null;
        }
    }
}