/**
 * This class includes methods that will help the robot navigate to the waypoints
 * set at the beginning of the demo
 * 
 * @author Lara Kollokian, 260806317
 * @author Julien Phillips, 260804197
 */

package ca.mcgill.ecse211.navigation;

import ca.mcgill.ecse211.lab3.Controller;
import ca.mcgill.ecse211.lab3.Lab3;
import ca.mcgill.ecse211.navigation.Odometer;
import ca.mcgill.ecse211.navigation.OdometerExceptions;

public class Navigation 
{
	private Odometer odo;
	
	private boolean travelToIsRunning = false;
	private boolean turnToIsRunning = false;
	
	private static final int FWD_SPEED = 150;
	private static final int TURN_SPEED = 100;
	private static final double TILE_SIZE = 30.48;
	private static final double ANGLE_RANGE = 1.5;
	private static final double RAD_TO_DEG = 57.2958;
		
	/**
	 * default constructor
	 * @throws OdometerExceptions
	 */
	public Navigation() throws OdometerExceptions
	{
		this.odo = Odometer.getOdometer();
	}
		
	/**
	 * This method causes the robot to travel to the absolute field location (x, y), specified in tile 
	 * points. This method continuously calls turnTo(double theta) and then sets the motor speed to 
	 * forward(straight). This will make sure that the heading is updated until the robot reaches the 
	 * exact goal. This method polls the odometer for information.
	 * 
	 * @param x double, x coordinate to travel to
	 * @param y double, y coordinate to travel to
	 */
	public void travelTo(double x, double y)
	{
		travelToIsRunning = true;
		double nextX = x*TILE_SIZE;
		double nextY = y*TILE_SIZE;
		double[] currentPos = odo.getXYT();
		double currX = currentPos[0];
		double currY = currentPos[1];
		double nextAngle;
		
		if ((nextX < currX+ANGLE_RANGE && nextX > currX-ANGLE_RANGE) && nextY < currY ) { //robot must travel at theta = 180
			nextAngle = 180;
			turnTo(nextAngle);	
			
		}
		else if ((nextX < currX+ANGLE_RANGE && nextX > currX-ANGLE_RANGE) && nextY > currY ) { //robot must travel at theta = 0
			nextAngle = 0;
			turnTo(nextAngle);	
		}
		else if ((nextY < currY+ANGLE_RANGE && nextY > currY-ANGLE_RANGE) && nextX < currX) { //robot must travel at theta = 270
			nextAngle = 270;
			turnTo(nextAngle);	
		}
		else if((nextY < currY+ANGLE_RANGE && nextY > currY-ANGLE_RANGE) && nextX > currX) { //robot must travel at theta = 90
			nextAngle = 90;
			turnTo(nextAngle);	
		}
		else if (nextX > currX && nextY > currY) //robot must travel at some angle within first quadrant
		{
			nextAngle = Math.atan(Math.abs(nextX-currX)/Math.abs(nextY-currY));
			turnTo(nextAngle*RAD_TO_DEG);		
		}
		else if (nextX > currX && nextY < currY) //robot must have to travel at some angle within the second quadrant
		{
			nextAngle = Math.atan(Math.abs(nextY-currY)/Math.abs(nextX-currX));
			turnTo(90 + nextAngle*RAD_TO_DEG);		
		}
		else if (nextX < currX && nextY < currY) //robot must travel at some angle within the third quadrant
		{
			nextAngle = Math.atan(Math.abs(nextX-currX)/Math.abs(nextY-currY));
			turnTo(180 + nextAngle*RAD_TO_DEG);		
		}
		else if (nextX <= currX && nextY >= currY) //fourth
		{
			nextAngle = Math.atan(Math.abs(nextY-currY)/Math.abs(nextX-currX));
			turnTo(270 + nextAngle*RAD_TO_DEG);		
		}	
		
		while(turnToIsRunning)
		{
			if(!turnToIsRunning)
			{
				break;
			}
		}

		double distance = Math.sqrt(Math.pow((nextX-currX), 2) + Math.pow((nextY-currY), 2));
		Lab3.leftMotor.setSpeed(FWD_SPEED);
		Lab3.rightMotor.setSpeed(FWD_SPEED);
		Lab3.leftMotor.rotate(Controller.convertDistance(Lab3.WHEEL_RAD, distance), true);
		Lab3.rightMotor.rotate(Controller.convertDistance(Lab3.WHEEL_RAD, distance), false);
		travelToIsRunning = false;
	}

	/**
	 * This method causes the robot to turn (on point) to the absolute heading theta. This method
	 * turns the minimal angle to its target.
	 * 
	 * @param theta double, angle to turn to get to destination
	 */
	void turnTo(double theta)
	{
		turnToIsRunning = true;
		double[] position = odo.getXYT();
		double currAngle = position[2];
		
		if(theta > currAngle)
		{
			if(theta-currAngle < 180) //turn clockwise
			{
				while(currAngle >= theta+ANGLE_RANGE || currAngle <= theta-ANGLE_RANGE)
				{
					Lab3.leftMotor.setSpeed(TURN_SPEED);
					Lab3.rightMotor.setSpeed(TURN_SPEED);
					Lab3.leftMotor.forward();
					Lab3.rightMotor.backward();
					position = odo.getXYT();
					currAngle = position[2];
				}
			}
			else //turn counterclockwise
			{
				while(currAngle >= theta+ANGLE_RANGE || currAngle <= theta-ANGLE_RANGE)
				{
					Lab3.leftMotor.setSpeed(TURN_SPEED);
					Lab3.rightMotor.setSpeed(TURN_SPEED);
					Lab3.leftMotor.backward();
					Lab3.rightMotor.forward();
					position = odo.getXYT();
					currAngle = position[2];
				}
			}
		}
		else
		{
			if(currAngle-theta < 180) //turn counterclockwise
			{
				while(currAngle >= theta+ANGLE_RANGE || currAngle <= theta-ANGLE_RANGE)
				{
					Lab3.leftMotor.setSpeed(TURN_SPEED);
					Lab3.rightMotor.setSpeed(TURN_SPEED);
					Lab3.leftMotor.backward();
					Lab3.rightMotor.forward();
					position = odo.getXYT();
					currAngle = position[2];
				}
			}
			else //turn clockwise
			{
				while(currAngle >= theta+ANGLE_RANGE || currAngle <= theta-ANGLE_RANGE)
				{
					Lab3.leftMotor.setSpeed(TURN_SPEED);
					Lab3.rightMotor.setSpeed(TURN_SPEED);
					Lab3.leftMotor.forward();
					Lab3.rightMotor.backward();
					position = odo.getXYT();
					currAngle = position[2];
				}
			}
		}
		
		turnToIsRunning = false;
	}
	
	/**
	 * This method returns true if another thread has called travelTo() or turnTo() and the 
	 * method has yet to return; false otherwise.
	 * 
	 * @return boolean
	 */
	public boolean isNavigating()
	{
		if(travelToIsRunning || turnToIsRunning)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
