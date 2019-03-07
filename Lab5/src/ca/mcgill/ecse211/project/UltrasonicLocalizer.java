/**
 * This class contains the methods for ultrasonic localization of the robot.
 * @author Matthew Kourlas
 */
package ca.mcgill.ecse211.project;

import ca.mcgill.ecse211.odometer.Odometer;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class UltrasonicLocalizer {
	
	private static final int DISTANCE_THRESHOLD = 35;	//how small distance is before wall is considered as detected
	private static final int NOISE_MARGIN = 10;			//width of the noise margin
	private static final int ROTATE_SPEED = 130;		//motor speed while turning
	
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	private static Odometer odometer;
	
	private boolean facingWall;		//indicates whether the robot is currently facing a wall
	
	
	public UltrasonicLocalizer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
	  Odometer odometer) {
		UltrasonicLocalizer.leftMotor = leftMotor;
		UltrasonicLocalizer.rightMotor = rightMotor;
		UltrasonicLocalizer.odometer = odometer;
	}
	
	/**
	 * localizing routine that uses falling edges to detect the threshold distance angles. Method does not return until the robot is
	 * done localizing.
	 */
	public void fallingEdge() {
		updateFacingWall();
		
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		//rotate clockwise
		leftMotor.forward();
		rightMotor.backward();
		
		//if facing wall, wait until not facing wall
		if(facingWall) {
			while(odometer.getDLRGB()[0] <= DISTANCE_THRESHOLD + NOISE_MARGIN) {
				//wait until robot detects rising edge (pointing away from wall)
			}
			facingWall = false;
		}
		
		System.out.println("Facing away from wall. Now ready to start localizing.");
		
		while(odometer.getDLRGB()[0] >= DISTANCE_THRESHOLD - NOISE_MARGIN) {
			//wait until robot detects falling edge (pointing towards wall)
		}
		//robot pointing just within back wall, set to angle A
		double angleA = odometer.getXYT()[2];
		facingWall = true;
		
		System.out.println("Back wall bound detected.");
		
		//rotate counter-clockwise
		leftMotor.backward();
		rightMotor.forward();
		
		while(odometer.getDLRGB()[0] <= DISTANCE_THRESHOLD + NOISE_MARGIN) {
			//wait until robot detects rising edge (pointing away from wall)
		}
		System.out.println("now facing away from wall");
		facingWall =  false;
		while(odometer.getDLRGB()[0] >= DISTANCE_THRESHOLD - NOISE_MARGIN) {
			//wait until robot detects falling edge (pointing towards wall)
		}
		//robot pointing just past left wall, set to angle B
		double angleB = odometer.getXYT()[2];
		facingWall = true;
		
		System.out.println("Left wall bound detected.");
		
		double dAngle;
		if(angleA < angleB)
			dAngle = 225 - (angleA + angleB) / 2;
		else
			dAngle = 45 - (angleA + angleB) / 2;
		double theta = odometer.getXYT()[2] + dAngle;
		odometer.setXYT(-10, -10, theta);
	}
	
	/**
	* This method. updates the facingWall field.
	* If robot is pointing towards the noise band, the robot turns clockwise until past the noise margin.
	*/
	private void updateFacingWall() {
		if(odometer.getDLRGB()[0] < DISTANCE_THRESHOLD - NOISE_MARGIN)
			facingWall = true;
		else if(odometer.getDLRGB()[0] > DISTANCE_THRESHOLD + NOISE_MARGIN)
			facingWall = false;
		else {
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);
			//turn clockwise
			leftMotor.forward();
			rightMotor.backward();
			while(DISTANCE_THRESHOLD - NOISE_MARGIN <= odometer.getDLRGB()[0] && odometer.getDLRGB()[0] <= DISTANCE_THRESHOLD + NOISE_MARGIN) {
				//wait until robot is pointing away from noise band
			}
			leftMotor.stop();
			rightMotor.stop();
			updateFacingWall();
		}	
	}

}
