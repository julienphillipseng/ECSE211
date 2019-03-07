package ca.mcgill.ecse211.localization;

import ca.mcgill.ecse211.game.RingChallenge;
import ca.mcgill.ecse211.odometry.Odometer;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class contains every method that is useful for the robot's navigation. This means navigation to a specific 
 * point, rotation to a specific angle or avoiding obstacles. It also contains useful methods that convert distances 
 * to wheel rotations and angles to distances, which are useful for navigation. Finally, there is also a method to 
 * calculate the closest gridpoint to the robots location.
 * 
 * @author Romain, Lara, Matthew
 *
 */
public class Navigation {
	
	private static Odometer odo;
	
	public static final int DISTANCE_RANGE = 4;
	public static final int CORRECTION_PERIOD = 1;	//how many tiles the robot travels (taxiTravelTo) before the robot corrects itself
	
	
	public static void setOdometer(Odometer odometer) {
		Navigation.odo = odometer;
	}
	
	
	/**
	 * Travels to the specified coordinates parallel to the x and y axes. If odometer correction is enabled, the robot will physically correct itself at each
	 * perpendicular line on its path.
	 * @param x continuous x coordinate in cm
	 * @param y continuous y coordinate in cm
	 * @param xFirst boolean which indicates if the robot will travel along the x axis first
	 * @param isCorrected odometry correction is enabled
	 */
	public static void taxiTravelTo(double x, double y, boolean xFirst, boolean isCorrected) {
		double[] XYT = odo.getXYT();
		
		//if no correction required
		if(!isCorrected) {
			if(xFirst)
				travelTo(x, XYT[1], false);
			else
				travelTo(XYT[0], y, false);
			travelTo(x, y, false);
			
			return;
		}
		
		//if correction required
		boolean goingRight = x > XYT[0];
		boolean goingUp = y > XYT[1];
		int[] coords = getClosestCoordinates(XYT[0], XYT[1]);
		
		double nextX = (coords[0] + CORRECTION_PERIOD * (goingRight ? 1 : -1)) * RingChallenge.TILE_SIZE;
		double nextY = (coords[1] + CORRECTION_PERIOD * (goingUp ? 1 : -1)) * RingChallenge.TILE_SIZE;
		
		if(xFirst) {
			while(goingRight ? (nextX < x) : (nextX > x)) {
				travelTo(nextX, XYT[1], true);
				nextX += CORRECTION_PERIOD * (goingRight ? RingChallenge.TILE_SIZE : -RingChallenge.TILE_SIZE);
			}
			if(Math.abs(x - odo.getXYT()[0]) > DISTANCE_RANGE) {
				travelTo(x, XYT[1], true);
			}
			while(goingUp ? (nextY < y) : (nextY > y)) {
				travelTo(x, nextY, true);
				nextY += CORRECTION_PERIOD * (goingUp ? RingChallenge.TILE_SIZE : -RingChallenge.TILE_SIZE);
			}
			if(Math.abs(y - odo.getXYT()[1]) > DISTANCE_RANGE) {
				travelTo(x, y, true);
			}
		}
		else {
			while(goingUp ? (nextY < y) : (nextY > y)) {
				travelTo(XYT[0], nextY, true);
				nextY += CORRECTION_PERIOD * (goingUp ? RingChallenge.TILE_SIZE : -RingChallenge.TILE_SIZE);
			}
			if(Math.abs(y - odo.getXYT()[1]) > DISTANCE_RANGE)
				travelTo(XYT[0], y, true);
			while(goingRight ? (nextX < x) : (nextX > x)) {
				travelTo(nextX, y, true);
				nextX += CORRECTION_PERIOD * (goingRight ? RingChallenge.TILE_SIZE : -RingChallenge.TILE_SIZE);
			}
			if(Math.abs(x - odo.getXYT()[0]) > DISTANCE_RANGE)
				travelTo(x, y, true);
		}
	}
	
	/**
	 * Taxi travel to, but takes grid intersection coordinates
	 * @param x x grid coordinate
	 * @param y y grid coordinate
	 * @param xFirst robot will travel along the x axis first
	 * @param isCorrected odometry correction is enabled
	 */
	public static void taxiTravelTo(int x, int y, boolean xFirst, boolean isCorrected) {
		taxiTravelTo(x * RingChallenge.TILE_SIZE, y * RingChallenge.TILE_SIZE, xFirst, isCorrected);
	}
	
	/**
	 * Travels to the specified set of continuous coordinates
	 * @param x double x continuous coordinate
	 * @param y double y continuous coordinate
	 * @param isCorrected boolean which indicates if the odometry correction is active
	 */
	public static void travelTo(double x, double y, boolean isCorrected) {

		double[] XYZ = odo.getXYT();
		//get dX and dY to arrive at next point
		double dX = x - XYZ[0];
		double dY = y - XYZ[1];
		//calculate angle to new point in radians
		double newTheta = Math.atan2(dX, dY);
		//calculate distance to new point
		double distance = Math.sqrt(dX * dX + dY * dY);
		
		turnTo(180 * newTheta / Math.PI);
		if(isCorrected)
			odo.enableCorrection();
		travelDistance(distance);
		odo.disableCorrection();
	}
	
	
	/**
	 * Travels to the specified set of grid intersection coordinates
	 * @param x integer x intersection coordinate
	 * @param y	integer y intersection coordinate
	 * @param isCorrected boolean which indicates if the odometry correction is active
	 */
	public static void travelTo(int x, int y, boolean isCorrected) {
		travelTo(x * RingChallenge.TILE_SIZE, y * RingChallenge.TILE_SIZE, isCorrected);
	}
	
	
	/**
	 * Sets the motors to forward and makes the wheels rotate the appropriate amount to get to 
	 * the desired distance 
	 * 
	 * @param distance double distance to be travelled by robot
	 */
	public static void travelDistance(double distance)
	{
		setMotorSpeeds(RingChallenge.MOTOR_SPEED, RingChallenge.leftMotor, RingChallenge.rightMotor,
				RingChallenge.L_WHEEL_RAD, RingChallenge.R_WHEEL_RAD);
		RingChallenge.leftMotor.rotate(convertDistance(RingChallenge.L_WHEEL_RAD, distance), true);
		RingChallenge.rightMotor.rotate(convertDistance(RingChallenge.R_WHEEL_RAD, distance), false);
	}
	
	/**
	 * Causes the robot to rotate to the angle theta by minimal angle to its target
	 * 
	 * @param theta double angle to turn to in degrees
	 */
	public static void turnTo (double theta) {	//assumes theta is in degrees and is >= -360

		//get change in theta required by turn in degrees
		double[] XYT = odo.getXYT();
		theta = (theta + 360) % 360;
		double dTheta = (theta - XYT[2] + 360) % 360;
		
		//correct dTheta so that it defines the shortest turn
		if(dTheta > 180) {
			dTheta -= 360;
		}
		
		//perform turn
		setMotorSpeeds(RingChallenge.MOTOR_SPEED, RingChallenge.leftMotor, RingChallenge.rightMotor,
				RingChallenge.L_WHEEL_RAD, RingChallenge.R_WHEEL_RAD);
		RingChallenge.leftMotor.rotate(convertAngle(RingChallenge.L_WHEEL_RAD, RingChallenge.TRACK, dTheta), true);
		RingChallenge.rightMotor.rotate(-convertAngle(RingChallenge.R_WHEEL_RAD, RingChallenge.TRACK, dTheta), false);
	}
	
	/**
	 * Finds the angle towards which the robot must turn to travel either in X or Y directions
	 * 
	 * @param axis X or Y axis
	 * @param curXY current X or Y coordinate (depending on set axis)
	 * @param nextXY next X or Y coordinate (depending on set axis)
	 * @return int the angle the robot should turn to
	 */
	public static int findTravelAngle(String axis, double curXY, double nextXY)
	{
		int angle = -1;
		if(axis.equals("x"))
		{
			if(nextXY < curXY - DISTANCE_RANGE) //next x is smaller (to the left)
			{
				angle = 270; //turn to the left -> 270 degrees
			}
			else if(nextXY > curXY + DISTANCE_RANGE) //next x is bigger (to the right)
			{
				angle = 90; //turn to the right -> 90 degrees
			}
			else //angle is relatively equal
			{
				angle = -1; //don't turn
			}
		}
		else if(axis.equals("y"))
		{
			if(nextXY < curXY - DISTANCE_RANGE) //next y is smaller (downwards)
			{
				angle = 180; //turn to south -> 180 degrees
			}
			else if(nextXY > curXY + DISTANCE_RANGE) //next y is bigger (upwards)
			{
				angle = 0; //turn to north -> 0 degrees
			}
			else //angle is relatively equal
			{
				angle = -1; //don't turn
			}
		}
		else
		{
			System.out.println("wrong axis");
		}
		return angle;
	}

	/**
	 * returns the quadrant that the right light sensor is in when the robot is on an intersection
	 * @param angle angle of the robot
	 * @return quadrant the right light sensor is in (0 = bottom left, 1 = bottom right, 2 = top right, 2 = top left
	 */
	public static int getQuadrant(double angle) {
		angle -= Math.atan((RingChallenge.LS_WIDTH / 2) / RingChallenge.LS_L_DISTANCE);
		if(angle < 360 && angle >= 270) {
			return 1;
		} else if(angle < 90 && angle >= 0) {
			return 0;
		} else if(angle < 180 && angle >= 90) {
			return 3;
		} else{
			return 2;
		}
	}
	
	/**
	 * This method allows the conversion of a distance to the total rotation of each wheel need to
	 * cover that distance.
	 * 
	 * @param radius
	 * @param distance
	 * @return number of wheel rotations in degrees
	 */
	public static int convertDistance(double radius, double distance) {
	    return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	/**
	 * This method allows the conversion of an angle required for the robot to turn to the total rotation of
	 * each wheel to cover that turn.
	 * 
	 * @param radius
	 * @param width
	 * @param angle
	 * @return number of wheel rotations in degrees
	 */
	public static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	
	/**
	 * Converts a pair of continuous coordinates into the closest grid coordinates
	 * @param x
	 * @param y
	 * @return integer array of grid coordinates [x,y]
	 */
	public static int[] getClosestCoordinates(double x, double y) {
		int[] coordinates = new int[2];
		coordinates[0] = (int) Math.round(x / RingChallenge.TILE_SIZE);
		coordinates[1] = (int) Math.round(y / RingChallenge.TILE_SIZE);
		return coordinates;
	}
	
	/**
	 * Sets the motor angular speeds of the left and right wheels according to whichever wheel travels faster at that speed. Used to
	 * synchronize linear wheel speeds for straight driving and turning in place.
	 * @param speed desired motor speed
	 * @param leftMotor
	 * @param rightMotor
	 * @param l_wheel_rad left wheel radius
	 * @param r_wheel_rad right wheel radius
	 */
	public static void setMotorSpeeds(int speed, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double l_wheel_rad,
			double r_wheel_rad) {
		if(l_wheel_rad > r_wheel_rad) {
			leftMotor.setSpeed(speed);
			rightMotor.setSpeed(Math.round(speed * l_wheel_rad / r_wheel_rad));
		}
		else {
			leftMotor.setSpeed(Math.round(speed * r_wheel_rad / l_wheel_rad));
			rightMotor.setSpeed(speed);
		}
	}
}
