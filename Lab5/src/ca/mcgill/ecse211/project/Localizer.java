/**
 * This class defines the localization thread.
 * @author Romain Couperier
 * @author Lillian Chiu
 * @author Lara Kollokian
 */
package ca.mcgill.ecse211.project;

import ca.mcgill.ecse211.navigation.Navigation;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.hardware.Button;
import lejos.hardware.lcd.TextLCD;

public class Localizer extends Thread {
	
	private static UltrasonicLocalizer usLocalizer;
	private static LightLocalizer lsLocalizer;
	private static Thread usPollerThread;
	private static Thread lsPollerThread;
	private static TextLCD lcd;
	private static Navigation navigation;
	private static Detection detection;
	
	public static int SC = Lab5.SC;
	
	private int usLocType;

	private static final double TILE_SIZE = Lab5.TILE_SIZE;

	/**
	 * The constructor for the Localizer class
	 * @param usLocalizer
	 * @param lsLocalizer
	 * @param usPollerThread
	 * @param lsPollerThread
	 * @param navigation
	 * @param usLocType
	 * @param lcd
	 * @param detection
	 */
	public Localizer(UltrasonicLocalizer usLocalizer, LightLocalizer lsLocalizer, Thread usPollerThread, Thread lsPollerThread,
	  Navigation navigation, int usLocType, TextLCD lcd, Detection detection) {
		Localizer.usLocalizer = usLocalizer;
		Localizer.lsLocalizer = lsLocalizer;
		Localizer.usPollerThread = usPollerThread;
		Localizer.lsPollerThread = lsPollerThread;
		Localizer.navigation = navigation;
		Localizer.lcd = lcd;
		this.usLocType = usLocType;
		Localizer.detection = detection;
	}
	
	
	/**
	 * This method is the Localization thread run method. It localizes the robot when it is placed in the (-1,-1) grid position on
	 * the 45 degree line from  (-1,-1) to (0,0) using ultrasonic and light localization.
	 */
	public void run() {
		usPollerThread.start();
		
		lsPollerThread.start();
		//wait 3 seconds for sensor to start
		try {
			sleep(1000);
		} catch (InterruptedException e) {
			//do nothing
		}
		
		usLocalizer.fallingEdge();
		
		//usPollerThread.interrupt();
		
		navigation.turnTo(0);
		
		navigation.travelTo(0, 0);
		
		//wait three seconds for sensor to start
		try {
			sleep(1000);
		} catch (InterruptedException e) {
			//do nothing
		}
		
		lsLocalizer.lightLocalization();
		
		//lsPollerThread.interrupt();
		
		navigation.travelTo(0, 0);
		navigation.turnTo(0);
		
		Odometer odo = null;
		try {
			odo = Odometer.getOdometer();
		} catch (OdometerExceptions e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(SC == 0) {
			odo.setXYT(TILE_SIZE, TILE_SIZE, 0);
		} else if (SC == 1) {
			odo.setXYT(7 * TILE_SIZE, TILE_SIZE, 0);
		} else if (SC == 2) {
			odo.setXYT(7 * TILE_SIZE, 7 * TILE_SIZE, 0);
		} else if (SC == 3) {
			odo.setXYT(TILE_SIZE, 7 * TILE_SIZE, 0);
		}
		
		navigation.travelTo(Lab5.LL_X, Lab5.LL_Y);
		navigation.travelTo(Lab5.LL_X + 1, Lab5.LL_Y + 1);
		
//		try {
//			detection.search();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		navigation.travelTo(Lab5.UR_X, Lab5.UR_Y);
	}
	
}
