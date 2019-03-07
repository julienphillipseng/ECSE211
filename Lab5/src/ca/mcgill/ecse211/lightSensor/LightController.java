/**
 * Interface for classes that respond to light intensity readings from
 * LightPoller class
 * @author Matthew Kourlas
 */
package ca.mcgill.ecse211.lightSensor;

public interface LightController {

	public void processLSData(int distance);

	public int readLSIntensity();
}
