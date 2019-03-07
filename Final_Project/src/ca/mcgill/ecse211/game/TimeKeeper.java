package ca.mcgill.ecse211.game;

/**
 * This class keeps track of time for the duration of the challenge. 
 * 
 * @author Romain
 *
 */
public class TimeKeeper {
	
	public static long startTime;
	public static long navigationStartTime;

	/**
	 * Records the time at which the robot starts working
	 */
	public static void startTimer() {
		TimeKeeper.startTime = System.currentTimeMillis();
	}
	
	public static void startNavigationTimer() {
		TimeKeeper.navigationStartTime = System.currentTimeMillis();
	}
	
	
	/**
	 * Returns the number of seconds since the robot started working
	 * 
	 * @return the number of seconds since start of operation
	 */
	public static int getTime() {
		return (int)((System.currentTimeMillis() - TimeKeeper.startTime) / 1000); 
	}
	
	public static int getNavigationTime() {
		return (int)((System.currentTimeMillis() - TimeKeeper.navigationStartTime) / 1000); 
	}
	
}
