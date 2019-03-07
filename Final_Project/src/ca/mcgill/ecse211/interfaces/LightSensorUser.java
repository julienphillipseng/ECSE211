package ca.mcgill.ecse211.interfaces;

/**
 * This interface contains methods that must be implemented by classes that
 * use the light sensors
 * 
 * @author Romain
 *
 */
public interface LightSensorUser {
	
	public void processLSData(int[] light);
	
}
