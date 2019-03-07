/**
 * This class contains the main method for the Lab 5 software.
 * @author Romain Couperier
 * @author Lara Kollokian
 */
package ca.mcgill.ecse211.project;

import ca.mcgill.ecse211.lightSensor.LightPoller;
import ca.mcgill.ecse211.navigation.Navigation;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.ultrasonicSensor.UltrasonicPoller;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Lab5 {

	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Port usPort = LocalEV3.get().getPort("S1");
	private static final Port linePort = LocalEV3.get().getPort("S4");
	private static final Port colorPort = LocalEV3.get().getPort("S3");
	static final TextLCD lcd = LocalEV3.get().getTextLCD();

	public static final int ROTATE_SPEED = 100;
	public static final double WHEEL_RAD = 2.2;
	public static final double TRACK = 15.8;
	static final double TILE_SIZE = 30.48;

	// parameters to be inputted before every run
	// search region can go from 2X2 squares to 6X6 squares
	// at least 2 rings and at most 5 rings in the search region
	// only 1 ring will have the target color in the search region
	// 2 rings are separated by a distance of at least 1 ring width (i.e. 10 cm)
	// rings will be placed only at grid intersections
	public static final int LL_X = 2; // Lower left corner of search region (x), range [0,8]
	public static final int LL_Y = 2; // Lower left corner of search region (y), range [0,8]
	public static final int UR_X = 6; // Upper right corner of search region (x), range [0,8]
	public static final int UR_Y = 6; // Upper right corner of search region (y), range [0,8]
	public static final int TR = 2; // Color of target ring, range [1,4]
	public static final int SC = 0; // Starting corner, range [0,3]

	/**
	 * This method is the starting point for the entirety of the Lab 5 software.
	 * @param args
	 * @throws OdometerExceptions
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws OdometerExceptions, InterruptedException {
		int buttonChoice;

		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
		Navigation navigation = new Navigation(leftMotor, rightMotor, WHEEL_RAD, TRACK, TILE_SIZE, odometer);
		UltrasonicLocalizer usLocalizer = new UltrasonicLocalizer(leftMotor, rightMotor, odometer);
		LightLocalizer lsLocalizer = new LightLocalizer(leftMotor, rightMotor, odometer, WHEEL_RAD, TRACK);
		Detection detection = new Detection(leftMotor, rightMotor, WHEEL_RAD, TRACK, TILE_SIZE, odometer, navigation);
		
		// set up ultrasonic poller
		@SuppressWarnings("resource")
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usDistance = usSensor.getMode("Distance");
		float[] usData = new float[usDistance.sampleSize()];
		UltrasonicPoller usPoller = new UltrasonicPoller(usDistance, usData, odometer);

		// set up light sensor poller
		@SuppressWarnings("resource")
		SensorModes lsSensor = new EV3ColorSensor(linePort);
		SampleProvider lsDistance = lsSensor.getMode("Red");
		float[] lsData = new float[lsDistance.sampleSize()];

		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorDistance = colorSensor.getMode("RGB");
		float[] colorData = new float[colorDistance.sampleSize()];

		LightPoller lsPoller = new LightPoller(lsDistance, lsData, colorDistance, colorData, odometer, lsLocalizer);
		// create threads
		Thread odoThread = new Thread(odometer);
		Thread usThread = new Thread(usPoller);
		Thread lsThread = new Thread(lsPoller);

		do {
			lcd.clear();
			lcd.drawString("< Left  | Right > ", 0, 0);
			lcd.drawString(" Detect | Start   ", 0, 1);
			lcd.drawString(" Color  | Search  ", 0, 2);
			lcd.drawString("        |         ", 0, 3);
			lcd.drawString("----------------- ", 0, 4);
			lcd.drawString("       Down       ", 0, 5);
			lcd.drawString("  Track Testing   ", 0, 6);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT && buttonChoice != Button.ID_DOWN);

		if (buttonChoice == Button.ID_DOWN) {
			// In order to check if the track is working this section makes the robot turn
			// 360 degrees
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);

			leftMotor.rotate(Navigation.convertAngle(WHEEL_RAD, TRACK, 360), true);
			rightMotor.rotate(-Navigation.convertAngle(WHEEL_RAD, TRACK, 360), false);
		} else {
			if (buttonChoice == Button.ID_LEFT) {

				lcd.clear();
				lsThread.start();
				
				while (Button.waitForAnyPress() != Button.ID_ESCAPE) {
					detection.detect();
					Button.waitForAnyPress();
				}
			} else {

				lcd.clear();
				
				Localizer localizer = new Localizer(usLocalizer, lsLocalizer, usThread, lsThread, navigation, 1, lcd, detection);
				odoThread.start();
				localizer.start();

				while (Button.waitForAnyPress() != Button.ID_ESCAPE)
					;
				System.exit(0);
			}
			while (Button.waitForAnyPress() != Button.ID_ESCAPE)
				;
			System.exit(0);
		}
	}
}