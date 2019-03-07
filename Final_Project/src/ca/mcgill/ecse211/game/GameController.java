package ca.mcgill.ecse211.game;

import java.util.ArrayList;

import ca.mcgill.ecse211.interfaces.*;
import ca.mcgill.ecse211.localization.*;
import ca.mcgill.ecse211.odometry.*;
import ca.mcgill.ecse211.sensor.SensorController;
import lejos.hardware.Sound;

/**
 * This class contains the state machine. Each change in state pauses or resumes
 * threads that will be needed for that particular state. It also contains the
 * run method that controls the flow of the entire game.
 * 
 * @author Romain, Lara, Matthew
 *
 */
public class GameController implements Runnable {

	private SensorController sensorController;
	private LightLocalization lsLocalization;
	private UltrasonicLocalization usLocalization;
	private GameNavigation gameNav;
	private RingSearcher ringSearcher;
	private OdometryCorrection odoCorrection;
	private Odometer odometer;

	public static GameState state;

	public GameController(SensorController sensorController, LightLocalization lsLocalization,
			UltrasonicLocalization usLocalization, RingSearcher ringSearcher,
			OdometryCorrection odoCorrection, GameNavigation gameNav, Odometer odometer) {
		this.sensorController = sensorController;
		this.lsLocalization = lsLocalization;
		this.usLocalization = usLocalization;
		this.ringSearcher = ringSearcher;
		this.odoCorrection = odoCorrection;
		this.gameNav = gameNav;
		this.odometer = odometer;
	}

	@Override
	public void run() {
		finalDemo();
	}

	/**
	 * Necessary method calls and state changes for final demo requirements
	 */
	public void finalDemo() {
		
		// Ultrasonic Localization
		changeState(GameState.USLOCALIZATION);
		usLocalization.fallingEdge();
		Navigation.travelTo(8.0, 8.0, false);

		// Light Localization
		changeState(GameState.LSLOCALIZATION);
		Navigation.turnTo(60);
		lsLocalization.lightLocalization(WiFi.localizeX, WiFi.localizeY, WiFi.corner);
		Navigation.travelTo(WiFi.localizeX, WiFi.localizeY, false);
		for (int i = 0; i < 3; i++)
			Sound.beep();
		
		System.out.println("Done Localization");
		System.out.println(odometer.getXYT()[0]);
		System.out.println(odometer.getXYT()[1]);

		TimeKeeper.startNavigationTimer();
		
		// Travel to Tunnel
		changeState(GameState.NAVIGATION);
		int tunnelOrientation = GameNavigation.getTunnelOrientation(true);
		gameNav.navToTunnel(tunnelOrientation);
		
		System.out.println("At Tunnel");
		System.out.println(odometer.getXYT()[0]);
		System.out.println(odometer.getXYT()[1]);

		// Traverse Tunnel
		int[][] obstacles = GameNavigation.getIslandObstacles();
		GameNavigation.navInTunnelToIsland(tunnelOrientation, obstacles);

		// Travel to Tower (avoiding obstacles)
		int[] tunnelCoordinates = Navigation.getClosestCoordinates(odometer.getXYT()[0], odometer.getXYT()[1]);
		ArrayList<Integer> availableSides = RingSearcher.checkSidesAvailable();
		System.out.println(availableSides.size());
		boolean localizationNeeded = GameNavigation.tunnelToTree(tunnelCoordinates, obstacles, availableSides);
		if(localizationNeeded) {
			this.changeState(GameState.LSLOCALIZATION);
			lsLocalization.lightLocalization(tunnelCoordinates[0], tunnelCoordinates[1], Navigation.getQuadrant(odometer.getXYT()[2]));
			Navigation.travelTo(tunnelCoordinates[0], tunnelCoordinates[1], false);
		}
		else {
			int[] treeCoords = Navigation.getClosestCoordinates(odometer.getXYT()[0], odometer.getXYT()[1]);
			if(Math.sqrt(Math.pow(treeCoords[0] - odometer.getXYT()[0], 2) + Math.pow(treeCoords[1] - odometer.getXYT()[1], 2)) >= 2.5)
				Navigation.travelTo(treeCoords[0], treeCoords[1], false);
		}
		
		for (int i = 0; i < 3; i++)
			Sound.beep();
		
		int navigationTime = TimeKeeper.getNavigationTime();
		System.out.println("Need " + navigationTime + " seconds to get back");

		// Search Tower
		do {
			this.changeState(GameState.NAVIGATION);
			int side = gameNav.navAroundTree(obstacles, availableSides);
			if (side != -1) {
				this.changeState(GameState.TOWERSEARCH);
				ringSearcher.searchSide(side);
			}
			else {
				System.out.println("All remaining sides inaccessible; going back");
				break;
			}
		} while (RingChallenge.GAME_TIME - TimeKeeper.getTime() > navigationTime + 45 && ringSearcher.getCount() < 3);
		if(RingChallenge.GAME_TIME - TimeKeeper.getTime() <= navigationTime + 45)
			System.out.println("Ran out of time; going back");
		else
			System.out.println("Got all of the rings can carry; going back");

		// Travel back to Tunnel
		changeState(GameState.NAVIGATION);
		GameNavigation.treeToTunnel(tunnelCoordinates, obstacles);

		// Traverse Tunnel
		GameNavigation.navInTunnelFromIsland(tunnelOrientation, obstacles);

		// Travel to start
		GameNavigation.tunnelToStart();
		
		//unload rings
		ringSearcher.unload();
		
		for (int i = 0; i < 5; i++)
			Sound.beep();
		
		GameNavigation.weirdFlexButOk();
	}

	/**
	 * This method changes the state of the game
	 * 
	 * @param newState State to be changed to
	 */
	public void changeState(GameState newState) {
		state = newState;
		ArrayList<UltrasonicUser> currentUltrasonicUsers = new ArrayList<UltrasonicUser>();
		ArrayList<LightSensorUser> currentLightSensorUsers = new ArrayList<LightSensorUser>();
		ArrayList<ColorSensorUser> currentColorSensorUsers = new ArrayList<ColorSensorUser>();
		switch (state) {
		case INSTRUCTIONS:
			sensorController.pauseUltrasonicPoller();
			sensorController.pauseLightPoller();
			sensorController.pauseColorPoller();
			break;
		case TESTING:
			sensorController.unpauseUltrasonicPoller();
			sensorController.unpauseLightPoller();
			sensorController.unpauseColorPoller();
			break;
		case USLOCALIZATION:
			currentUltrasonicUsers.add(usLocalization);
			sensorController.unpauseUltrasonicPoller();
			sensorController.pauseLightPoller();
			sensorController.pauseColorPoller();
			break;
		case LSLOCALIZATION:
			currentLightSensorUsers.add(lsLocalization);
			sensorController.pauseUltrasonicPoller();
			sensorController.unpauseLightPoller();
			sensorController.pauseColorPoller();
			break;
		case NAVIGATION:
			currentLightSensorUsers.add(odoCorrection);
			sensorController.pauseUltrasonicPoller();
			sensorController.unpauseLightPoller();
			sensorController.pauseColorPoller();
			break;
		case TUNNEL:
			sensorController.pauseUltrasonicPoller();
			sensorController.pauseLightPoller();
			sensorController.pauseColorPoller();
			break;
		case TOWERSEARCH:
			currentColorSensorUsers.add(ringSearcher);
			sensorController.pauseUltrasonicPoller();
			sensorController.unpauseLightPoller();
			sensorController.unpauseColorPoller();
			break;
		case DONE:
			sensorController.pauseUltrasonicPoller();
			sensorController.pauseLightPoller();
			sensorController.pauseColorPoller();
			break;
		}
		sensorController.setCurrentUltrasonicUsers(currentUltrasonicUsers);
		sensorController.setCurrentLeftLightSensorUsers(currentLightSensorUsers);
		sensorController.setCurrentColorSensorUsers(currentColorSensorUsers);
	}
}
