package ca.mcgill.ecse211.interfaces;

/**
 * This interface contains methods that must be implemented by classes that
 * use the ultrasonic sensor
 * 
 * @author Romain
 *
 */
public interface UltrasonicUser {
	
	public void processUSDistance(int distance);
	
}
