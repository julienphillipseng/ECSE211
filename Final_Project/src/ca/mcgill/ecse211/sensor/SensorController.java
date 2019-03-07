package ca.mcgill.ecse211.sensor;

import java.util.ArrayList;

import ca.mcgill.ecse211.interfaces.ColorSensorUser;
import ca.mcgill.ecse211.interfaces.LightSensorUser;
import ca.mcgill.ecse211.interfaces.UltrasonicUser;

/**
 * This class contains methods that deal with control and output of ultrasonic, color and light sensors. It also
 * ensures that there is only one instance of a sensorController throughout the code.
 * 
 * @author Romain
 *
 */
public class SensorController {
	
	private static SensorController sensorController = null;
	
	private LightPoller lightPoller;
	private LightPoller colorPoller;
	private UltrasonicPoller ultrasonicPoller;

	private ArrayList<UltrasonicUser> currentUltrasonicUsers = new ArrayList<UltrasonicUser>();
	private ArrayList<LightSensorUser> currentLightSensorUsers = new ArrayList<LightSensorUser>();
	private ArrayList<ColorSensorUser> currentColorSensorUsers = new ArrayList<ColorSensorUser>();

	/**
	 * Constructor for sensor controller which cannot be accessed externally.
	 * 
	 * @param currentUltrasonicUser
	 * @param currentLeftLightSensorUser
	 * @param currentRightLightSensorUser
	 * @param currentColorSensorUser
	 */
	private SensorController(LightPoller lightPoller, LightPoller colorPoller, UltrasonicPoller ultrasonicPoller) {
		this.lightPoller = lightPoller;
		this.colorPoller = colorPoller;
		this.ultrasonicPoller = ultrasonicPoller;
		this.lightPoller.sensorController = this;
		this.colorPoller.sensorController = this;
		this.ultrasonicPoller.sensorController = this;
	}
	
	
	/**
	 * This method is meant to ensure only one instance of the sensorController is used
	 * throughout the code.
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @return new or existing SensorController Object
	 * @throws SensorExceptions
	 */
	public synchronized static SensorController getSensorController(LightPoller lightPoller, LightPoller colorPoller,
			UltrasonicPoller ultrasonicPoller) throws SensorExceptions {
		if (sensorController != null) { // Return existing object
			return sensorController;
		} else { // create object and return it
			sensorController = new SensorController(lightPoller, colorPoller, ultrasonicPoller);
			return sensorController;
		}
	}

	/**
	 * This class is meant to return the existing SensorController Object. It is meant to be
	 * used only if an sensorController object has been created
	 * 
	 * @return error if no previous sensor controller exists
	 */
	public synchronized static SensorController getSensorController() throws SensorExceptions {

		if (sensorController == null) {
			throw new SensorExceptions("No previous Sensor Controller exists.");

		}
		return sensorController;
	}
	

	//Pausing and unpausing methods
	/**
	 * Unpauses leftLightPoller
	 */
	public void unpauseLightPoller() {
		lightPoller.running = true;
	}
	
	/**
	 * Pauses leftLightPoller
	 */
	public void pauseLightPoller() {
		lightPoller.running = false;
	}
	
	/**
	 * Unpauses colorPoller
	 */
	public void unpauseColorPoller() {
		colorPoller.running = true;
	}
	
	/**
	 * Pauses colorPoller
	 */
	public void pauseColorPoller() {
		colorPoller.running = false;
	}
	
	/**
	 * Unpauses ultrasonicPoller
	 */
	public void unpauseUltrasonicPoller() {
		ultrasonicPoller.running = true;
	}
	
	/**
	 * Pauses colorPoller
	 */
	public void pauseUltrasonicPoller() {
		ultrasonicPoller.running = false;
	}
	
	
	//Setter methods for sensor user lists
	/**
	 * Setter method for ultrasonic user list
	 * @param currentUltrasonicUsers
	 */
	public void setCurrentUltrasonicUsers(ArrayList<UltrasonicUser> currentUltrasonicUsers) {
		this.currentUltrasonicUsers = currentUltrasonicUsers;
	}
	
	/**
	 * Setter method for left light sensor user list
	 * @param currentLeftLightSensorUsers
	 */
	public void setCurrentLeftLightSensorUsers(ArrayList<LightSensorUser> currentLightSensorUsers) {
		this.currentLightSensorUsers = currentLightSensorUsers;
	}
	
	/**
	 * Setter method for color sensor user list
	 * @param currentColorSensorUsers
	 */
	public void setCurrentColorSensorUsers(ArrayList<ColorSensorUser> currentColorSensorUsers) {
		this.currentColorSensorUsers = currentColorSensorUsers;
	}
	

	//Sensor data passing methods
	/**
	 * Sets the distance for all classes that use the ultrasonic sensor
	 * 
	 * @param distance
	 */
	public void setDistance(int distance) {
		//distance = usFilter.getFilteredValue(distance > 255 ? 255 : distance);
		for (UltrasonicUser ultrasonicUser : currentUltrasonicUsers) {
			ultrasonicUser.processUSDistance(distance);
		}
	}

	/**
	 * Sets the left light sensor value for all classes that use the left light sensor
	 * 
	 * @param light
	 */
	public void setLight(int[] light) {
		for (LightSensorUser lightSensorUser : currentLightSensorUsers) {
			lightSensorUser.processLSData(light);
		}
	}

	/**
	 * Sets the color value for all classes that use the color sensor. Also computes RGB normalization calculations and
	 * determines the color indicated by the RGB values.
	 * 
	 * @param light array of doubles, RGB values
	 */
	public void setColor(double[] light) {
		int color = 0;

		double R = light[0];
		double G = light[1];
		double B = light[2];

		double Rn = R / Math.sqrt(Math.pow(R, 2) + Math.pow(G, 2) + Math.pow(B, 2));
		double Gn = G / Math.sqrt(Math.pow(R, 2) + Math.pow(G, 2) + Math.pow(B, 2));
		double Bn = B / Math.sqrt(Math.pow(R, 2) + Math.pow(G, 2) + Math.pow(B, 2));

		double ORANGE_DISTANCE = Math
				.sqrt(Math.pow(Rn - ORANGE_R, 2) + Math.pow(Gn - ORANGE_G, 2) + Math.pow(Bn - ORANGE_B, 2));
		double YELLOW_DISTANCE = Math
				.sqrt(Math.pow(Rn - YELLOW_R, 2) + Math.pow(Gn - YELLOW_G, 2) + Math.pow(Bn - YELLOW_B, 2));
		double GREEN_DISTANCE = Math
				.sqrt(Math.pow(Rn - GREEN_R, 2) + Math.pow(Gn - GREEN_G, 2) + Math.pow(Bn - GREEN_B, 2));
		double BLUE_DISTANCE = Math
				.sqrt(Math.pow(Rn - BLUE_R, 2) + Math.pow(Gn - BLUE_G, 2) + Math.pow(Bn - BLUE_B, 2));

		if (ORANGE_DISTANCE < 0.1) {
			color = 4;
		} else if (YELLOW_DISTANCE < 0.12) {
			color = 3;
		} else if (BLUE_DISTANCE < 0.15) {
			color = 1;
		} else if (GREEN_DISTANCE < 0.1) {
			color = 2;
		}
		for (ColorSensorUser colorSensorUser : currentColorSensorUsers) {
			colorSensorUser.processColorData(color);
		}
	}

	//color sensor constants
	private static double ORANGE_MEAN_R = 0.133333;
	private static double ORANGE_MEAN_G = 0.031333;
	private static double ORANGE_MEAN_B = 0.008823;
	private static double YELLOW_MEAN_R = 0.0802921; //
	private static double YELLOW_MEAN_G = 0.0609802; //
	private static double YELLOW_MEAN_B = 0.016470; //
	private static double GREEN_MEAN_R = 0.024509; //0.067642;
	private static double GREEN_MEAN_G = 0.060784; //0.111789;
	private static double GREEN_MEAN_B = 0.007843; //0.022945;
	private static double BLUE_MEAN_R = 0.039234;
	private static double BLUE_MEAN_G = 0.118627;
	private static double BLUE_MEAN_B = 0.120593;

	private static double ORANGE_R = (ORANGE_MEAN_R
			/ Math.sqrt(Math.pow(ORANGE_MEAN_R, 2) + Math.pow(ORANGE_MEAN_G, 2) + Math.pow(ORANGE_MEAN_B, 2)));
	private static double ORANGE_G = (ORANGE_MEAN_G
			/ Math.sqrt(Math.pow(ORANGE_MEAN_R, 2) + Math.pow(ORANGE_MEAN_G, 2) + Math.pow(ORANGE_MEAN_B, 2)));
	private static double ORANGE_B = (ORANGE_MEAN_B
			/ Math.sqrt(Math.pow(ORANGE_MEAN_R, 2) + Math.pow(ORANGE_MEAN_G, 2) + Math.pow(ORANGE_MEAN_B, 2)));
	private static double YELLOW_R = (YELLOW_MEAN_R
			/ Math.sqrt(Math.pow(YELLOW_MEAN_R, 2) + Math.pow(YELLOW_MEAN_G, 2) + Math.pow(YELLOW_MEAN_B, 2)));
	private static double YELLOW_G = (YELLOW_MEAN_G
			/ Math.sqrt(Math.pow(YELLOW_MEAN_R, 2) + Math.pow(YELLOW_MEAN_G, 2) + Math.pow(YELLOW_MEAN_B, 2)));
	private static double YELLOW_B = (YELLOW_MEAN_B
			/ Math.sqrt(Math.pow(YELLOW_MEAN_R, 2) + Math.pow(YELLOW_MEAN_G, 2) + Math.pow(YELLOW_MEAN_B, 2)));
	private static double GREEN_R = (GREEN_MEAN_R
			/ Math.sqrt(Math.pow(GREEN_MEAN_R, 2) + Math.pow(GREEN_MEAN_G, 2) + Math.pow(GREEN_MEAN_B, 2)));
	private static double GREEN_G = (GREEN_MEAN_G
			/ Math.sqrt(Math.pow(GREEN_MEAN_R, 2) + Math.pow(GREEN_MEAN_G, 2) + Math.pow(GREEN_MEAN_B, 2)));
	private static double GREEN_B = (GREEN_MEAN_B
			/ Math.sqrt(Math.pow(GREEN_MEAN_R, 2) + Math.pow(GREEN_MEAN_G, 2) + Math.pow(GREEN_MEAN_B, 2)));
	private static double BLUE_R = (BLUE_MEAN_R
			/ Math.sqrt(Math.pow(BLUE_MEAN_R, 2) + Math.pow(BLUE_MEAN_G, 2) + Math.pow(BLUE_MEAN_B, 2)));
	private static double BLUE_G = (BLUE_MEAN_G
			/ Math.sqrt(Math.pow(BLUE_MEAN_R, 2) + Math.pow(BLUE_MEAN_G, 2) + Math.pow(BLUE_MEAN_B, 2)));
	private static double BLUE_B = (BLUE_MEAN_B
			/ Math.sqrt(Math.pow(BLUE_MEAN_R, 2) + Math.pow(BLUE_MEAN_G, 2) + Math.pow(BLUE_MEAN_B, 2)));
}
