/**
 * This class fetches data from the ultrasonic sensor.
 * @author Matthew Kourlas
 */
package ca.mcgill.ecse211.ultrasonicSensor;

import ca.mcgill.ecse211.odometer.Odometer;
import lejos.robotics.SampleProvider;

public class UltrasonicPoller extends Thread {
	
	private static final int US_POLLER_PERIOD = 50;

	private SampleProvider us;
	private float[] usData;
	private Odometer cont;

	/**
	 * Constructor for the UltrasonicPoller class.
	 * @param us
	 * @param usData
	 * @param cont
	 */
	public UltrasonicPoller(SampleProvider us, float[] usData, Odometer cont) {
		this.us = us;
	    this.usData = usData;
	    this.cont = cont;
	}
	
	/**
	 * This method continuously fetches the distance measured from the ultrasonic sensor.
	 * After fetching, it calls the setD method for the controller of the poller.
	 */
	public void run() {
		long updateStart, updateEnd, sleepPeriod;
		int distance;
		
		while(true) {
			updateStart = System.currentTimeMillis();
			
			us.fetchSample(usData, 0); // acquire data
		    distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int
		    cont.setD(distance); // now take action depending on value
		    
			updateEnd = System.currentTimeMillis();
			sleepPeriod = US_POLLER_PERIOD - (updateEnd - updateStart);
			try {
				if(sleepPeriod >= 0)
					Thread.sleep(sleepPeriod);
		    } catch (InterruptedException e) {
		    	return;	//end thread
		    }
		}
	}
}
