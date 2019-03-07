package ca.mcgill.ecse211.odometry;

import ca.mcgill.ecse211.game.RingChallenge;
import ca.mcgill.ecse211.interfaces.LightSensorUser;
import ca.mcgill.ecse211.localization.Navigation;
import ca.mcgill.ecse211.sensor.LineDetector;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class controls odometry correction. It uses an existing instance of the odometer since it is
 * a singleton pattern. The run method performs odometry correction using both light sensors at
 * the back of the robot
 * 
 * @author Matthew, Lara
 *
 */
public class OdometryCorrection implements LightSensorUser {
	private static final int THRESHOLD = 30;
	private static final int WINDOW_SIZE = 5;
	
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private Odometer odometer;
	
	private LineDetector leftLineDetector = new LineDetector(WINDOW_SIZE, THRESHOLD);
	private LineDetector rightLineDetector = new LineDetector(WINDOW_SIZE, THRESHOLD);
	
	//first element of an array indicates the left light sensor, while the second element indicates the right
	private boolean[] inLine = new boolean[2];
	private boolean[] lineCrossed = new boolean[2];
	private int[][] tachoCount = new int[2][2];
	boolean running;

	/**
	 * This is the default class constructor. An existing instance of the odometer
	 * is used. This is to ensure thread safety.
	 * 
	 * @throws OdometerExceptions
	 */
	public OdometryCorrection(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) throws OdometerExceptions {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odometer = Odometer.getOdometer();
	}

	/**
	 * Here is where the odometry correction code is run. It updates the odometer each time the light sensors cross a line.
	 * @param light light sensor values read by the light sensor
	 */
	public void processLSData(int[] light) {
		//determine if sensors have crossed the line
		boolean[] lineDetected = {leftLineDetector.lineCrossed(light[0]), rightLineDetector.lineCrossed(light[1])};
		
		//check if correction enabled
		if(!running)
			return;
		
		//check if left light sensor has entered a line
		if(lineDetected[0] && !inLine[0]) {
			inLine[0] = true;
			tachoCount[0][0] = leftMotor.getTachoCount();
			tachoCount[0][1] = rightMotor.getTachoCount();
			if(lineCrossed[1]) {
				correctOdometer(1);
				lineCrossed[1] = false;
			}
			else
				lineCrossed[0] = true;
		}
		//check if left light sensor has exited a line
		else if(!lineDetected[0] && inLine[0]) {
			inLine[0] = false;
		}
		
		//check if right light sensor has entered a line
		if(lineDetected[1] && !inLine[1]) {
			inLine[1] = true;
			tachoCount[1][0] = leftMotor.getTachoCount();
			tachoCount[1][1] = rightMotor.getTachoCount();
			if(lineCrossed[0]) {
				correctOdometer(0);
				lineCrossed[0] = false;
			}
			else
				lineCrossed[1] = true;
		}
		//check if right light sensor has exited a line
		else if(!lineDetected[1] && inLine[1]) {
			inLine[1] = false;
		}
	}

	/**
	 * This method determines what the corrected values for the odometer are and applies them
	 * @param leadingSensor which sensor detected a line first (0 = left, 1 = right)
	 */
	private void correctOdometer(int leadingSensor) {
		int laggingSensor = leadingSensor == 0 ? 1 : 0;
		double dL = Math.PI * RingChallenge.L_WHEEL_RAD * (tachoCount[laggingSensor][0] - tachoCount[leadingSensor][0]) / 180;
		double dR = Math.PI * RingChallenge.R_WHEEL_RAD * (tachoCount[laggingSensor][1] - tachoCount[leadingSensor][1]) / 180;
		double distance = (dL + dR) / 2;
		double dTheta = Math.atan(distance / RingChallenge.LS_WIDTH);
		if(leadingSensor == 1)
			dTheta *= -1;
		double perpDistance = (RingChallenge.LS_L_DISTANCE + distance / 2) * Math.cos(dTheta);	//perpendicular distance
		dTheta *= 180 / Math.PI;
		
		if(dTheta > 30)
			return;
		
		double[] XYT = odometer.getXYT();
		int[] coords;
		
		//correct odometer based on which way the robot is facing and where the closest grid intersection is
		if(315 <= XYT[2] || XYT[2] < 45) {	//up
			coords = Navigation.getClosestCoordinates(XYT[0], XYT[1] - perpDistance);
			odometer.setY(coords[1] * RingChallenge.TILE_SIZE + perpDistance);
			odometer.setTheta((dTheta + 360) % 360);
		}
		else if(45 <= XYT[2] && XYT[2] < 135) {	//right
			coords = Navigation.getClosestCoordinates(XYT[0] - perpDistance, XYT[1]);
			odometer.setX(coords[0] * RingChallenge.TILE_SIZE + perpDistance);
			odometer.setTheta((90 + dTheta) % 360);
		}
		else if(135 <= XYT[2] && XYT[2] < 225) {	//down
			coords = Navigation.getClosestCoordinates(XYT[0], XYT[1] + perpDistance);
			odometer.setY(coords[1] * RingChallenge.TILE_SIZE - perpDistance);
			odometer.setTheta((180 + dTheta) % 360);
		}
		else {	//left
			coords = Navigation.getClosestCoordinates(XYT[0] + perpDistance, XYT[1]);
			odometer.setX(coords[0] * RingChallenge.TILE_SIZE - perpDistance);
			odometer.setTheta((270 + dTheta) % 360);
		}
		Sound.beep();
	}
}
