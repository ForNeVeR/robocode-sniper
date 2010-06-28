package ForNeVeR;

import robocode.Robot;
import robocode.ScannedRobotEvent;
import static ForNeVeR.Geometry.*;

/**
 * Class for representing target's position: its' absolute coordinates,
 * heading, velocity and time it was seen at this position.
 * @author ForNeVeR
 */
public class TargetPosition {
    /**
     * Initializes object.
     * @param time
     * @param coords
     * @param heading
     * @param velocity
     */
    private void init(long time, Point coords, double heading, double velocity)
    {
        this.time = time;
        this.coords = coords;
        this.heading = heading;
        this.velocity = velocity;
    }

    /**
     * Creates a TargetPosition object from all its' members' values.
     * @param time
     * @param coords
     * @param heading
     * @param velocity
     */
    public TargetPosition(long time, Point coords, double heading,
            double velocity) {
        init(time, coords, heading, velocity);
    }

    /**
     * Creates a target position using your robot position and
     * ScannedRobotEvent relative to it.
     * @param robot Your robot instance, used only for position retrieving.
     * @param event ScannedRobotEvent for enemy target.
     */
    public TargetPosition(Robot robot, ScannedRobotEvent event) {
        Point robotPos = new Point(robot.getX(), robot.getY());
        Point enemyPos = movePointByVector(robotPos, event.getDistance(),
                event.getBearingRadians() +
                degreesToRadians(robot.getHeading()));

        init(event.getTime(), enemyPos, event.getHeadingRadians(),
                event.getVelocity());
    }

    public long time;
    public Point coords;
    public double heading;
    public double velocity;
}
