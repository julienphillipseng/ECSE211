package ca.mcgill.ecse211.localization;

import ca.mcgill.ecse211.game.RingChallenge;
import ca.mcgill.ecse211.interfaces.LightSensorUser;
import ca.mcgill.ecse211.odometry.Odometer;
import ca.mcgill.ecse211.sensor.LineDetector;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class contains methods that allow the robot to perform light localization. It also implements the 
 * LightSensorUser interface, therefore there is the processLSData method. 
 * 
 * @author Matthew, Romain
 *
 */
public class LightLocalization implements LightSensorUser {
	
	private static final int THRESHOLD = 30;
	private static final int WINDOW_SIZE = 5;
	private static final int ROTATE_SPEED = 120;
	
	private static final int[] CORRECTION_ANGLES = {-9, -9, -8, -6};
	
	private static double TRACK;
	private static double L_WHEEL_RAD;
	private static double R_WHEEL_RAD;
	private static double LS_W_DISTANCE;
	private static double LS_L_DISTANCE;
	
	private double angleXp;
	private double angleXn;
	private double angleYp;
	private double angleYn;
	
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private Odometer odometer;
	
	private int lineCounter;
	private boolean inLine;
	private boolean localizing = false;
	
	private LineDetector lineDetector = new LineDetector(WINDOW_SIZE, THRESHOLD);
	
	
	/**
	 * Constructor for the LightLocalization class
	 * @param odometer
	 * @param leftMotor
	 * @param rightMotor
	 * @param TRACK
	 * @param WHEEL_RAD
	 * @param LS_W_DISTANCE width distance from the robot centre to the right light sensor
	 * @param LS_L_DISTANCE length distance from the robot centre to the light sensors
	 */
	public LightLocalization(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double TRACK,
			double L_WHEEL_RAD, double R_WHEEL_RAD, double LS_WIDTH, double LS_L_DISTANCE) {
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		LightLocalization.L_WHEEL_RAD = L_WHEEL_RAD;
		LightLocalization.R_WHEEL_RAD = R_WHEEL_RAD;
		LightLocalization.TRACK = TRACK;
		LightLocalization.LS_W_DISTANCE = LS_WIDTH / 2;
		LightLocalization.LS_L_DISTANCE = LS_L_DISTANCE;
	}
	
	/**
	 * Localizes the robot using the right light sensor at the back of the robot. Requires that the robot is close enough to a grid
	 * intersection that when turning in a circle, the right light sensor touches all four grid lines branching from the intersection
	 * @param gridX x grid coordinate
	 * @param gridY y grid coordinate
	 * @param quadrant	integer expressing the quadrant relative to the intersection where the right light sensor starts.
	 * 0 = bottom left, 1 = bottom right, 2 = top right, 3 = top left.
	 */
	public void lightLocalization(int gridX, int gridY, int quadrant) {
		lineCounter = quadrant;
		inLine = false;
		localizing = true;
		
		Navigation.setMotorSpeeds(ROTATE_SPEED, leftMotor, rightMotor, L_WHEEL_RAD, R_WHEEL_RAD);
		
		//rotate counter-clockwise
		leftMotor.rotate(-Navigation.convertAngle(L_WHEEL_RAD, TRACK, 360), true);
		rightMotor.rotate(Navigation.convertAngle(R_WHEEL_RAD, TRACK, 360), false);
		
		//stop the motors
		leftMotor.stop(true);
		rightMotor.stop(false);
		
		localizing = false;
		
		double dAngleX = (angleXp - angleXn + 360) % 360;
		double dAngleY = (angleYn - angleYp + 360) % 360;
		
		double angleOffset = Math.atan(LS_W_DISTANCE / LS_L_DISTANCE);
		double lsDistance = Math.sqrt(LS_W_DISTANCE * LS_W_DISTANCE + LS_L_DISTANCE * LS_L_DISTANCE);
		
		double dx = lsDistance * -Math.cos(Math.PI * dAngleY / 360);
		double dy = lsDistance * -Math.cos(Math.PI * dAngleX / 360);
		
		angleOffset *= 180 / Math.PI;
		
		double dThetaY = 270 - (angleYn - angleOffset) + dAngleY / 2;	//theta correction calculated with y axis
		if(dThetaY > 180)
			dThetaY -= 360;
		double dTheta = dThetaY + CORRECTION_ANGLES[quadrant];
		
		odometer.setXYT(gridX * RingChallenge.TILE_SIZE + dx, gridY * RingChallenge.TILE_SIZE + dy,
				odometer.getXYT()[2] + (dTheta + 360) % 360);
		
	}

	/**
	 * receieves data from the light sensors
	 * @param light light intensities read by the sensor [left, right]
	 */
	@Override
	public void processLSData(int[] light) {
		if(!localizing)
			return;
		
		boolean lineCrossed = lineDetector.lineCrossed(light[1]);
		
		//record angle and increment counter if line crossed
		if(lineCrossed && !inLine) {
			inLine = true;
			Sound.beep();
			//get angle
			switch (lineCounter) {
			case 0:
				angleYn = odometer.getXYT()[2];
				break;
			case 1:
				angleXp = odometer.getXYT()[2];
				break;
			case 2:
				angleYp = odometer.getXYT()[2];
				break;
			case 3:
				angleXn = odometer.getXYT()[2];
				break;
			}
		}
		else if(!lineCrossed && inLine) {
			inLine = false;
			lineCounter++;
			lineCounter %= 4;
		}
	}
}
