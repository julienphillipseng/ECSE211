/**
 * This thread detects the color of rings with the data from the color sensor
 * @author
 */

package ca.mcgill.ecse211.project;

import java.util.ArrayList;

import ca.mcgill.ecse211.navigation.Navigation;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class Detection {

	private static Odometer odo;
	private static Navigation navigation;
	
	private static final int FORWARD_SPEED = 100;
	
	private static double WHEEL_RAD;
	private static double TRACK;
	private static double TILE_SIZE;
	
	private static ArrayList<Double> ringAngles = new ArrayList<Double>();
	
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	
	private static double ORANGE_MEAN_R = 0.133333;
	private static double ORANGE_MEAN_G = 0.031333;
	private static double ORANGE_MEAN_B = 0.008823;
	private static double YELLOW_MEAN_R = 0.202921;
	private static double YELLOW_MEAN_G = 0.109802;
	private static double YELLOW_MEAN_B = 0.026470;
	private static double GREEN_MEAN_R = 0.067642;
	private static double GREEN_MEAN_G = 0.111789;
	private static double GREEN_MEAN_B = 0.022945;
	private static double BLUE_MEAN_R = 0.039234;
	private static double BLUE_MEAN_G = 0.118627;
	private static double BLUE_MEAN_B = 0.120593;
	
	private static double ORANGE_R = (ORANGE_MEAN_R/Math.sqrt(Math.pow(ORANGE_MEAN_R, 2) + Math.pow(ORANGE_MEAN_G, 2) + Math.pow(ORANGE_MEAN_B, 2)));
	private static double ORANGE_G = (ORANGE_MEAN_G/Math.sqrt(Math.pow(ORANGE_MEAN_R, 2) + Math.pow(ORANGE_MEAN_G, 2) + Math.pow(ORANGE_MEAN_B, 2)));
	private static double ORANGE_B = (ORANGE_MEAN_B/Math.sqrt(Math.pow(ORANGE_MEAN_R, 2) + Math.pow(ORANGE_MEAN_G, 2) + Math.pow(ORANGE_MEAN_B, 2)));
	private static double YELLOW_R = (YELLOW_MEAN_R/Math.sqrt(Math.pow(YELLOW_MEAN_R, 2) + Math.pow(YELLOW_MEAN_G, 2) + Math.pow(YELLOW_MEAN_B, 2)));
	private static double YELLOW_G = (YELLOW_MEAN_G/Math.sqrt(Math.pow(YELLOW_MEAN_R, 2) + Math.pow(YELLOW_MEAN_G, 2) + Math.pow(YELLOW_MEAN_B, 2)));
	private static double YELLOW_B = (YELLOW_MEAN_B/Math.sqrt(Math.pow(YELLOW_MEAN_R, 2) + Math.pow(YELLOW_MEAN_G, 2) + Math.pow(YELLOW_MEAN_B, 2)));
	private static double GREEN_R = (GREEN_MEAN_R/Math.sqrt(Math.pow(GREEN_MEAN_R, 2) + Math.pow(GREEN_MEAN_G, 2) + Math.pow(GREEN_MEAN_B, 2)));
	private static double GREEN_G = (GREEN_MEAN_G/Math.sqrt(Math.pow(GREEN_MEAN_R, 2) + Math.pow(GREEN_MEAN_G, 2) + Math.pow(GREEN_MEAN_B, 2)));
	private static double GREEN_B = (GREEN_MEAN_B/Math.sqrt(Math.pow(GREEN_MEAN_R, 2) + Math.pow(GREEN_MEAN_G, 2) + Math.pow(GREEN_MEAN_B, 2)));
	private static double BLUE_R = (BLUE_MEAN_R/Math.sqrt(Math.pow(BLUE_MEAN_R, 2) + Math.pow(BLUE_MEAN_G, 2) + Math.pow(BLUE_MEAN_B, 2)));
	private static double BLUE_G = (BLUE_MEAN_G/Math.sqrt(Math.pow(BLUE_MEAN_R, 2) + Math.pow(BLUE_MEAN_G, 2) + Math.pow(BLUE_MEAN_B, 2)));
	private static double BLUE_B = (BLUE_MEAN_B/Math.sqrt(Math.pow(BLUE_MEAN_R, 2) + Math.pow(BLUE_MEAN_G, 2) + Math.pow(BLUE_MEAN_B, 2)));
	
	/**
	 * Constructor for detection class
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @param WHEEL_RAD
	 * @param TRACK
	 * @param TILE_SIZE
	 * @param odometer	global odometer object
	 * @param navigation	global navigation object
	 * @throws OdometerExceptions
	 */
	public Detection(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			  double WHEEL_RAD, double TRACK, double TILE_SIZE, Odometer odometer, Navigation navigation) throws OdometerExceptions {
		Detection.odo = odometer;
		Detection.leftMotor = leftMotor;
		Detection.rightMotor = rightMotor;
		Detection.WHEEL_RAD = WHEEL_RAD;
		Detection.TRACK = TRACK;
		Detection.TILE_SIZE = TILE_SIZE;
		Detection.navigation = navigation;
	}
	
	/**
	 * Detects the color of the ring
	 * 
	 * @return the number associated to the ring color
	 * @throws InterruptedException
	 */
	public int detect() throws InterruptedException
	{
		Lab5.lcd.clear();
		boolean colorDetected = false;
		int color = 0;
		
		while(!colorDetected) {
			double R = odo.getDLRGB()[2];
			double G = odo.getDLRGB()[3];
			double B = odo.getDLRGB()[4];
			
			double Rn = R / Math.sqrt(Math.pow(R, 2) + Math.pow(G, 2) + Math.pow(B, 2));
			double Gn = G / Math.sqrt(Math.pow(R, 2) + Math.pow(G, 2) + Math.pow(B, 2));
			double Bn = B / Math.sqrt(Math.pow(R, 2) + Math.pow(G, 2) + Math.pow(B, 2));
			
			double ORANGE_DISTANCE = Math.sqrt(Math.pow(Rn - ORANGE_R, 2) + Math.pow(Gn - ORANGE_G, 2) + Math.pow(Bn - ORANGE_B, 2));
			double YELLOW_DISTANCE = Math.sqrt(Math.pow(Rn - YELLOW_R, 2) + Math.pow(Gn - YELLOW_G, 2) + Math.pow(Bn - YELLOW_B, 2));
			double GREEN_DISTANCE = Math.sqrt(Math.pow(Rn - GREEN_R, 2) + Math.pow(Gn - GREEN_G, 2) + Math.pow(Bn - GREEN_B, 2));
			double BLUE_DISTANCE = Math.sqrt(Math.pow(Rn - BLUE_R, 2) + Math.pow(Gn - BLUE_G, 2) + Math.pow(Bn - BLUE_B, 2));
			
			if(ORANGE_DISTANCE < 0.1) {
				Lab5.lcd.drawString("Color Detected: ", 0, 1);
				Lab5.lcd.drawString("ORANGE ", 0, 2);
				colorDetected = true;
				color = 4;
			} else if(YELLOW_DISTANCE < 0.1) {
				Lab5.lcd.drawString("Color Detected: ", 0, 1);
				Lab5.lcd.drawString("YELLOW ", 0, 2);
				colorDetected = true;
				color = 3;
			} else if(BLUE_DISTANCE < 0.1) {
				Lab5.lcd.drawString("Color Detected: ", 0, 1);
				Lab5.lcd.drawString("BLUE ", 0, 2);
				colorDetected = true;
				color = 1;
			} else if(GREEN_DISTANCE < 0.1) {
				Lab5.lcd.drawString("Color Detected: ", 0, 1);
				Lab5.lcd.drawString("GREEN ", 0, 2);
				colorDetected = true;
				color = 2;
			} 
			
		}
		return color;
	}
	
	/**
	 * This method implements the searching algorithm
	 * @throws InterruptedException
	 */
	public void search() throws InterruptedException
	{
		//depending on size of search area
		//travel to a corner
		//do a 360
		//every time the USdistance falls below a certain distance
		//store the angle
		//after the 360, turn to each angle with detected ring, check color
		//either move to next corner or UR 
		int color = 0;
		int target = Lab5.TR;
		Thread.sleep(1000);
		
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		
		leftMotor.rotate(Navigation.convertAngle(WHEEL_RAD, TRACK, 360), true);
		rightMotor.rotate(-Navigation.convertAngle(WHEEL_RAD, TRACK, 360), true);	
		
		Lab5.lcd.clear();
		
		while(leftMotor.isMoving()) //while the robot is turning in a circle, scan for rings
		{
			Lab5.lcd.drawString("Distance: " + odo.getDLRGB()[0], 0, 1);
			if(odo.getDLRGB()[0] < 40)
			{
				double angle1 = odo.getXYT()[2];
				System.out.println("Angle 1: " + angle1);
				while (odo.getDLRGB()[0] < 50) {
				
				}
				double angle2 = odo.getXYT()[2];
				ringAngles.add((angle1 + angle2) / 2.0);
				System.out.println("Angle 2: " + angle2);
			}
		}
		
		Lab5.lcd.clear();
		
		for(Double angle: ringAngles) {
			System.out.println("Angle" + angle);
		}
		
		for(Double angle: ringAngles) //check each ring to see if it's the target
		{
			System.out.println("Turning to: " + angle);
			navigation.turnTo(angle * Math.PI/180);
			
			System.out.println("Done turning");
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			
			leftMotor.forward();
			rightMotor.forward();
			
			long firstTime = System.currentTimeMillis();
			color = this.detect();
			leftMotor.stop();
			rightMotor.stop();
			long secondTime = System.currentTimeMillis();
			
			long travelTime = secondTime - firstTime;
			
			double rotationsTraveled = FORWARD_SPEED * travelTime * 1000;
			
			if(color == target) {
				Sound.twoBeeps();
			} else {
				Sound.beep();
			}
			
			leftMotor.rotate(-(int) rotationsTraveled);
			rightMotor.rotate(-(int) rotationsTraveled);
			
		}
		
		ringAngles.clear();
		
	}
}
