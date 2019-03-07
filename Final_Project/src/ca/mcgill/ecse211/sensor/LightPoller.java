package ca.mcgill.ecse211.sensor;

import lejos.robotics.SampleProvider;

/**
 * This class runs a thread that polls the light sensor at specific intervals of time and stores
 * the data in an array as RGB values
 * 
 * @author Romain, Matthew
 */
public class LightPoller extends Thread {

	private static final int COLOR_POLLER_PERIOD = 50;
	private static final int LINE_POLLER_PERIOD = 40;
	private int pollerPeriod;

	private LightPollerType sensorType;
	private SampleProvider[] sampleProvider;
	private float[][] data;
	SensorController sensorController;

	protected boolean running = false;

	/**
	 * Constructor for the color light poller
	 * @param colorSampleProvider
	 * @param colorData data buffer for light sensor
	 */
	public LightPoller(SampleProvider colorSampleProvider, float[] colorData) {
		this.sampleProvider = new SampleProvider[] {colorSampleProvider};
		this.data = new float[][] {colorData};
		this.sensorType = LightPollerType.COLOR;
		this.pollerPeriod = COLOR_POLLER_PERIOD;
	}
	
	
	/**
	 * Constructor for the line light poller
	 * 
	 * @param leftSampleProvider
	 * @param leftData data buffer for left light sensor
	 * @param rightSampleProvider
	 * @param rightData data buffer for right light sensor
	 */
	public LightPoller(SampleProvider leftSampleProvider, float[] leftData, SampleProvider rightSampleProvider,
			float[] rightData) {
		this.sampleProvider = new SampleProvider[] {leftSampleProvider, rightSampleProvider};
		this.data = new float[][] {leftData, rightData};
		this.sensorType = LightPollerType.LINE;
		this.pollerPeriod = LINE_POLLER_PERIOD;
	}
	

	/**
	 * polls the light sensor at the interval set by LS_POLLER_PERIOD
	 */
	public void run() {
		long updateStart, updateEnd, sleepPeriod;
		int[] intensity = new int[2];
		double[] colorRGB = new double[3];

		while (true) {
			
			updateStart = System.currentTimeMillis();
			
			if(running) {
				switch(sensorType) {
				case LINE:
					sampleProvider[0].fetchSample(data[0], 0); // acquire data (left)
					intensity[0] = (int) (data[0][0] * 100.0); // extract from buffer, cast to int (left)
					sampleProvider[1].fetchSample(data[1], 0); // acquire data (right)
					intensity[1] = (int) (data[1][0] * 100.0); // extract from buffer, cast to int (right)
					sensorController.setLight(intensity);
					break;
				case COLOR:
					sampleProvider[0].fetchSample(data[0], 0); // acquire data
					colorRGB[0] = (data[0][0]); 
					colorRGB[1] = (data[0][1]);
					colorRGB[2] = (data[0][2]);
					sensorController.setColor(colorRGB);
					break;
				}
			}

			updateEnd = System.currentTimeMillis();
			sleepPeriod = pollerPeriod - (updateEnd - updateStart);
			try {
				if (sleepPeriod >= 0)
					Thread.sleep(pollerPeriod - (updateEnd - updateStart));
			} catch (InterruptedException e) {
				return; // end thread
			}
		}
	}
}
