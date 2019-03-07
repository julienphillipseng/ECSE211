package ca.mcgill.ecse211.wallfollowing;

import lejos.hardware.Button;
import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController {

  private final int bandCenter;
  private final int bandwidth;
  private final int motorLow;
  private final int motorHigh;
  private int distance;

  public BangBangController(int bandCenter, int bandwidth, int motorLow, int motorHigh) 
  {
    // Default Constructor
    this.bandCenter = bandCenter;
    this.bandwidth = bandwidth;
    this.motorLow = motorLow;
    this.motorHigh = motorHigh;
    WallFollowingLab.leftMotor.setSpeed(motorHigh); // Start robot moving forward
    WallFollowingLab.rightMotor.setSpeed(motorHigh);
    WallFollowingLab.leftMotor.forward();
    WallFollowingLab.rightMotor.forward();
  }

  @Override
  public void processUSData(int distance) 
  {
    this.distance = distance;
    // TODO: process a movement based on the us distance passed in (BANG-BANG style)
    
    int distError = bandCenter-distance;
    if(Math.abs(distError) <= bandwidth) { //bot is within limits
    	WallFollowingLab.leftMotor.setSpeed(motorHigh);
    	WallFollowingLab.rightMotor.setSpeed(motorHigh);
    	WallFollowingLab.leftMotor.forward();
    	WallFollowingLab.rightMotor.forward();
    }
    else if (distError > 0) { //when the robot is too close
    	if (distError >= 12 ) { // very close to wall and must reverse
    		WallFollowingLab.leftMotor.setSpeed(motorLow + 50);
        	WallFollowingLab.rightMotor.setSpeed(motorHigh + 100 );
        	WallFollowingLab.leftMotor.backward();
        	WallFollowingLab.rightMotor.backward();
    	}
    	else { // close but not too close = right turn
    		WallFollowingLab.leftMotor.setSpeed(motorHigh);
        	WallFollowingLab.rightMotor.setSpeed(motorLow);
        	WallFollowingLab.leftMotor.forward();
        	WallFollowingLab.rightMotor.forward();
    	}
    	
    }
    else if (distError < 0) { //too far = left turn 
    	WallFollowingLab.leftMotor.setSpeed(motorLow + 100);
    	WallFollowingLab.rightMotor.setSpeed(motorHigh);
    	WallFollowingLab.leftMotor.forward();
    	WallFollowingLab.rightMotor.forward();
    }
  }

  @Override
  public int readUSDistance() 
  {
    return this.distance;
  }
}
