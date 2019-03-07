/**
 * Lab3.java
 * Contains main method, deals with the display and starting all the appropriate threads
 * Derived from the sample code given for Lab 2
 * 
 * @author Lara Kollokian, 260806317
 * @author Julien Phillips, 260804197
 * @author Whoever wrote the sample code for lab 2
 */
package ca.mcgill.ecse211.lab3;

import ca.mcgill.ecse211.navigation.*;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Lab3 
{
	// Parameters: adjust these for desired performance
	public static final int bandCenter = 35; // Offset from the wall (cm)
	public static int distance = 0;
	public static boolean avoidBlocks = false;

	// Motor Objects, and Robot related parameters
	private static final Port usPort = LocalEV3.get().getPort("S1");
	public static final EV3LargeRegulatedMotor leftMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	public static final double WHEEL_RAD = 2.15; 
	public static final double TRACK = 11.5; 

	public static void main(String[] args) throws OdometerExceptions 
	{
		int buttonChoice;

		// Odometer related objects
		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD); // TODO Complete implementation
		Display odometryDisplay = new Display(lcd); // No need to change
		
		//setting up ultrasonic poller
		@SuppressWarnings("resource")
		SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
		SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from this instance
		float[] usData = new float[usDistance.sampleSize()]; // usData is the buffer in which data are returned    

		// clear the display
		lcd.clear();

		// ask the user whether they want to navigate through the waypoints
		// without obstacles or with obstacles
		lcd.drawString("< Left | Right >", 0, 0);
		lcd.drawString("       |        ", 0, 1);
		lcd.drawString(" Drive | Avoid  ", 0, 2);
		lcd.drawString("  to   | obs-   ", 0, 3);
		lcd.drawString("points | tacles ", 0, 4);

		buttonChoice = Button.waitForAnyPress(); // Record choice (left or right press)

		// Start odometer and display threads
		Thread odoThread = new Thread(odometer);
		odoThread.start();
		Thread odoDisplayThread = new Thread(odometryDisplay);
		odoDisplayThread.start();

		// spawn a new Thread to avoid SquareDriver.drive() from blocking
		(new Thread() {
			public void run() {
				Controller.drive(leftMotor, rightMotor, WHEEL_RAD, WHEEL_RAD, TRACK);
			}
		}).start();

		// Start correction if right button was pressed
		if (buttonChoice == Button.ID_RIGHT) 
		{
//			AvoidBlock avoid = new AvoidBlock(bandCenter, bandWidth, motorLow, motorHigh);
			avoidBlocks = true;
			UltrasonicPoller usPoller = null; 
			usPoller = new UltrasonicPoller(usDistance, usData);
			usPoller.start();
		}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}