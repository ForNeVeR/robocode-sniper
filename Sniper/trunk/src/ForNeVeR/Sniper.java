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

		out.println("Sniper reporting.");

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

                // TODO: Proceed refactoring here.
				double turningRadians = normalRelativeAngle(bearingToEnemy - getGunHeadingRadians());
				double bulletPower = firePower(t);

				double bulletRadius = 0; // Distance travelled by our bullet

				for(long time = 0; bulletRadius < distance || turningRadians > Rules.GUN_TURN_RATE * time; time++)
				{
					bulletRadius += Rules.getBulletSpeed(bulletPower);
					future_x += t.velocity * Math.sin(t.heading);
					future_y += t.velocity * Math.cos(t.heading);

					distance = Math.sqrt(Math.pow(future_x - getX(), 2) + Math.pow(future_y - getY(), 2));
					bearingToEnemy = Math.atan2(future_x - getX(), future_y - getY());
					turningRadians = normalRelativeAngle(bearingToEnemy - getGunHeadingRadians());

					if(future_x < 0 || future_x > getBattleFieldWidth() || future_y < 0 || future_y > getBattleFieldHeight())
						break;
				}

				setTurnGunRightRadians(turningRadians);

				// Now we have to plan moving.
				// Estimate current enemy coordinates:
				double enemyX = t.x + t.velocity * Math.sin(t.heading) * (getTime() - t.time);
				double enemyY = t.y + t.velocity * Math.cos(t.heading) * (getTime() - t.time);

				distance = Math.sqrt(Math.pow(enemyX - getX(), 2) + Math.pow(enemyY - getY(), 2));
				bearingToEnemy = Math.atan2(enemyX - getX(), enemyY - getY());

				if(distance > DISTANCE_TO_ENEMY + DELTA_DISTANCE)
				{
					double targetX = enemyX + DISTANCE_TO_ENEMY * Math.sin(bearingToEnemy + Math.PI / 2);
					double targetY = enemyY + DISTANCE_TO_ENEMY * Math.cos(bearingToEnemy + Math.PI / 2);
					double angleToTarget = normalRelativeAngle(Math.atan2(targetX - getX(), targetY - getY()) - getHeadingRadians());

					double targetX_2nd = enemyX + DISTANCE_TO_ENEMY * Math.sin(bearingToEnemy - Math.PI / 2);
					double targetY_2nd = enemyY + DISTANCE_TO_ENEMY * Math.cos(bearingToEnemy - Math.PI / 2);
					double angleToTarget_2nd = normalRelativeAngle(Math.atan2(targetX_2nd - getX(), targetY_2nd - getY()) - getHeadingRadians());

					if(targetX < 0 || targetX > getBattleFieldWidth() || targetY < 0 || targetY > getBattleFieldHeight()
						|| Math.abs(angleToTarget_2nd) < Math.abs(angleToTarget))
					{
						targetX = targetX_2nd;
						targetY = targetY_2nd;
						angleToTarget = angleToTarget_2nd;
					}

					Graphics2D g = getGraphics();
					g.setColor(Color.yellow);
					g.drawOval((int) (targetX - 25), (int) (targetY - 25), 50, 50);

					setTurnRightRadians(angleToTarget);
					setAhead(Geometry.distanceBetween(getX(), getY(), targetX, targetY));
				}
				else if(distance < DISTANCE_TO_ENEMY - DELTA_DISTANCE)
				{
					setBack(DISTANCE_TO_ENEMY - distance);
				}
				else
				{
					double bearing = normalRelativeAngle(bearingToEnemy - getHeadingRadians() + Math.PI / 2);
					if(Math.abs(bearing) > Math.PI)
						bearing = normalRelativeAngle(bearingToEnemy - getHeadingRadians() - Math.PI / 2);

					Graphics2D g = getGraphics();
					g.setColor(Color.yellow);
					g.drawLine((int)getX(), (int)getY(), (int)(getX() + 50 * Math.sin(bearing + getHeadingRadians())), (int)(getY() + 50 * Math.cos(bearing + getHeadingRadians())));

					setTurnRightRadians(bearing);
					setAhead(2 * DISTANCE_TO_ENEMY * Math.PI * heading);
				}

				//double angle = Utils.normalRelativeAngle(bearingToEnemy - getHeadingRadians());


				/*if(distance < DISTANCE_TO_ENEMY - DELTA_DISTANCE)
				{
					currentX += distance Utils.
					setTurnRightRadians(angle);
					if(Math.abs(angle) < Math.PI / 2)
						setBack(DISTANCE_TO_ENEMY - distance);
					else
						setAhead(DISTANCE_TO_ENEMY - distance);
				}
				else if(distance > DISTANCE_TO_ENEMY + DELTA_DISTANCE)
				{
					setTurnRightRadians(angle);
					if(Math.abs(angle) < Math.PI / 2)
						setAhead(distance - DISTANCE_TO_ENEMY);
					else
						setBack(distance - DISTANCE_TO_ENEMY);
				}
				else // DISTANCE_TO_ENEMY - DELTA_DISTANCE < distance < DISTANCE_TO_ENEMY + DELTA_DISTANCE
				{
					double l_angle = Utils.normalRelativeAngle(angle - Math.PI / 2);
					double r_angle = Utils.normalRelativeAngle(angle + Math.PI / 2);

					if(Math.abs(l_angle) < Math.abs(r_angle))
						angle = l_angle;
					else
						angle = r_angle;
					setTurnRightRadians(angle);
					setAhead(DISTANCE_TO_ENEMY * Math.PI * heading);
				}*/

				if(getGunHeat() == 0 && Math.abs(getGunTurnRemainingRadians()) < Math.atan2(20, distance))
					fire(bulletPower);
				else
					doNothing();
			}
			else
			{
				future_x = getX();
				future_y = getY();
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
		double bearing = e.getBearingRadians();
        double distance = e.getDistance();

        double x = getX() + distance * Math.sin(getHeadingRadians() + bearing);
        double y = getY() + distance * Math.cos(getHeadingRadians() + bearing);

        map.setTarget(e.getName(), getTime(), x, y, e.getHeadingRadians(), e.getVelocity());
    }

    @Override public void onRobotDeath(RobotDeathEvent e)
    {
        map.removeTarget(e.getName());
    }

	@Override public void onPaint(Graphics2D g)
	{
		g.setColor(Color.red);
		g.drawOval((int) (future_x - 25), (int) (future_y - 25), 50, 50);

		if(currentTarget != null)
		{
			double currentX = currentTarget.x + currentTarget.velocity * Math.sin(currentTarget.heading) * (getTime() - currentTarget.time);
			double currentY = currentTarget.y + currentTarget.velocity * Math.cos(currentTarget.heading) * (getTime() - currentTarget.time);

			g.setColor(Color.green);
			g.drawOval((int) (currentX - (DISTANCE_TO_ENEMY - DELTA_DISTANCE)), (int) (currentY - (DISTANCE_TO_ENEMY - DELTA_DISTANCE)),
		 		(int)(DISTANCE_TO_ENEMY - DELTA_DISTANCE) * 2, (int)(DISTANCE_TO_ENEMY - DELTA_DISTANCE) * 2);
			g.drawOval((int) (currentX - (DISTANCE_TO_ENEMY + DELTA_DISTANCE)), (int) (currentY - (DISTANCE_TO_ENEMY + DELTA_DISTANCE)),
		 		(int)(DISTANCE_TO_ENEMY + DELTA_DISTANCE) * 2, (int)(DISTANCE_TO_ENEMY + DELTA_DISTANCE) * 2);
		}

		for(int i = 0; i < map.targets.size(); i++)
		{
            // Paint estimated position of target
			RadarTarget t = map.targets.get(i).estimatePositionAt(getTime());
			g.setColor(Color.blue);
			g.drawOval((int) (t.x - 25), (int) (t.y - 25), 50, 50);
            
            // Paint last scanned position of target
            t = map.targets.get(i);
			g.setColor(Color.orange);
			g.drawOval((int) (t.x - 25), (int) (t.y - 25), 50, 50);
		}
	}

	/*public void onHitByBullet(HitByBulletEvent e)
	{
		// Move ahead 100 and in the same time turn left papendicular to the bullet
		setTurnRightRadians(Math.PI / 2 - e.getBearingRadians());
		setAhead(100);
	}*/

	@Override public void onHitRobot(HitRobotEvent e)
	{
        // TODO: Analyse self and enemy energy and decise wherther to ram him
        // or not.
		setBack(500);
	}

	@Override public void onHitWall(HitWallEvent e)
	{
		heading = -heading;
    }
}