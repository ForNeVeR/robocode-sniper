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
	private final static double DISTANCE_TO_ENEMY = 100;
	private final static double DELTA_DISTANCE = 35;

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

		while(true)
        {
			if(getRadarTurnRemaining() == 0)
	            setTurnRadarRightRadians(PI * 2);

			RadarTarget target = map.getNearestTarget(getX(), getY());

			if(target != null)
			{
				// Determine moving.
				double distanceToEnemy = distanceBetween(target.coords, getX(),
                        getY());
				double bearingToEnemy = Math.atan2(target.coords.x - getX(),
                        target.coords.y - getY());

                if(distanceToEnemy > DISTANCE_TO_ENEMY + DELTA_DISTANCE)
                {
                    // We have 2 points to move to: to the left side of enemy
                    // and to the right side of him. We have to determine which
                    // point to use.
                    Point leftPoint = movePointByVector(target.coords,
                            DISTANCE_TO_ENEMY, bearingToEnemy - PI / 2);
                    Point rightPoint = movePointByVector(target.coords,
                            DISTANCE_TO_ENEMY, bearingToEnemy + PI / 2);
                    // TODO: Somehow check both points. Temporary solution is
                    // just always use left point.
                    setMoveToPoint(leftPoint);
                }
                else if(distanceToEnemy < DISTANCE_TO_ENEMY - DELTA_DISTANCE)
                {
                    // TODO: Check if we are ramming enemy. Else:
                    setTurnRight(normalRelativeAngle(bearingToEnemy
                            - getHeading()));
                    setBack(DISTANCE_TO_ENEMY - distanceToEnemy);
                }
                else
                {
                    // Cycling maneuver.
                    // Again, we have 2 possible bearings to turn to: clockwise
                    // or counter-clockwise.
                    double bearingCW = normalRelativeAngle(bearingToEnemy
                            - getHeading() + PI / 2);
                    double bearingCCW = normalRelativeAngle(bearingToEnemy
                            - getHeading() - PI / 2);
                    // TODO: Determine appropriate bearing for moving.
                    setTurnRight(bearingCW);
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

                // Enemy position modelling.
				double bulletRadius = 0; // distance travelled by our bullet
                double imaginaryDistanceToEnemy = distanceToEnemy;
				for(long time = 0; bulletRadius < imaginaryDistanceToEnemy ||
                        turnByAngle > Rules.GUN_TURN_RATE * time; time++)
				{
					bulletRadius += Rules.getBulletSpeed(bulletPower);
					
                    Point imaginaryTarget = target.estimatePositionAt(getTime()
                            + time);
                    imaginaryDistanceToEnemy = distanceBetween(imaginaryTarget,
                            getX(), getY());
					double imaginaryBearingToEnemy =
                            Math.atan2(imaginaryTarget.x - getX(),
                            imaginaryTarget.y - getY());
					turnByAngle = normalRelativeAngle(imaginaryBearingToEnemy
                            - getGunHeadingRadians());

					if(imaginaryTarget.x < 0
                            || imaginaryTarget.x > getBattleFieldWidth()
                            || imaginaryTarget.y < 0
                            || imaginaryTarget.y > getBattleFieldHeight())
						break;
				}

				setTurnGunRightRadians(turnByAngle);

				if(getGunHeat() == 0
                        && Math.abs(getGunTurnRemainingRadians()) < Math.atan2(
                        20, imaginaryDistanceToEnemy))
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
     * Calculates firepower needed to fire to target.
     * @param t RadarTarget object represents target.
     * @return Preferred firepower.
     */
	private double firePower(RadarTarget t)
	{
		double distance = distanceBetween(t.coords, getX(), getY());

		double power = 0;
		if(distance < 250)
			power = Rules.MAX_BULLET_POWER;
		else
			power = 250 / distance * Rules.MAX_BULLET_POWER;

		return Math.max(power, 0.5);
	}

    /**
     * Starts movement to specified point.
     * @param p Point to move to.
     */
    private void setMoveToPoint(Point p)
    {
        double distanceToPoint = distanceBetween(p, getX(), getY());
        double relativeBearingToPoint = normalRelativeAngle(Math.atan2(p.x
                - getX(), p.y - getY()) - getHeading());

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
		Point myPos = new Point(getX(), getY());
        Point enemyPos = movePointByVector(myPos, e.getDistance(),
                e.getBearingRadians());

        map.setTarget(e.getName(), getTime(), enemyPos.x, enemyPos.y,
                e.getHeadingRadians(), e.getVelocity());
    }

    @Override public void onRobotDeath(RobotDeathEvent e)
    {
        map.removeTarget(e.getName());
    }

	@Override public void onPaint(Graphics2D g)
	{
        RadarTarget currentTarget = map.getNearestTarget(getX(), getY());

		if(currentTarget != null)
		{
			Point currentPos = movePointByVector(currentTarget.coords,
                    currentTarget.velocity * (getTime() - currentTarget.time),
                    currentTarget.heading);

			g.setColor(Color.green);
			g.drawOval((int) (currentPos.x - (DISTANCE_TO_ENEMY
                    - DELTA_DISTANCE)), (int) (currentPos.y
                    - (DISTANCE_TO_ENEMY - DELTA_DISTANCE)),
                    (int) (DISTANCE_TO_ENEMY - DELTA_DISTANCE) * 2,
                    (int)(DISTANCE_TO_ENEMY - DELTA_DISTANCE) * 2);
			g.drawOval((int) (currentPos.x - (DISTANCE_TO_ENEMY
                    + DELTA_DISTANCE)), (int) (currentPos.y
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

    @Override public void onHitRobot(HitRobotEvent e)
	{
        // TODO: Analyse self and enemy energy and decise wherther to ram him
        // or not.
		setBack(500);
	}
}