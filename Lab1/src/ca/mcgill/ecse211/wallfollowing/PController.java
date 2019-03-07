package ca.mcgill.ecse211.wallfollowing;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {

  /* Constants */
  private static final int MOTOR_SPEED = 150;
  private static final int FILTER_OUT = 20;

  private final int bandCenter;
  private final int bandWidth;
  private int distance;
  private int filterControl;

  public PController(int bandCenter, int bandwidth) {
    this.bandCenter = bandCenter;
    this.bandWidth = bandwidth;
    this.filterControl = 0;

    WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED); // Initalize motor rolling forward
    WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED);
    WallFollowingLab.leftMotor.forward();
    WallFollowingLab.rightMotor.forward();
  }
  
  /**
   * 
   * @param diff
   * @return
   */
//  public int calcGain (int diff)
//  {
//	  int correction = 0; //correction changes proportionally to error
//	  
//	  return correction;
//  }
  
  public int speedCorrect (int distance)
  {
	  
	  int error = Math.abs(distance-bandCenter) ;
	  int newSpeed = error * 8;
	  if (newSpeed > 100) {
		  newSpeed = 100; //correction changes proportionally to error
	  }
//	  int newSpeed = error *3/100;
//	  System.out.println(newSpeed);
	  return newSpeed;
  }

  @Override
  public void processUSData(int distance) {

    // rudimentary filter - toss out invalid samples corresponding to null
    // signal.
    // (n.b. this was not included in the Bang-bang controller, but easily
    // could have).
    //
    if (distance >= 255 && filterControl < FILTER_OUT) {
      // bad value, do not set the distance var, however do increment the
      // filter value
      filterControl++;
    } else if (distance >= 255) {
      // We have repeated large values, so there must actually be nothing
      // there: leave the distance alone
      this.distance = distance;
    } else {
      // distance went below 255: reset filter and leave
      // distance alone.
      filterControl = 0;
      this.distance = distance;
      
    }

    // TODO: process a movement based on the us distance passed in (P style)
    if (distance > 150) {
    	distance = 150;
    }
    int distError = bandCenter-distance;
    int diff = 0;
    if(Math.abs(distError) <= bandWidth) //bot is within limits
    { 
    	WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED);
    	WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED);
    	WallFollowingLab.leftMotor.forward();
    	WallFollowingLab.rightMotor.forward();
    }
    else if (distError > 0) { //robot is too close to the wall must turn right
    	diff = speedCorrect(distance);
    	if ( distError >= 17) { //way too close to the wall, must REVERSE to make the SHARP RIGHT TURN ahead
    		WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED-diff/2);
        	WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED+diff);
        	WallFollowingLab.leftMotor.backward(); // you could try putting this as forwards.if you do, i would make distError >= 17(ish).
        	WallFollowingLab.rightMotor.backward();
    	} 
    	else { // slower right turn 
    		WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED+diff);
        	WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED); // not sure if you need the -diff here? i would try both with it and without it.
        	WallFollowingLab.leftMotor.forward();
        	WallFollowingLab.rightMotor.forward();
    	}
    	diff = speedCorrect(distance);
    	
    }
    else if (distError < 0) //far
    {
    	diff = speedCorrect(distance);
    	WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED); // not sure if you really need to subtract diff here either. try with and without. really depends on 
    	WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED+ 3*diff/4); //       the value of diff. keeping it might make the robot turn too fast if diff is too large.
    	WallFollowingLab.leftMotor.forward();
    	WallFollowingLab.rightMotor.forward();
    }
  }


  @Override
  public int readUSDistance() {
    return this.distance;
  }

}
