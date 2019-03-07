package ca.mcgill.ecse211.game;

import java.util.ArrayList;

import ca.mcgill.ecse211.interfaces.ColorSensorUser;
import ca.mcgill.ecse211.localization.Navigation;
import ca.mcgill.ecse211.odometry.Odometer;
import ca.mcgill.ecse211.odometry.OdometerExceptions;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * Contains methods to control the arm of the robot when searching the tower for
 * rings, attempting to grasp a ring off one side of the tower as well as useful
 * constants like the values of colors to be detected
 * 
 * @author Romain, Matthew
 */
public class RingSearcher implements ColorSensorUser {

	private static final int UPPER_LEVEL_DETECT = 310;
	private static final int LOWER_LEVEL_DETECT = 360;

	private int currentArmAngle = 180;	//0 to 360
	private int currentColorAngle = 0;
	private int colorDetected = 0;
	private double distanceFromOrigin = 0;
	private double offset = 0;
	private boolean detecting = false;
	private int count = 0;

	private static Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private EV3LargeRegulatedMotor armMotor;
	private EV3LargeRegulatedMotor colorMotor;

	public RingSearcher(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			EV3LargeRegulatedMotor armMotor, EV3LargeRegulatedMotor colorMotor) throws OdometerExceptions {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.armMotor = armMotor;
		this.colorMotor = colorMotor;
		RingSearcher.odometer = Odometer.getOdometer();
	}
	
	/**
	 * Getter method for the count field in RingSearcher
	 * @return number of rings the robot has retrieved
	 */
	public int getCount() {
		return count;
	}

	/**
	 * This method controls the logic for the robot to find and grab a ring off a
	 * side of the tower. In order for this method to work, the robot must be
	 * located at an intersection, one tile off the towers location.
	 * 
	 * It will start by running light localization to make sure it knows its exact
	 * location. Once completed, the robot turns towards the tower given its
	 * coordinates and the towers.
	 * 
	 * The robot will then try and detect if a ring is on the top level or bottom
	 * level of the tower repeatedly. Each iteration adds 0.5 to the offset in case
	 * the tower was not positioned as expected. This loop ends after 4 iterations
	 * if the robot cannot find a ring.
	 * 
	 * Finally, the robot will use this information to grab the ring off the tree
	 * and store it on the arm.
	 * 
	 * @param side side of the tree the robot is searching (1 = north, 2 = east, 3 = south, 4 = west)
	 */
	public void searchSide(int side) {

		colorMotor.setSpeed(RingChallenge.MOTOR_SPEED);
		Navigation.setMotorSpeeds(RingChallenge.MOTOR_SPEED, RingChallenge.leftMotor, RingChallenge.rightMotor,
				RingChallenge.L_WHEEL_RAD, RingChallenge.R_WHEEL_RAD);
		
		// Reset all variables
		colorDetected = 0;
		distanceFromOrigin = 0;
		offset = 0;

		if (side == 1) {
			Navigation.turnTo(180);
		} else if (side == 2) {
			Navigation.turnTo(270);
		} else if (side == 3) {
			Navigation.turnTo(0);
		} else if (side == 4) {
			Navigation.turnTo(90);
		}
		
		armMotor.setSpeed(RingChallenge.MOTOR_SPEED);
		turnArmTo(90, true);

		while(colorDetected == 0 && offset <= 2) {
			detectLevel(offset);
			offset += 0.5;
		}
		
		System.out.println("Color Detected: " + colorDetected);
		colorBeep(colorDetected);
		armMotor.setSpeed(RingChallenge.MOTOR_SPEED / 2);
		turnColorArmTo(0);
		
		syncForwardTo(7 + offset);
		
		switch(count) {
		case 0:
			turnArmTo(200, false);
			break;
		case 1:
			turnArmTo(215, false);
			break;
		case 2:
			turnArmTo(300, false);
			break;
		}
		
		count++;
		
		syncForwardTo(0);
	}

	/**
	 * returns the closest side (1 = north, 2 = east, 3 = south, 4 = west) to the
	 * current robot position. Returns -1 if there are no more sides.
	 * 
	 * @param availableSides ArrayList of sides accessible by the robot
	 * @return closest side
	 */
	public static int getClosestSide(ArrayList<Integer> availableSides) {
		int closestSide = -1;
		double lowestDistance = -1;

		for (int side : availableSides) {
			int[] coords = getSideCoordinates(side);
			double x = coords[0] * RingChallenge.TILE_SIZE - odometer.getXYT()[0];
			double y = coords[1] * RingChallenge.TILE_SIZE - odometer.getXYT()[1];
			double distance = Math.sqrt(x * x + y * y);
			if (lowestDistance == -1 || lowestDistance > distance) {
				lowestDistance = distance;
				closestSide = side;
			}
		}
		return closestSide;
	}

	/**
	 * returns the grid coordinates of a given side (1 = north, 2 = east, 3 = south, 4 = west)
	 * 
	 * @return grid coordinates of input side
	 */
	public static int[] getSideCoordinates(int side) {
		int[] coords = new int[2];
		switch (side) {
		case 1:
			coords[0] = WiFi.Tr_x;
			coords[1] = WiFi.Tr_y + 1;
			break;
		case 2:
			coords[0] = WiFi.Tr_x + 1;
			coords[1] = WiFi.Tr_y;
			break;
		case 3:
			coords[0] = WiFi.Tr_x;
			coords[1] = WiFi.Tr_y - 1;
			break;
		case 4:
			coords[0] = WiFi.Tr_x - 1;
			coords[1] = WiFi.Tr_y;
			break;
		default:
			System.out.println("Input side invalid; must be between 1 and 4 inclusive.");
			return null;
		}
		return coords;
	}

	/**
	 * The robot uses this method to find where a ring is located in the tree. It
	 * starts by checking the top level for a ring, followed by the bottom level. If
	 * a ring is detected then the corresponding boolean variable is set to true.
	 * 
	 * The offset is used in case the robot does not find the ring on the first run.
	 * If this is the case then an offset is added to the robots forward movement in
	 * case the tower was not positioned as expected.
	 * 
	 * @param offset Distance added to the robots forward movement
	 */
	private void detectLevel(double offset) {
		syncForwardTo(3 + offset);
		turnColorArmTo(UPPER_LEVEL_DETECT);
		detect();
		if(colorDetected == 0) {
			turnColorArmTo(LOWER_LEVEL_DETECT);
			detect();
		}
	}
	
	
	public void unload() {
		armMotor.setSpeed(RingChallenge.MOTOR_SPEED);
		turnArmTo(0, true);
		RingChallenge.armMotor.rotate(-360);
	}

	/**
	 * Given a new angle for the arm, this method uses the current position of the
	 * arm to rotate the motor to the specified angle.
	 * 
	 * @param angle New angle for the arm (angle from 0 to 360 in degrees)
	 * @param clockwise whether the arm turns clockwise
	 */
	public void turnArmTo(int angle, boolean clockwise) {
		int dAngle = (angle - currentArmAngle + 360) % 360;
		if(clockwise)
			armMotor.rotate(-dAngle);
		else
			armMotor.rotate(-(dAngle - 360));
		currentArmAngle = angle;
	}

	/**
	 * Given a new angle for the arm, this method uses the current position of the
	 * arm to rotate the motor to the specified angle.
	 * 
	 * @param angle New angle for the arm (in degrees)
	 */
	private void turnColorArmTo(int angle) {
		int dAngle = currentColorAngle - angle;
		colorMotor.rotate(-dAngle);
		currentColorAngle = angle;
	}

	/**
	 * Beeps the required number of times based on the input color
	 * 
	 * @param colorDetected Color of the ring
	 */
	private static void colorBeep(int colorDetected) {
		System.out.println(colorDetected);
		while (colorDetected != 0) {
			Sound.beep();
			colorDetected--;
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * helper method that waits a second for the colour sensor to detect a colour
	 */
	private void detect() {
		detecting = true;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		detecting = false;
	}
	
	/**
	 * helper method that moves the robot a certain distance from the grid line two tiles from the tree
	 * @param distance distance from grid line two tiles from tree
	 */
	private void syncForwardTo(double distance) {
		double dDistance = distance - distanceFromOrigin;
		leftMotor.rotate(Navigation.convertDistance(RingChallenge.L_WHEEL_RAD, dDistance), true);
		rightMotor.rotate(Navigation.convertDistance(RingChallenge.R_WHEEL_RAD, dDistance), false);
		distanceFromOrigin = distance;
	}
	
	/**
	 * This method checks which sides of the tree are accessible depending on its
	 * position on the island. If the tree is on a corner, then none of the sides
	 * are available. If it is on an edge but not a corner, then one side is
	 * available. If it is one square off from the corner, then two sides are
	 * available. If it is one square off from the edge, then there are three sides
	 * available. If it is sufficiently far from all edges and corners, then all
	 * sides are available for searching. It also calculates which sides of the tree
	 * are the ones available, ie the north, west, south or east sides.
	 *
	 * @return Arraylist of available sides, as a list of integers north = 1, east =
	 *         2, south = 3, west = 4
	 */
	public static ArrayList<Integer> checkSidesAvailable() {
		int treeX = WiFi.Tr_x;
		int treeY = WiFi.Tr_y;

		int oppTreeX = WiFi.opp_Tr_x;
		int oppTreeY = WiFi.opp_Tr_y;

		int islandLL_x = WiFi.Island_LL_x;
		int islandLL_y = WiFi.Island_LL_y;
		int islandUR_x = WiFi.Island_UR_x;
		int islandUR_y = WiFi.Island_UR_y;

		int BRR_LL_x = WiFi.TunLL_x;
		int BRR_LL_y = WiFi.TunLL_y;
		int BRR_UR_x = WiFi.TunUR_x;
		int BRR_UR_y = WiFi.TunUR_y;

		int BRG_LL_x = WiFi.opp_TunLL_x;
		int BRG_LL_y = WiFi.opp_TunLL_y;
		int BRG_UR_x = WiFi.opp_TunUR_x;
		int BRG_UR_y = WiFi.opp_TunUR_y;

		ArrayList<Integer> sides = new ArrayList<Integer>();

		// Check side 1
		if (islandUR_y == treeY + 1) {
		} else if (islandUR_x == treeX || islandLL_x == treeX) {
			System.out.println("Side 1 not available due to island size (X)");
		} else if ((BRR_LL_y == (treeY + 1)) && (BRR_LL_x == treeX - 1 || BRR_LL_x == treeX)) {
			System.out.println("Side 1 not available due to bridge restriction (BRR_LL)");
		} else if (BRG_LL_y == treeY + 1 && (BRG_LL_x == treeX - 1 || BRG_LL_x == treeX)) {
			System.out.println("Side 1 not available due to bridge restriction (BRG_LL)");
		} else if (BRR_UR_y == treeY + 2 && (BRR_UR_x == treeX && BRR_UR_x == treeX + 1)) {
			System.out.println("Side 1 not available due to bridge restriction (BRR_UR)");
		} else if (BRG_UR_y == treeY + 2 && (BRG_UR_x == treeX && BRG_UR_x == treeX + 1)) {
			System.out.println("Side 1 not available due to bridge restriction (BRG_UR)");
		} else if (oppTreeX == treeX && oppTreeY == treeY + 1) {
			System.out.println("Side 1 not available due to opponents tree");
		} else {
			sides.add(1);
		}

		// Check side 2
		if (islandUR_y == treeY || islandLL_y == treeY) {
			System.out.println("Side 2 not available due to island size (Y)");
		} else if (islandUR_x == treeX + 1) {
			System.out.println("Side 2 not available due to island size (X)");
		} else if (BRR_LL_x == treeX + 1 && (BRR_LL_y == treeY - 1 || BRR_LL_y == treeY)) {
			System.out.println("Side 2 not available due to bridge restriction (BRR_LL)");
		} else if (BRG_LL_x == treeX + 1 && (BRG_LL_y == treeY - 1 || BRG_LL_y == treeY)) {
			System.out.println("Side 2 not available due to bridge restriction (BRG_LL)");
		} else if (BRR_UR_x == treeX + 2 && (BRR_UR_y == treeY && BRR_UR_y == treeY + 1)) {
			System.out.println("Side 2 not available due to bridge restriction (BRR_UR)");
		} else if (BRG_UR_x == treeX + 2 && (BRG_UR_y == treeY && BRG_UR_y == treeY + 1)) {
			System.out.println("Side 2 not available due to bridge restriction (BRG_UR)");
		} else if (oppTreeX == treeX + 1 && oppTreeY == treeY) {
			System.out.println("Side 2 not available due to opponents tree");
		} else {
			sides.add(2);
		}

		// Check side 3
		if (islandLL_y == treeY - 1) {
			System.out.println("Side 3 not available due to island size (Y)");
		} else if (islandLL_x == treeX || islandUR_x == treeX) {
			System.out.println("Side 3 not available due to island size (X)");
		} else if (BRR_LL_y == treeY - 2 && (BRR_LL_x == treeX - 1 || BRR_LL_x == treeX)) {
			System.out.println("Side 3 not available due to bridge restriction (BRR_LL)");
		} else if (BRG_LL_y == treeY - 2 && (BRG_LL_x == treeX - 1 || BRG_LL_x == treeX)) {
			System.out.println("Side 3 not available due to bridge restriction (BRG_LL)");
		} else if (BRR_UR_y == treeY - 1 && (BRR_UR_x == treeX && BRR_UR_x == treeX + 1)) {
			System.out.println("Side 3 not available due to bridge restriction (BRR_UR)");
		} else if (BRG_UR_y == treeY - 1 && (BRG_UR_x == treeX && BRG_UR_x == treeX + 1)) {
			System.out.println("Side 3 not available due to bridge restriction (BRG_UR)");
		} else if (oppTreeX == treeX && oppTreeY == treeY - 1) {
			System.out.println("Side 3 not available due to opponents tree");
		} else {
			sides.add(3);
		}

		// Check side 4
		if (islandUR_y == treeY || islandLL_y == treeY) {
			System.out.println("Side 4 not available due to island size (Y)");
		} else if (islandLL_x == treeX - 1) {
			System.out.println("Side 4 not available due to island size (X)");
		} else if (BRR_LL_x == treeX - 2 && (BRR_LL_y == treeY - 1 || BRR_LL_y == treeY)) {
			System.out.println("Side 4 not available due to bridge restriction (BRR_LL)");
		} else if (BRG_LL_x == treeX - 2 && (BRG_LL_y == treeY - 1 || BRG_LL_y == treeY)) {
			System.out.println("Side 4 not available due to bridge restriction (BRG_LL)");
		} else if (BRR_UR_x == treeX - 1 && (BRR_UR_y == treeY && BRR_UR_y == treeY + 1)) {
			System.out.println("Side 4 not available due to bridge restriction (BRR_UR)");
		} else if (BRG_UR_x == treeX - 1 && (BRG_UR_y == treeY && BRG_UR_y == treeY + 1)) {
			System.out.println("Side 4 not available due to bridge restriction (BRG_UR)");
		} else if (oppTreeX == treeX - 1 && oppTreeY == treeY) {
			System.out.println("Side 4 not available due to opponents tree");
		} else {
			sides.add(4);
		}

		return sides;
	}

	/**
	 *colour data receiving method
	 *@param color colour detected by colour sensor
	 */
	@Override
	public void processColorData(int color) {
		if (color != 0 && detecting) {
			this.colorDetected = color;
		}
	}

}
