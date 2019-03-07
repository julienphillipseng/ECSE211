package ca.mcgill.ecse211.sensor;

/**
 * This class is used to handle errors regarding the singleton pattern used for the sensorController and sensor pollers
 *
 */

@SuppressWarnings("serial")
public class SensorExceptions extends Exception {

	public SensorExceptions(String Error) {
		super(Error);
	}
}
