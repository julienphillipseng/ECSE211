package ca.mcgill.ecse211.odometry;

/**
 * This class is used to handle errors regarding the singleton pattern used for the odometer and
 * odometerData. Taken from the sample code given for the labs.
 *
 */
@SuppressWarnings("serial")
public class OdometerExceptions extends Exception {

  public OdometerExceptions(String Error) {
    super(Error);
  }

}
