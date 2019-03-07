/**
 * This class contains the methods that direct the movements of the robot relative to the external grid
 * @author Matthew Kourlas
 */
package ca.mcgill.ecse211.navigation;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation {
	
	private static Odometer odometer;
	
	private static final int FORWARD_SPEED = 200;
	private static final int ROTATE_SPEED = 150;
	
	private static double WHEEL_RAD;
	private static double TRACK;
	private static double TILE_SIZE;
	
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	
	private boolean navigating;
	
	/**
	 * Constructor for the Navigation class
	 * @param leftMotor
	 * @param rightMotor
	 * @param WHEEL_RAD
	 * @param TRACK
	 * @param TILE_SIZE
	 * @param odometer
	 */
	public Navigation(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
	  double WHEEL_RAD, double TRACK, double TILE_SIZE, Odometer odometer) {
		
		Navigation.leftMotor = leftMotor;
		Navigation.rightMotor = rightMotor;
		Navigation.WHEEL_RAD = WHEEL_RAD;
		Navigation.TRACK = TRACK;
		Navigation.TILE_SIZE = TILE_SIZE;
		Navigation.odometer = odometer;
	}
	
	
	/**
	 * This method turns the robot to theta radians the shortest way. The method does not
	 * return until the turn is complete.
	 * @param theta
	 */
	public void turnTo (double theta) {	//assumes theta is in radians
		
		navigating = true;
		
		//get change in theta required by turn in degrees
		double[] XYT = odometer.getXYT();
		theta *= 180 / Math.PI;
		double dTheta = theta - XYT[2];
		
		//correct dTheta so that it defines the shortest turn
		if(XYT[2] < 180 && dTheta > 180) {
			dTheta -= 360;
		}
		else if(XYT[2] > 180 && dTheta < -180) {
			dTheta += 360;
		}
		
		//perform turn
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.rotate(convertAngle(WHEEL_RAD, TRACK, dTheta), true);
		rightMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, dTheta), false);
	}
	
	/**
	 * This method makes the robot travel to grid position (x,y). The method does not return
	 * until the robot has finished moving.
	 * @param x
	 * @param y
	 */
	public void travelTo (double x, double y) {
		
		//indicate robot is navigating
		navigating = true;
		
		double[] XYZ = odometer.getXYT();
		
		//get dX and dY to arrive at next point
		double dX = TILE_SIZE * (double) x - XYZ[0];
		double dY = TILE_SIZE * (double) y - XYZ[1];
		double newTheta;
		
		//calculate angle to new point in radians
		if(dY == 0)
			newTheta = dX > 0 ? Math.PI / 2 : -Math.PI / 2;
		else if(dY > 0)
			newTheta = Math.atan(dX/dY);
		else
			newTheta = Math.atan(dX/dY) + Math.PI;
		double distance = Math.sqrt(dX * dX + dY * dY);
		
		//turn to calculated angle
		turnTo(newTheta);
		
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.rotate(convertDistance(WHEEL_RAD, distance), true);
		rightMotor.rotate(convertDistance(WHEEL_RAD, distance), false);
		
		//indicate robot is done moving
		navigating = false;
	}
	

	/**
	 * This method converts a distance to wheel turns in degrees using the wheel radius.
	 * @param radius
	 * @param distance
	 * @return wheel turns in degrees
	 */
	public static int convertDistance(double radius, double distance) {
	    return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	/**
	 * This method converts an angle the robot needs to turn to wheel rotations in degrees.
	 * @param radius
	 * @param width
	 * @param angle
	 * @return wheel turns in degrees
	 */
	public static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	
	//
	/**
	 * This method returns if the robot is navigation (ie not idle).
	 * @return isNavigating boolean
	 */
	public boolean isNavigating() {
		return navigating;
	}
}
