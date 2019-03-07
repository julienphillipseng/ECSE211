package ca.mcgill.ecse211.lab3;

import ca.mcgill.ecse211.navigation.Navigation;
import ca.mcgill.ecse211.navigation.OdometerExceptions;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class is used to drive the robot on the demo floor.
 */
public class Controller //implements UltrasonicController
{	
	//this is where you set the waypoints before the demo
	//map 1 = {{0, 2}, {1, 1}, {2, 2}, {2, 1}, {1, 0}};
	//map 2 = {{1, 1}, {0, 2}, {2, 2}, {2, 1}, {1, 0}};
	//map 3 = {{1, 0}, {2, 1}, {2, 2}, {0, 2}, {1, 1}};
	//map 4 = {{0, 1}, {1, 2}, {1, 0}, {2, 1}, {2, 2}};
	public static final int[][] waypoints = {{1, 1}, {0, 2}, {2, 2}, {2, 1}, {1, 0}};

	/**
	 * This method is meant to drive the robot in a square of size 2x2 Tiles. It is to run in parallel
	 * with the odometer and Odometer correcton classes allow testing their functionality.
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @param leftRadius
	 * @param rightRadius
	 * @param width
	 */
	public static void drive(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			double leftRadius, double rightRadius, double track) 
	{
		// reset the motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) 
		{
			motor.stop();
			motor.setAcceleration(3000);
		}
		// Sleep for 2 seconds
		try 
		{
			Thread.sleep(2000);
		} 
		catch (InterruptedException e) 
		{
			// There is nothing to be done here
		}
		
		//instantiate a navigation object
		Navigation nav = null;
		try {
			nav = new Navigation();
		} catch (OdometerExceptions e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// CODE TO NAVIGATE ROBOT TO COORDINATES  
		int trajectory = waypoints.length;
		for(int i = 0; i < trajectory; i++)
		{
			nav.travelTo(waypoints[i][0], waypoints[i][1]);
		}
	}

	/**
	 * This method allows the conversion of a distance to the total rotation of each wheel need to
	 * cover that distance.
	 * 
	 * @param radius
	 * @param distance
	 * @return
	 */
	public static int convertDistance(double radius, double distance) 
	{
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	public static int convertAngle(double radius, double width, double angle) 
	{
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}
