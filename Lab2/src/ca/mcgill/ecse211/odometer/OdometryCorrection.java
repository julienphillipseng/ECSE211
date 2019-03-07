/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometer;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class OdometryCorrection implements Runnable 
{
  private static final long CORRECTION_PERIOD = 10;
  private Odometer odometer;
  
  //setting up color sensor
  private static Port portColor = LocalEV3.get().getPort("S1");
  private static SensorModes myColor = new EV3ColorSensor(portColor);
  private static SampleProvider myColorSample = myColor.getMode("Red");
  private static float[] sampleColor = new float[myColor.sampleSize()];
  private static final double TILE_SIZE = 30.48;
  
  private double[] position;

  /**
   * This is the default class constructor. An existing instance of the odometer is used. This is to
   * ensure thread safety.
   * 
   * @throws OdometerExceptions
   */
  public OdometryCorrection() throws OdometerExceptions 
  {

    this.odometer = Odometer.getOdometer();

  }

  /**
   * Here is where the odometer correction code should be run.
   * 
   * @throws OdometerExceptions
   */
  // run method (required for Thread)
  public void run() 
  {
    long correctionStart, correctionEnd;
    int xCount = -1, yCount = -1;
    double newX, newY;

    while (true) 
    {
      correctionStart = System.currentTimeMillis();

      // TODO Trigger correction (When do I have information to correct?)
      // TODO Calculate new (accurate) robot position
      // TODO Update odometer with new calculated (and more accurate) values
      
      myColorSample.fetchSample(sampleColor, 0);
      
      //current position
      position = odometer.getXYT();
      double theta = position[2];
      
      //to plot the data points
//      numSamples++;
//      System.out.println(numSamples + "," + sampleColor[0]*1000);
      
      //when the sensor detects a line
      if((sampleColor[0] * 1000) < 175 && (sampleColor[0] * 1000) > 100) //change to differential calculation instead of abs value
      {
    	  Sound.beep();
    	  if(theta > -10 && theta < 10) //angle is 0, bot is going up
    	  {
    		  yCount++;
    		  newY = yCount * TILE_SIZE;
    		  odometer.setY(newY);
    	  }
    	  else if(theta > 80 && theta < 100) //angle is 90, bot is going right
    	  {
    		  xCount++;
    		  newX = xCount * TILE_SIZE;
    		  odometer.setX(newX);
    	  }
    	  else if(theta > 170 && theta < 190) //angle is 180. bot is going down
    	  {
    		  newY = yCount * TILE_SIZE;
    		  odometer.setY(newY);
    		  yCount--;
    	  }
    	  else if(theta > 260 && theta < 280) //angle is 270, bot is going left
    	  {
    		  newX = xCount * TILE_SIZE;
    		  odometer.setX(newX);
    		  xCount--;
    	  } 
      }

      // this ensure the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) 
      {
        try 
        {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } 
        catch (InterruptedException e) 
        {
          // there is nothing to be done here
        }
      }
    }
  }
}
