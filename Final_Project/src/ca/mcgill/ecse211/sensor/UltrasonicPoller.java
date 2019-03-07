package ca.mcgill.ecse211.sensor;

import lejos.robotics.SampleProvider;

/**
 * This class runs a thread that polls the ultrasonic sensor at specific intervals of time and stores
 * the data in the sensorController instance
 * 
 * @author Romain, Matthew
 */
public class UltrasonicPoller extends Thread {

	private static final int US_POLLER_PERIOD = 50;

	private SampleProvider usSP;
	private float[] usData;
	SensorController sensorController;
	
	public boolean running = false;

	/**
	 * Constructor for ultrasonic poller
	 * 
	 * @param usSP ultrasonic sample provider
	 * @param usData data buffer for ultrasonic sensor
	 */
	public UltrasonicPoller(SampleProvider usSP, float[] usData) {
		this.usSP = usSP;
		this.usData = usData;
	}
	

	/**
	 * polls the ultrasonic sensor at the interval set by US_POLLER_PERIOD
	 */
	public void run() {
		long updateStart, updateEnd, sleepPeriod;
		int distance;

		while (true) {
			updateStart = System.currentTimeMillis();
			
			if (running) {
				usSP.fetchSample(usData, 0); // acquire data
				distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int
				sensorController.setDistance(distance); // now take action depending on value
			}
			
			updateEnd = System.currentTimeMillis();
			sleepPeriod = US_POLLER_PERIOD - (updateEnd - updateStart);
			try {
				if (sleepPeriod >= 0)
					Thread.sleep(sleepPeriod);
			} catch (InterruptedException e) {
				return; // end thread
			}
		}
	}
}
