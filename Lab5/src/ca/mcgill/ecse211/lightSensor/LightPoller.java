/**
 * Fetches data from light/color sensor
 * @author Matthew Kourlas
 */
package ca.mcgill.ecse211.lightSensor;

import ca.mcgill.ecse211.odometer.Odometer;
import lejos.robotics.SampleProvider;

public class LightPoller extends Thread {

	private static final int LS_POLLER_PERIOD = 50;

	private LightController cont;
	private SampleProvider line;
	private SampleProvider color;
	private Odometer odo;
	private float[] lineData;
	private float[] colorData;

	/**
	 * Constructor for the LightPoller Class
	 * 
	 * @param line	intensity sample provider
	 * @param lineData	intensity data buffer
	 * @param color	RGB sample provider
	 * @param colorData	RGB data buffer
	 * @param odo	odometer
	 * @param cont	Lightcontroller object
	 */
	public LightPoller(SampleProvider line, float[] lineData, SampleProvider color, float[] colorData, Odometer odo, LightController cont) {
	    this.line = line;
	    this.color = color;
	    this.odo = odo;
	    this.lineData = lineData;
	    this.colorData = colorData;
	    this.cont = cont;
	  }

	/**
	 * Fetches light intensity and RGB intensities from the colour
	 * sensor and updates the odometer's RGB values and controller's
	 * intensity value respectively
	 */
	public void run() {
		long updateStart, updateEnd, sleepPeriod;
		int lineInt;
		double[] colorRGB = new double[3];

		while (true) {
			updateStart = System.currentTimeMillis();

			line.fetchSample(lineData, 0); // acquire data
			lineInt = (int) (lineData[0] * 100.0); // extract from buffer, cast to int
			odo.setLine(lineInt);
			cont.processLSData(lineInt); // now take action depending on value
			
			color.fetchSample(colorData, 0); // acquire data
			colorRGB[0] = (colorData[0]); // extract from buffer, cast to int
			colorRGB[1] = (colorData[1]);
			colorRGB[2] = (colorData[2]);
			odo.setColor(colorRGB);

			updateEnd = System.currentTimeMillis();
			sleepPeriod = LS_POLLER_PERIOD - (updateEnd - updateStart);
			try {
				if (sleepPeriod >= 0)
					Thread.sleep(LS_POLLER_PERIOD - (updateEnd - updateStart));
			} catch (InterruptedException e) {
				return; // end thread
			}
		}
	}
}

