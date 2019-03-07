package ca.mcgill.ecse211.interfaces;

/**
 * This interface contains methods that must be implemented by classes that
 * use the color sensor
 * 
 * @author Romain
 *
 */
public interface ColorSensorUser {
	
	public void processColorData(int color);
}
