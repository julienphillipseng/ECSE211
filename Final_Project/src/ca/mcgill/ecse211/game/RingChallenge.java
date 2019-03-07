package ca.mcgill.ecse211.game;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import ca.mcgill.ecse211.localization.LightLocalization;
import ca.mcgill.ecse211.localization.Navigation;
import ca.mcgill.ecse211.localization.UltrasonicLocalization;
import ca.mcgill.ecse211.odometry.Odometer;
import ca.mcgill.ecse211.odometry.OdometerExceptions;
import ca.mcgill.ecse211.odometry.OdometryCorrection;
import ca.mcgill.ecse211.sensor.LightPoller;
import ca.mcgill.ecse211.sensor.SensorController;
import ca.mcgill.ecse211.sensor.SensorExceptions;
import ca.mcgill.ecse211.sensor.UltrasonicPoller;

/**
 * The RingChallenge class contains the main method of the program. It initializes all the
 * motors, ports, sensors, and pollers. It also starts all the threads. It contains useful constant 
 * values like motor speed, wheel radius, track, tile size, etc.
 *  
 * @author Romain, Matthew
 * @version 2018/10/31
 */
public class RingChallenge {

	// Declare the different robots motors
	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	public static final EV3LargeRegulatedMotor armMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	public static final EV3LargeRegulatedMotor colorMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));

	// Declare the different sensors around the robot
	private static final Port usPort = LocalEV3.get().getPort("S3");
	private static final Port leftLinePort = LocalEV3.get().getPort("S1");
	private static final Port rightLinePort = LocalEV3.get().getPort("S2");
	private static final Port colorPort = LocalEV3.get().getPort("S4");

	// Declare the LCD Display on the EV3 Brick
	public static final TextLCD lcd = LocalEV3.get().getTextLCD();

	// Declare different design and environment variables
	public static final int MOTOR_SPEED = 200;
	public static final double L_WHEEL_RAD = 2.1;
	public static final double R_WHEEL_RAD = 2.1;
	public static final double TRACK = 14.6;// was 14.74 before
	public static final double TILE_SIZE = 30.48;
	public static final double LS_WIDTH = 11.9;
	public static final double LS_L_DISTANCE = 12.8;
	public static final int GAME_GRID_X = 15;
	public static final int GAME_GRID_Y = 9;
	public static final int GAME_TIME = 300;	//seconds
	
	/**
	 * Main method for the robot
	 * @param args
	 * @throws InterruptedException
	 * @throws OdometerExceptions
	 * @throws SensorExceptions
	 */
	public static void main(String[] args) throws InterruptedException, OdometerExceptions, SensorExceptions {

		// Create an Odometer Object and SensorController null pointer
		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, L_WHEEL_RAD, R_WHEEL_RAD);
		
		//Get Odometer's Odometry Correction object
		OdometryCorrection odoCorrection = odometer.getOdometryCorrection();
		
		// Create an instance of all sensors and their corresponding SampleProviders,
		@SuppressWarnings("resource")
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usDistance = usSensor.getMode("Distance");
		float[] usData = new float[usDistance.sampleSize()];

		@SuppressWarnings("resource")
		EV3ColorSensor leftLineSensor = new EV3ColorSensor(leftLinePort);
		SampleProvider leftLineSP = leftLineSensor.getRedMode();
		float[] leftLineData = new float[leftLineSP.sampleSize()];

		@SuppressWarnings("resource")
		EV3ColorSensor rightLineSensor = new EV3ColorSensor(rightLinePort);
		SampleProvider rightLineSP = rightLineSensor.getRedMode();
		float[] rightLineData = new float[rightLineSP.sampleSize()];

		@SuppressWarnings("resource")
		EV3ColorSensor colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorSP = colorSensor.getRGBMode();
		float[] colorData = new float[colorSP.sampleSize()];

		//create pollers
		UltrasonicPoller usPoller = new UltrasonicPoller(usDistance, usData);
		LightPoller lightPoller = new LightPoller(leftLineSP, leftLineData, rightLineSP, rightLineData);
		LightPoller colorPoller = new LightPoller(colorSP, colorData);
		
		//create Sensor Controller
		SensorController sensorController = SensorController.getSensorController(lightPoller, colorPoller, usPoller);
		
		//create Localization objects
		LightLocalization lsLocalization = new LightLocalization(odometer, leftMotor, rightMotor, TRACK, L_WHEEL_RAD, R_WHEEL_RAD, LS_WIDTH,
				LS_L_DISTANCE);
		UltrasonicLocalization usLocalization = new UltrasonicLocalization(odometer, leftMotor, rightMotor);
		
		//Navigation class is completely static, so no object construction necessary
		
		//create RingSearcher object
		RingSearcher ringSearcher = new RingSearcher(leftMotor, rightMotor, armMotor, colorMotor);
		
		//set navigation odometer
		Navigation.setOdometer(odometer);
		
		//create GameNavigation object
		GameNavigation gameNav = new GameNavigation(odometer);
		
		//create Game Controller
		GameController game = new GameController(sensorController, lsLocalization, usLocalization, ringSearcher,
				odoCorrection, gameNav, odometer);
		
		Thread odoThread = new Thread(odometer);
		Thread usThread = new Thread(usPoller);
		Thread lsThread = new Thread(lightPoller);
		Thread colorLsThread = new Thread(colorPoller);
		Thread gameThread = new Thread(game);
		
		Sound.setVolume(Sound.VOL_MAX);
		
		//get parameters from server using Wifi class
		WiFi.wifi();
		
		// Start the timer
		TimeKeeper.startTimer();
		
		odoThread.start();
		usThread.start();
		lsThread.start();
		colorLsThread.start();
		gameThread.start();
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
