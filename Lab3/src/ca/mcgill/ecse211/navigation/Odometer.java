/**
 * This class is meant as a skeleton for the odometer class to be used.
 * 
 * @author Rodrigo Silva
 * @author Dirk Dubois
 * @author Derek Yu
 * @author Karim El-Baba
 * @author Michael Smith
 */

package ca.mcgill.ecse211.navigation;

import ca.mcgill.ecse211.lab3.Lab3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends OdometerData implements Runnable {

  private OdometerData odoData;
  private static Odometer odo = null; // Returned as singleton

  // Motors and related variables
  private int leftMotorTachoCount;
  private int rightMotorTachoCount;
  private EV3LargeRegulatedMotor leftMotor;
  private EV3LargeRegulatedMotor rightMotor;
  
  public static int lastTachoL;
  public static int lastTachoR;

  private static final double PI = 3.14159;
  private static final double RAD_TO_DEG = 57.2958;

  private double[] position;


  private static final long ODOMETER_PERIOD = 25; // odometer update period in ms

  /**
   * This is the default constructor of this class. It initiates all motors and variables once.It
   * cannot be accessed externally.
   * 
   * @param leftMotor
   * @param rightMotor
   * @throws OdometerExceptions
   */
  private Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
      final double TRACK, final double WHEEL_RAD) throws OdometerExceptions {
    odoData = OdometerData.getOdometerData(); // Allows access to x,y,z
                                              // manipulation methods
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;

    // Reset the values of x, y and z to 0
    odoData.setXYT(0, 0, 0);

    this.leftMotorTachoCount = 0;
    this.rightMotorTachoCount = 0;

  }

  /**
   * This method is meant to ensure only one instance of the odometer is used throughout the code.
   * 
   * @param leftMotor
   * @param rightMotor
   * @return new or existing Odometer Object
   * @throws OdometerExceptions
   */
  public synchronized static Odometer getOdometer(EV3LargeRegulatedMotor leftMotor,
      EV3LargeRegulatedMotor rightMotor, final double TRACK, final double WHEEL_RAD)
      throws OdometerExceptions 
  {
    if (odo != null) 
    { // Return existing object
      return odo;
    } else { // create object and return it
      odo = new Odometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
      return odo;
    }
  }

  /**
   * This class is meant to return the existing Odometer Object. It is meant to be used only if an
   * odometer object has been created
   * 
   * @return error if no previous odometer exists
   */
  public synchronized static Odometer getOdometer() throws OdometerExceptions 
  {

    if (odo == null) 
    {
      throw new OdometerExceptions("No previous Odometer exits.");

    }
    return odo;
  }

  /**
   * This method is where the logic for the odometer will run. Use the methods provided from the
   * OdometerData class to implement the odometer.
   */
  // run method (required for Thread)
  public void run() 
  {
    long updateStart, updateEnd;

    while (true) 
    {
      updateStart = System.currentTimeMillis();
      
      leftMotorTachoCount = leftMotor.getTachoCount();
      rightMotorTachoCount = rightMotor.getTachoCount();
      position = odo.getXYT();

      // TODO Calculate new robot position based on tachometer counts
      double distL, distR, deltaD, deltaT, dX, dY, theta = position[2];
      
      distL = PI*Lab3.WHEEL_RAD*(leftMotorTachoCount-lastTachoL)/180; //distance traveled by left wheel
      distR = PI*Lab3.WHEEL_RAD*(rightMotorTachoCount-lastTachoR)/180; //distance traveled by right wheel
      lastTachoL = leftMotorTachoCount;
      lastTachoR = rightMotorTachoCount;
      deltaD = 0.5*(distL+distR); //change in distance traveled
      deltaT = (distL-distR)/Lab3.TRACK * RAD_TO_DEG; //change in angle (in degrees)
      theta += deltaT;
      dX = deltaD * Math.sin(theta/RAD_TO_DEG); //converting back to radians for calculation
      dY = deltaD * Math.cos(theta/RAD_TO_DEG);
      
      // TODO Update odometer values with new calculated values
      odo.update(dX, dY, deltaT);

      // this ensures that the odometer only runs once every period
      updateEnd = System.currentTimeMillis();
      if (updateEnd - updateStart < ODOMETER_PERIOD) 
      {
        try 
        {
          Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
        } 
        catch (InterruptedException e) 
        {
          // there is nothing to be done
        }
      }
    }
  }

}