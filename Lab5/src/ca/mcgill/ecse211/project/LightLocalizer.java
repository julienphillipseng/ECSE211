/**
 * This class contains methods pertaining to using the light sensor for localization.
 * @author Matthew Kourlas
 */
package ca.mcgill.ecse211.project;

import ca.mcgill.ecse211.lightSensor.LightController;
import ca.mcgill.ecse211.navigation.Navigation;
import ca.mcgill.ecse211.odometer.Odometer;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class LightLocalizer implements LightController {

	private static final int INTENSITY_THRESHOLD = 7;	//change in intensity required to trigger line edge detection
	private static final double LS_DISTANCE = 15.0;		//distance from center of the robot to the light sensor
	private static final int ROTATE_SPEED = 150;		//motor speed while turning
	
	private static double WHEEL_RAD;
	private static double TRACK;
	
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	private static Odometer odometer;
	
	private int intensity;
	private int lastIntensity;
	private boolean inLine;
	private int lineCounter;
	
	private double angleXa;
	private double angleXb;
	private double angleYa;
	private double angleYb;
	
	
	/**
	 * Constructor method for the LightLocalizer class.
	 * @param leftMotor
	 * @param rightMotor
	 * @param odometer
	 * @param WHEEL_RAD
	 * @param TRACK
	 */
	public LightLocalizer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
	  Odometer odometer, double WHEEL_RAD, double TRACK) {
		LightLocalizer.leftMotor = leftMotor;
		LightLocalizer.rightMotor = rightMotor;
		LightLocalizer.odometer = odometer;
		LightLocalizer.WHEEL_RAD = WHEEL_RAD;
		LightLocalizer.TRACK = TRACK;
		lastIntensity = 0;
	}
	
	
	//Use light sensor to localize robot
	/*assumes robot starts relatively close to (0,0) and is roughly at a 45 degree angle so that the light sensor starts in
	 *the bottom left grid square */
	/**
	 * This method uses the light sensor to localize the robot. It assumes that the assumes robot starts relatively close to
	 * (0,0) and is roughly at a 45 degree angle so that the light sensor starts in the bottom left grid square.
	 */
	public void lightLocalization() {
		//assume light sensor does not start on a line
		lineCounter = 1;
		inLine = false;
		
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		//rotate counter-clockwise
		leftMotor.rotate(-Navigation.convertAngle(WHEEL_RAD, TRACK, 360), true);
		rightMotor.rotate(Navigation.convertAngle(WHEEL_RAD, TRACK, 360), false);
		
		//processLSData is responsible for collecting angle values
		
		//calculate x, y, and theta correction using 4 angles around (0,0) and apply them
		double dAngleX = angleXa - angleXb;
		if(dAngleX < 0)
			dAngleX += 360;
		double dAngleY = angleYa - angleYb;
		if(dAngleY < 0)
			dAngleY += 360;
		double x = LS_DISTANCE * -Math.cos(Math.PI * dAngleY / 360);
		double y = LS_DISTANCE * -Math.cos(Math.PI * dAngleX / 360);
		double dThetaY = 270 - angleYa + dAngleY / 2;	//theta correction calculated with y axis
		double dThetaX = 180 - angleXb - dAngleX / 2;	//theta correction calculated with x axis
		if(dThetaX > 180)
			dThetaX -= 360;
		if(dThetaY > 180)
			dThetaY -= 360;
		double dTheta = (dThetaY + dThetaX) / 2;					//average of theta corrections
		odometer.setXYT(x, y, odometer.getXYT()[2] + dTheta);
		
		System.out.println("Xa: "+angleXa);
		System.out.println("Xb: "+angleXb);
		System.out.println("Ya: "+angleYa);
		System.out.println("Yb: "+angleYb);
		
		System.out.println("X: "+x);
		System.out.println("Y: "+y);
		System.out.println("dThetaX: "+dThetaX);
		System.out.println("dThetaY: "+dThetaY);
	}

	
	/**
	 * This method is called after the light sensor reads a new light intensity. It determines whether the robot had crosses a line,
	 * and if so, records the angle of the robot for the purpose of localization calculations.
	 * @param intensity light intensity measures by the light sensor
	 */
	public void processLSData(int intensity) {
		this.lastIntensity = this.intensity;
		this.intensity = intensity;
		//compare current and last intensity reading to determine if a line has been entered or exited
		if(this.intensity - lastIntensity <= -INTENSITY_THRESHOLD && !inLine) {
			inLine = true;
			//get angle
			switch (lineCounter) {
			case 1:
				angleYa = odometer.getXYT()[2];
				break;
			case 2:
				angleXa = odometer.getXYT()[2];
				break;
			case 3:
				angleYb = odometer.getXYT()[2];
				break;
			case 4:
				angleXb = odometer.getXYT()[2];
				break;
			}
		}
		else if(this.intensity - lastIntensity >= INTENSITY_THRESHOLD && inLine) {
			inLine = false;
			Sound.beep();
			lineCounter++;
		}
	}

	
	/**
	 * light intensity get method
	 * @return intensity
	 */
	public int readLSIntensity() {
		return intensity;
	}

}
