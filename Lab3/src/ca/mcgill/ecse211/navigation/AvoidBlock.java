package ca.mcgill.ecse211.navigation;

import ca.mcgill.ecse211.lab3.Lab3;

public class AvoidBlock //implements UltrasonicController
{

  private static int bandCenter;
  private static int motorLow;
  private static int motorHigh;
  private static int adjust = 100;

  public AvoidBlock(int bandCenter, int motorLow, int motorHigh) 
  {
    // Default Constructor
    AvoidBlock.bandCenter = bandCenter;
    AvoidBlock.motorLow = motorLow;
    AvoidBlock.motorHigh = motorHigh;
    Lab3.leftMotor.setSpeed(motorHigh); // Start robot moving forward
    Lab3.rightMotor.setSpeed(motorHigh);
    Lab3.leftMotor.forward();
    Lab3.rightMotor.forward();
  }
  
	public static void processUSData(int distance) 
	{
		// TODO: process a movement based on the us distance passed in (BANG-BANG style)

		int distError = bandCenter-distance;
		if (distance < bandCenter) //when the robot is too close
		{ 
			if (distError >= 12 ) { // very close to wall and must reverse
				Lab3.leftMotor.setSpeed(motorLow + adjust/2);
				Lab3.rightMotor.setSpeed(motorHigh + adjust );
				Lab3.leftMotor.backward();
				Lab3.rightMotor.backward();
			}
			else { // close but not too close = right turn
				Lab3.leftMotor.setSpeed(motorHigh);
				Lab3.rightMotor.setSpeed(motorLow);
				Lab3.leftMotor.forward();
				Lab3.rightMotor.forward();
			}
		}
		else
		{
			
		}
	}

}
