package ForNeVeR;

import java.awt.*;
import static java.lang.Math.PI;
import robocode.*;
import static ForNeVeR.Geometry.*;

/**
 * Sniper RoboCode bot. Uses advanced targeting system.
 * @author ForNeVeR
 */
public class Sniper extends AdvancedRobot {
    /**
     * Distance that robot will try to keep from current enemy.
     */
	private final static double DISTANCE_TO_ENEMY = 150;
	private final static double DELTA_DISTANCE = 50;

	private RadarMap map;

    public Sniper() {
        map = new RadarMap();
    }

    @Override public void run() {
        setColors(Color.black, Color.black, Color.green, Color.green,
                Color.red);
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

		while(true) {
			Point currentPos = new Point(getX(), getY());
            RadarTarget target = map.getNearestTarget(currentPos);

			if (target != null) {
                // Determine moving.
				double distanceToEnemy = distanceBetween(target.lastCoords(),
                        currentPos);
				double bearingToEnemy = getBearing(currentPos,
                        target.lastCoords());

                if (distanceToEnemy > DISTANCE_TO_ENEMY + DELTA_DISTANCE ||
                        distanceToEnemy < DISTANCE_TO_ENEMY) {
                    // We have 2 points to move to: to the left side of enemy
                    // and to the right side of him. We have to determine which
                    // point to use.
                    Point leftPoint = movePointByVector(target.lastCoords(),
                            DISTANCE_TO_ENEMY, bearingToEnemy - PI / 2);
                    Point rightPoint = movePointByVector(target.lastCoords(),
                            DISTANCE_TO_ENEMY, bearingToEnemy + PI / 2);

                    Point pointToMove;
                    if (isPointOnTheBattlefield(leftPoint)) {
                        pointToMove = leftPoint;
                    } else {
                        pointToMove = rightPoint;
                    }

                    Graphics2D g = getGraphics();
                    g.setColor(Color.blue);
                    g.drawOval((int)(pointToMove.x - 25), (int)(pointToMove.y -
                            25), 50, 50);
                    setMoveToPoint(pointToMove);
                } else {
                    // Cycling maneuver.
                    // Again, we have 2 possible bearings to turn to: clockwise
                    // or counter-clockwise.
                    double bearingCW = normalizeAngle(bearingToEnemy -
                            getHeadingRadians() - PI / 2);
                    double bearingCCW = normalizeAngle(bearingToEnemy -
                            getHeadingRadians() + PI / 2);
                    if (Math.abs(bearingCW) < Math.abs(bearingCCW)) {
                        setTurnRightRadians(bearingCW);
                    } else {
                        setTurnRightRadians(bearingCCW);
                    }

                    // Now determine turning speed for moving inside a circle
                    // with radius = DISTANCE_TO_ENEMY.
                    double circleLength = 2 * PI * DISTANCE_TO_ENEMY;
                    double cycleTime = circleLength / Rules.MAX_VELOCITY;
                    setMaxTurnRate(radiansToDegrees(2 * PI / cycleTime));
                    setAhead(circleLength);
                }

				double turnByAngle = normalizeAngle(bearingToEnemy -
                        getGunHeadingRadians());
				double bulletPower = firePower(target);
                double bulletSpeed = Rules.getBulletSpeed(bulletPower);

                // Enemy position modelling.
				double bulletRadius = 0; // distance travelled by our bullet
                double imaginaryDistanceToEnemy = distanceToEnemy;
				for (long time = 0; bulletRadius < imaginaryDistanceToEnemy ||
                        turnByAngle > Rules.GUN_TURN_RATE * time; time++) {
                    bulletRadius += bulletSpeed;
					
                    Point targetingPos = target.estimatePositionAt(getTime() +
                            time);
                    imaginaryDistanceToEnemy = distanceBetween(targetingPos,
                            currentPos);
					double imaginaryBearingToEnemy = getBearing(currentPos,
                            targetingPos);
					turnByAngle = normalizeAngle(imaginaryBearingToEnemy -
                            getGunHeadingRadians());

					if (!isPointOnTheBattlefield(targetingPos)) {
						break;
                    }
				}

				setTurnGunRightRadians(turnByAngle);

				if (getGunHeat() == 0 &&
                        Math.abs(getGunTurnRemainingRadians()) <
                        getHeight() / (2 * imaginaryDistanceToEnemy)) {
					fire(bulletPower);
                } else {
					doNothing();
                }
			} else {
				doNothing();
			}
		}
	}

    /**
     * Checks whether point is on the battlefield or off its' limits.
     * @param point
     * @return
     */
    private boolean isPointOnTheBattlefield(Point point) {
        return point.x >= 0 && point.x <= getBattleFieldWidth() &&
                point.y >= 0 && point.y <= getBattleFieldHeight();
    }

    /**
     * Calculates firepower needed to fire to target.
     * @param t RadarTarget object represents target.
     * @return Preferred firepower.
     */
	private double firePower(RadarTarget t) {
		Point currentPos = new Point(getX(), getY());
        double distance = distanceBetween(t.lastCoords(), currentPos);

		double power;
		if (distance <= DISTANCE_TO_ENEMY + DELTA_DISTANCE) {
			power = Rules.MAX_BULLET_POWER;
        } else {
			power = (DISTANCE_TO_ENEMY + DELTA_DISTANCE) / distance *
                    Rules.MAX_BULLET_POWER;
        }

		return Math.max(power, 0.5);
	}

    /**
     * Starts movement to specified point.
     * @param p Point to move to.
     */
    private void setMoveToPoint(Point p) {
        Point currentPos = new Point(getX(), getY());
        
        double distanceToPoint = distanceBetween(p, currentPos);
        double relativeBearingToPoint = normalizeAngle(getBearing(currentPos,
                p) - getHeadingRadians());

        // Set this to max because it might be set to lesser value by other
        // methods:
        setMaxTurnRate(Rules.MAX_TURN_RATE);
        setTurnRightRadians(relativeBearingToPoint);
        setAhead(distanceToPoint);
    }

	@Override public void onScannedRobot(ScannedRobotEvent e) {
		map.setTarget(this, e);
    }

    @Override public void onRobotDeath(RobotDeathEvent e) {
        map.removeTarget(e.getName());
    }

	@Override public void onPaint(Graphics2D g) {
        Point currentPos = new Point(getX(), getY());
        RadarTarget currentTarget = map.getNearestTarget(currentPos);

		if (currentTarget != null) {
			Point targetPos = currentTarget.estimatePositionAt(getTime());

			g.setColor(Color.green);
			g.drawOval((int) (targetPos.x - (DISTANCE_TO_ENEMY - 
                    DELTA_DISTANCE)),
                    (int)(targetPos.y - (DISTANCE_TO_ENEMY - DELTA_DISTANCE)),
                    (int)(DISTANCE_TO_ENEMY - DELTA_DISTANCE) * 2,
                    (int)(DISTANCE_TO_ENEMY - DELTA_DISTANCE) * 2);
			g.drawOval((int) (targetPos.x - (DISTANCE_TO_ENEMY +
                    DELTA_DISTANCE)),
                    (int)(targetPos.y - (DISTANCE_TO_ENEMY + DELTA_DISTANCE)),
                    (int)(DISTANCE_TO_ENEMY + DELTA_DISTANCE) * 2,
                    (int)(DISTANCE_TO_ENEMY + DELTA_DISTANCE) * 2);
		}

		for (RadarTarget target : map.targets) {
            // Paint last seen position of target:
			g.setColor(Color.orange);
			g.drawOval((int) (target.lastCoords().x - 25),
                    (int) (target.lastCoords().y - 25), 50, 50);

            // Paint estimated position of target:
			Point p = target.estimatePositionAt(getTime());
			g.setColor(Color.blue);
			g.drawOval((int) (p.x - 25), (int) (p.y - 25), 50, 50);
        }
	}

    @Override public void onHitWall(HitWallEvent e) {
        setMaxTurnRate(Rules.MAX_TURN_RATE);
        double turning = PI / 2 - Math.abs(e.getBearingRadians());
        if (e.getBearingRadians() > 0) {
            setTurnRightRadians(turning);
        } else {
            setTurnLeftRadians(turning);
        }
        setBack(25);
        doNothing();
    }
}