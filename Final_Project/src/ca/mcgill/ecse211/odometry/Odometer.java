package ca.mcgill.ecse211.odometry;

/**
 * The odometer class ensures that there is only one instance of an odometer at all times. It
 * has methods to ensure that is the case (singleton). It also contains the logic of the odometer 
 * itself. Angles are stored in degrees but are converted to radians for calculations.
 * 
 * @author Rodrigo Silva
 * @author Dirk Dubois
 * @author Derek Yu
 * @author Karim El-Baba
 * @author Michael Smith
 * 
 * Edited by:
 * @author Matthew
 */

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends OdometerData implements Runnable {

	private OdometerData odoData;
	private static Odometer odo = null; // Returned as singleton
	private static OdometryCorrection odoCorrection;

	// Motors and related variables
	private int leftMotorTachoCount;
	private int rightMotorTachoCount;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;

	private int lastTachoL;
	private int lastTachoR;
	private double Theta;

	private final double TRACK;
	private final double L_WHEEL_RAD;
	private final double R_WHEEL_RAD;

	private double[] position;

	private static final long ODOMETER_PERIOD = 25; // odometer update period in ms

	/**
	 * This is the default constructor of this class. It initiates all motors and
	 * variables once. It cannot be accessed externally.
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @throws OdometerExceptions
	 */
	private Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, final double TRACK,
			final double L_WHEEL_RAD, final double R_WHEEL_RAD) throws OdometerExceptions {
		odoData = OdometerData.getOdometerData(); // Allows access to x,y,z
													// manipulation methods
		
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;

		// Reset the values of x, y and z to 0
		odoData.setXYT(0, 0, 0);

		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;

		this.TRACK = TRACK;
		this.L_WHEEL_RAD = L_WHEEL_RAD;
		this.R_WHEEL_RAD = R_WHEEL_RAD;

	}

	/**
	 * This method is meant to ensure only one instance of the odometer is used
	 * throughout the code.
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @return new or existing Odometer Object
	 * @throws OdometerExceptions
	 */
	public synchronized static Odometer getOdometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			final double TRACK, final double L_WHEEL_RAD, final double R_WHEEL_RAD) throws OdometerExceptions {
		if (odo != null) { // Return existing object
			return odo;
		} else { // create object and return it
			odo = new Odometer(leftMotor, rightMotor, TRACK, L_WHEEL_RAD, R_WHEEL_RAD);
			odoCorrection = new OdometryCorrection(leftMotor, rightMotor);
			return odo;
		}
	}

	/**
	 * This class is meant to return the existing Odometer Object. It is meant to be
	 * used only if an odometer object has been created
	 * 
	 * @return error if no previous odometer exists
	 */
	public synchronized static Odometer getOdometer() throws OdometerExceptions {

		if (odo == null) {
			throw new OdometerExceptions("No previous Odometer exits.");

		}
		return odo;
	}
	
	/**
	 * This method enables odometer correction by the odometry correction class
	 */
	public synchronized void enableCorrection() {
		odoCorrection.running = true;
	}
	
	/**
	 * This method disables odometer correction by the odometry correction class
	 */
	public synchronized void disableCorrection() {
		odoCorrection.running = false;
	}
	
	public OdometryCorrection getOdometryCorrection() {
		return odoCorrection;
	}

	/**
	 * This method is where the logic for the odometer will run. Use the methods
	 * provided from the OdometerData class to implement the odometer.
	 */
	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;

		while (true) {
			updateStart = System.currentTimeMillis();

			//Get the current tachoCount for each motor
			leftMotorTachoCount = leftMotor.getTachoCount();
			rightMotorTachoCount = rightMotor.getTachoCount();

			//Get the current values of the robot
			position = odo.getXYT();

			// Calculate new robot position based on tachometer counts
			double distL = Math.PI * L_WHEEL_RAD * (leftMotorTachoCount - lastTachoL) / 180; // compute wheel
			double distR = Math.PI * R_WHEEL_RAD * (rightMotorTachoCount - lastTachoR) / 180; // displacements
			lastTachoL = leftMotorTachoCount; // save tacho counts for next iteration
			lastTachoR = rightMotorTachoCount;
			double deltaD = 0.5 * (distL + distR); // compute vehicle displacement
			double deltaT = (distL - distR) / TRACK;
			Theta = position[2] * Math.PI / 180; // Converting Theta back to radians for the calculations
			Theta += deltaT; // update heading
			double dX = deltaD * Math.sin(Theta); // compute X component of displacement
			double dY = deltaD * Math.cos(Theta); // compute Y component of displacement

			// Update odometer values with new calculated values
			odo.update(dX, dY, deltaT * 180 / Math.PI);

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done
				}
			}
		}
	}

}
