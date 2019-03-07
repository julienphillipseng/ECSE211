package ca.mcgill.ecse211.localization;

import ca.mcgill.ecse211.game.RingChallenge;
import ca.mcgill.ecse211.interfaces.UltrasonicUser;
import ca.mcgill.ecse211.odometry.Odometer;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class contrains methods that allow the robot to perform ultrasonic localization
 * using the falling edge method. It implements the UltrasonicUser interface
 * 
 * @author Matthew, Romain
 *
 */
public class UltrasonicLocalization implements UltrasonicUser {
	

	private static final int DISTANCE_THRESHOLD = 35;	//how small distance is before wall is considered as detected
	private static final int NOISE_MARGIN = 5;			//width of the noise margin
	private static final int ROTATE_SPEED = 250;		//motor speed while turning
	
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private Odometer odometer;
	
	private double angleBottom;
	private double angleLeft;
	
	private int localizationStep;
	private boolean localizing = false;
	
	
	/**
	 * Constructor for the UltrasonicLocalization class
	 * @param odometer
	 * @param leftMotor
	 * @param rightMotor
	 */
	public UltrasonicLocalization(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
	}
	
	
	/**
	 * Localizes the robot using the falling edge method and the ultrasonic sensor at the front of the robot. Requires being in a
	 * starting corner grid space roughly in the middle between the x and y grid lines
	 */
	public void fallingEdge() {
		localizationStep = 0;
		localizing = true;
		
		Navigation.setMotorSpeeds(ROTATE_SPEED, leftMotor, rightMotor, RingChallenge.L_WHEEL_RAD, RingChallenge.R_WHEEL_RAD);
		
		//rotate clockwise
		leftMotor.forward();
		rightMotor.backward();
		
		while(localizing);
		
		//calculate correction to theta
		double dAngle;
		if(angleBottom < angleLeft)
			dAngle = 225 - (angleBottom + angleLeft) / 2;
		else
			dAngle = 45 - (angleBottom + angleLeft) / 2;
		double theta = odometer.getXYT()[2] + dAngle;
		
		//apply correction
		odometer.setTheta((theta + 360) % 360);
	}


	@Override
	/**
	 * Receives the distance readings from the sensor controller
	 */
	public void processUSDistance(int distance) {
		if(!localizing)
			return;
		else if(distance > DISTANCE_THRESHOLD + NOISE_MARGIN) {
			switch(localizationStep) {
			case 0:
				localizationStep++;
				break;
			case 2:
				localizationStep++;
				break;
			}
		}
		else if(distance < DISTANCE_THRESHOLD - NOISE_MARGIN) {
			switch(localizationStep) {
			case 1:
				angleBottom = odometer.getXYT()[2];
				//rotate counter-clockwise
				leftMotor.backward();
				rightMotor.forward();
				localizationStep++;
				break;
			case 3:
				angleLeft = odometer.getXYT()[2];
				if((angleBottom + 360 - angleLeft) % 360 < 90)
					break;
				//stop robot
				leftMotor.stop(true);
				rightMotor.stop(false);
				localizationStep++;
				localizing = false;
				break;
			}
		}
	}
}
