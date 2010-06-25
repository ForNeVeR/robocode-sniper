package ForNeVeR;

import java.awt.*;
import static java.lang.Math.PI;
import robocode.*;
import static ForNeVeR.Geometry.*;

/**
 * Sniper RoboCode bot. Uses advanced targeting system.
 * @author ForNeVeR
 */
public class Sniper extends AdvancedRobot
{
    /**
     * Distance that robot will try to keep from current enemy.
     */
	private final static double DISTANCE_TO_ENEMY = 150;
	private final static double DELTA_DISTANCE = 50;

	private RadarMap map;

    public Sniper()
    {
        map = new RadarMap();
    }

	/**
	 * run: Sniper's default behavior
	 */
    @Override public void run()
    {
        setColors(Color.black, Color.black, Color.green, Color.green,
                Color.red);
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

		while(true)
        {
			Point currentPos = new Point(getX(), getY());
            RadarTarget target = map.getNearestTarget(currentPos);

			if(target != null)
			{
				// Determine moving.
				double distanceToEnemy = distanceBetween(target.coords,
                        currentPos);
				double bearingToEnemy = Math.atan2(target.coords.x -
                        currentPos.x, target.coords.y - currentPos.y);

                if(distanceToEnemy > DISTANCE_TO_ENEMY + DELTA_DISTANCE ||
                        distanceToEnemy < DISTANCE_TO_ENEMY)
                {
                    // We have 2 points to move to: to the left side of enemy
                    // and to the right side of him. We have to determine which
                    // point to use.
                    Point leftPoint = movePointByVector(target.coords,
                            DISTANCE_TO_ENEMY, bearingToEnemy - PI / 2);
                    Point rightPoint = movePointByVector(target.coords,
                            DISTANCE_TO_ENEMY, bearingToEnemy + PI / 2);

                    Point pointToMove;
                    if (isPointOnTheBattlefield(leftPoint))
                        pointToMove = leftPoint;
                    else
                        pointToMove = rightPoint;

                    Graphics2D g = getGraphics();
                    g.setColor(Color.blue);
                    g.drawOval((int) (pointToMove.x - 25), (int) (pointToMove.y
                            - 25), 50, 50);
                    setMoveToPoint(pointToMove);
                }
                else
                {
                    // Cycling maneuver.
                    // Again, we have 2 possible bearings to turn to: clockwise
                    // or counter-clockwise.
                    double bearingCW = normalRelativeAngle(bearingToEnemy
                            - getHeadingRadians() - PI / 2);
                    double bearingCCW = normalRelativeAngle(bearingToEnemy
                            - getHeadingRadians() + PI / 2);
                    if (Math.abs(bearingCW) < Math.abs(bearingCCW))
                        setTurnRightRadians(bearingCW);
                    else
                        setTurnRightRadians(bearingCCW);

                    // Now determine turning speed for moving inside a circle
                    // with radius = DISTANCE_TO_ENEMY.
                    double circleLength = 2 * PI * DISTANCE_TO_ENEMY;
                    double cycleTime = circleLength / Rules.MAX_VELOCITY;
                    setMaxTurnRate(radiansToDegrees(2 * PI / cycleTime));
                    setAhead(circleLength);
                }

				double turnByAngle = normalRelativeAngle(bearingToEnemy
                        - getGunHeadingRadians());
				double bulletPower = firePower(target);
                double bulletSpeed = Rules.getBulletSpeed(bulletPower);

                // Enemy position modelling.
				double bulletRadius = 0; // distance travelled by our bullet
                double imaginaryDistanceToEnemy = distanceToEnemy;
				for(long time = 0; bulletRadius < imaginaryDistanceToEnemy ||
                        turnByAngle > Rules.GUN_TURN_RATE * time; time++)
				{
					bulletRadius += bulletSpeed;
					
                    Point targetingPos = target.estimatePositionAt(getTime()
                            + time);
                    imaginaryDistanceToEnemy = distanceBetween(targetingPos,
                            currentPos);
					double imaginaryBearingToEnemy =
                            Math.atan2(targetingPos.x - currentPos.x,
                            targetingPos.y - currentPos.y);
					turnByAngle = normalRelativeAngle(imaginaryBearingToEnemy
                            - getGunHeadingRadians());

					if(!isPointOnTheBattlefield(targetingPos))
						break;
				}

				setTurnGunRightRadians(turnByAngle);

				if(getGunHeat() == 0 &&
                        Math.abs(getGunTurnRemainingRadians()) <
                        getHeight() / (2 * imaginaryDistanceToEnemy))
					fire(bulletPower);
				else
					doNothing();
			}
			else
			{
				doNothing();
			}
		}
	}

    /**
     * Checks whether point is on the battlefield or off its' limits.
     * @param point
     * @return
     */
    private boolean isPointOnTheBattlefield(Point point)
    {
        return point.x >= 0 && point.x <= getBattleFieldWidth() &&
                point.y >= 0 && point.y <= getBattleFieldHeight();
    }

    /**
     * Calculates firepower needed to fire to target.
     * @param t RadarTarget object represents target.
     * @return Preferred firepower.
     */
	private double firePower(RadarTarget t)
	{
		Point currentPos = new Point(getX(), getY());
        double distance = distanceBetween(t.coords, currentPos);

		double power;
		if(distance <= DISTANCE_TO_ENEMY + DELTA_DISTANCE)
			power = Rules.MAX_BULLET_POWER;
		else
			power = (DISTANCE_TO_ENEMY + DELTA_DISTANCE) / distance *
                    Rules.MAX_BULLET_POWER;

		return Math.max(power, 0.5);
	}

    /**
     * Starts movement to specified point.
     * @param p Point to move to.
     */
    private void setMoveToPoint(Point p)
    {
        Point currentPos = new Point(getX(), getY());
        
        double distanceToPoint = distanceBetween(p, currentPos);
        double relativeBearingToPoint = normalRelativeAngle(Math.atan2(p.x
                - currentPos.x, p.y - currentPos.y) - getHeadingRadians());

        // Set this to max because it might be set to lesser value by other
        // methods.
        setMaxTurnRate(Rules.MAX_TURN_RATE);
        setTurnRightRadians(relativeBearingToPoint);
        setAhead(distanceToPoint);
    }

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	@Override public void onScannedRobot(ScannedRobotEvent e)
    {
		Point currentPos = new Point(getX(), getY());
        Point enemyPos = movePointByVector(currentPos, e.getDistance(),
                e.getBearingRadians() + getHeadingRadians());

        map.setTarget(e.getName(), getTime(), enemyPos, e.getHeadingRadians(),
                e.getVelocity());
    }

    @Override public void onRobotDeath(RobotDeathEvent e)
    {
        map.removeTarget(e.getName());
    }

	@Override public void onPaint(Graphics2D g)
	{
        Point currentPos = new Point(getX(), getY());
        RadarTarget currentTarget = map.getNearestTarget(currentPos);

		if(currentTarget != null)
		{
			Point targetPos = movePointByVector(currentTarget.coords,
                    currentTarget.velocity * (getTime() - currentTarget.time),
                    currentTarget.heading);

			g.setColor(Color.green);
			g.drawOval((int) (targetPos.x - (DISTANCE_TO_ENEMY
                    - DELTA_DISTANCE)), (int) (targetPos.y
                    - (DISTANCE_TO_ENEMY - DELTA_DISTANCE)),
                    (int) (DISTANCE_TO_ENEMY - DELTA_DISTANCE) * 2,
                    (int)(DISTANCE_TO_ENEMY - DELTA_DISTANCE) * 2);
			g.drawOval((int) (targetPos.x - (DISTANCE_TO_ENEMY
                    + DELTA_DISTANCE)), (int) (targetPos.y
                    - (DISTANCE_TO_ENEMY + DELTA_DISTANCE)),
                    (int) (DISTANCE_TO_ENEMY + DELTA_DISTANCE) * 2,
                    (int) (DISTANCE_TO_ENEMY + DELTA_DISTANCE) * 2);
		}

		for(int i = 0; i < map.targets.size(); i++)
		{
            // Paint estimated position of target
			Point p = map.targets.get(i).estimatePositionAt(getTime());
			g.setColor(Color.blue);
			g.drawOval((int) (p.x - 25), (int) (p.y - 25), 50, 50);
            
            // Paint last scanned position of target
            RadarTarget t = map.targets.get(i);
			g.setColor(Color.orange);
			g.drawOval((int) (t.coords.x - 25), (int) (t.coords.y - 25), 50,
                    50);
		}
	}

    @Override public void onHitWall(HitWallEvent e)
    {
        setMaxTurnRate(Rules.MAX_TURN_RATE);
        double turning = PI / 2 - Math.abs(e.getBearingRadians());
        if (e.getBearingRadians() > 0)
            setTurnRightRadians(turning);
        else
            setTurnLeftRadians(turning);
        setBack(Rules.DECELERATION);
        doNothing();
    }

    @Override public void onHitRobot(HitRobotEvent e)
	{
        // TODO: Analyse self and enemy energy and decise wherther to ram him
        // or not.
		setBack(500);
	}
}