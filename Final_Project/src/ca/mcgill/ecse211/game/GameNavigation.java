package ca.mcgill.ecse211.game;

import java.util.ArrayList;

import ca.mcgill.ecse211.localization.Navigation;
import ca.mcgill.ecse211.odometry.Odometer;
import lejos.hardware.Sound;

/**
 * This class contains higher-level navigation methods, such as methods that
 * drive it from the starting point to the tunnel, through the tunnel, to the
 * tree, around the tree searching for rings, back to the tunnel, and back to
 * the starting point. It calls methods from the Navigation class and the
 * RingSearcher class.
 * 
 * @author Lara, Romain, Matthew
 */
public class GameNavigation {

	private static Odometer odometer;

	
	/**
	 * 
	 * @param odo odometer
	 */
	public GameNavigation(Odometer odometer) {
		GameNavigation.odometer = odometer;
	}
	
	/**
	 * Returns the tunnel orientation of the input tunnel.
	 * @return integer representation of tunnel orientation:
	 * 1 = vertical & LL on starting island, 2 = vertical & LL on island, 
	 * 3 = horizontal & LL on starting island, 4 = horizontal && LL on island
	 */
	public static int getTunnelOrientation(boolean ourTunnel) {
		int LL_x, LL_y, UR_x, UR_y;
		if(ourTunnel) {
			LL_x = WiFi.LL_x;
			LL_y = WiFi.LL_y;
			UR_x = WiFi.UR_x;
			UR_y = WiFi.UR_y;
		}
		else {
			LL_x = WiFi.opp_LL_x;
			LL_y = WiFi.opp_LL_y;
			UR_x = WiFi.opp_UR_x;
			UR_y = WiFi.opp_UR_y;
		}
		//The island can either be completely above, below, right of, or left of the starting island, but never a combination of these.
		//The relative position of islands also indicates whether the bridge is vertical or horizontal.
		if(Math.max(LL_y, UR_y) <= Math.min(WiFi.Island_LL_y, WiFi.Island_UR_y))	//island above starting island?
			return 1;
		else if(Math.min(LL_y, UR_y) >= Math.max(WiFi.Island_LL_y, WiFi.Island_UR_y))	//island below starting island?
			return 2;
		else if(Math.max(LL_x, UR_x) <= Math.min(WiFi.Island_LL_x, WiFi.Island_UR_x))	//island right of starting island?
			return 3;
		else
			return 4;
	}
	
	
	/**
	 * navigates to the tunnel such that the robot ends up facing into the tunnel 0.4 tiles from the tunnel entrance
	 * @param tunnelOrientation 1 = vertical & LL on starting island, 2 = vertical & LL on island, 
	 * 3 = horizontal & LL on starting island, 4 = horizontal && LL on island
	 */
	public void navToTunnel(int tunnelOrientation) {
		switch(tunnelOrientation) {
		case 1:	//below vertical bridge
			System.out.println("Below Bridge Vertical");
			Navigation.taxiTravelTo((WiFi.TunLL_x + 0.5) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y - 1) * RingChallenge.TILE_SIZE, false, true);
			Navigation.travelTo((WiFi.TunLL_x + 0.5) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y - 0.4) * RingChallenge.TILE_SIZE, true);
			break;
		case 2:	//above vertical bridge
			System.out.println("Above Bridge Vertical");
			Navigation.taxiTravelTo((WiFi.TunLL_x + 0.5) * RingChallenge.TILE_SIZE,
					(WiFi.TunUR_y + 1) * RingChallenge.TILE_SIZE, false, true);
			Navigation.travelTo((WiFi.TunLL_x + 0.5) * RingChallenge.TILE_SIZE,
					(WiFi.TunUR_y + 0.4) * RingChallenge.TILE_SIZE, true);
			break;
		case 3:	//left of horizontal bridge
			System.out.println("Left Bridge Horizontal");
			Navigation.taxiTravelTo((WiFi.TunLL_x - 1) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y + 0.5) * RingChallenge.TILE_SIZE, true, true);
			Navigation.travelTo((WiFi.TunLL_x - 0.4) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y + 0.5) * RingChallenge.TILE_SIZE, true);
			break;
		case 4:	//right of horizontal bridge
			System.out.println("Right Bridge Horizontal");
			Navigation.taxiTravelTo((WiFi.TunUR_x + 1) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y + 0.5) * RingChallenge.TILE_SIZE, true, true);
			Navigation.travelTo((WiFi.TunUR_x + 0.4) * RingChallenge.TILE_SIZE,
				(WiFi.TunLL_y + 0.5) * RingChallenge.TILE_SIZE, true);
			break;
		}
	}
	
	
	/**
	 * Navigates the robot from 0.4 tiles from the entrance to the tunnel to 0.6 tiles out of the exit of the tunnel,
	 * then to the one of the two closest grid intersections (favouring the one that does not have an obstacle)
	 * @param tunnelOrientation 1 = vertical & LL on starting island, 2 = vertical & LL on island, 
	 * 3 = horizontal & LL on starting island, 4 = horizontal && LL on island
	 * @param obstacles list of coordinates that the robot cannot travel on
	 */
	public static void navInTunnelToIsland(int tunnelOrientation, int[][] obstacles) {
		switch(tunnelOrientation) {
		case 1:
			Navigation.travelTo((WiFi.TunLL_x + 0.5) * RingChallenge.TILE_SIZE,
					(WiFi.TunUR_y + 0.6) * RingChallenge.TILE_SIZE, false);
			if(!hasObstacle(WiFi.TunLL_x, WiFi.TunUR_y + 1, obstacles))
				Navigation.travelTo(WiFi.TunLL_x, WiFi.TunUR_y + 1, false);
			else
				Navigation.travelTo(WiFi.TunLL_x + 1, WiFi.TunUR_y + 1, false);
			break;
		case 2:
			Navigation.travelTo((WiFi.TunLL_x + 0.5) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y - 0.6) * RingChallenge.TILE_SIZE, false);
			if(!hasObstacle(WiFi.TunLL_x, WiFi.TunLL_y - 1, obstacles))
				Navigation.travelTo(WiFi.TunLL_x, WiFi.TunLL_y - 1, false);
			else
				Navigation.travelTo(WiFi.TunLL_x + 1, WiFi.TunLL_y - 1, false);
			break;
		case 3:
			Navigation.travelTo((WiFi.TunUR_x + 0.6) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y + 0.5) * RingChallenge.TILE_SIZE, false);
			if(!hasObstacle(WiFi.TunUR_x + 1, WiFi.TunLL_y, obstacles))
				Navigation.travelTo(WiFi.TunUR_x + 1, WiFi.TunLL_y, false);
			else
				Navigation.travelTo(WiFi.TunUR_x + 1, WiFi.TunLL_y + 1, false);
			break;
		case 4:
			Navigation.travelTo((WiFi.TunLL_x - 0.6) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y + 0.5) * RingChallenge.TILE_SIZE, false);
			if(!hasObstacle(WiFi.TunLL_x - 1, WiFi.TunLL_y, obstacles))
				Navigation.travelTo(WiFi.TunLL_x - 1, WiFi.TunLL_y, false);
			else
				Navigation.travelTo(WiFi.TunLL_x - 1, WiFi.TunLL_y + 1, false);
			break;
		}
	}
	
	
	/**
	 * returns an 2d array with all of the obstacle coordinates: {tunnel1, tunnel2, opp_tunnel1, opp_tunnel2, tree, opp_tree}
	 * where each element is a coordinate pair {x, y}
	 * @return list of obstacle coordinates
	 */
	public static int[][] getIslandObstacles(){
		int tunnelOrientation = getTunnelOrientation(true);
		int opp_tunnelOrientation = getTunnelOrientation(false);
		int[][] obstacles = new int[6][2];
		switch(tunnelOrientation) {
		case 1:
			obstacles[0][0] = WiFi.TunUR_x;
			obstacles[0][1] = WiFi.TunUR_y;
			obstacles[1][0] = WiFi.TunLL_x;
			obstacles[1][1] = WiFi.TunUR_y;
			break;
		case 2:
			obstacles[0][0] = WiFi.TunUR_x;
			obstacles[0][1] = WiFi.TunLL_y;
			obstacles[1][0] = WiFi.TunLL_x;
			obstacles[1][1] = WiFi.TunLL_y;
			break;
		case 3:
			obstacles[0][0] = WiFi.TunUR_x;
			obstacles[0][1] = WiFi.TunUR_y;
			obstacles[1][0] = WiFi.TunUR_x;
			obstacles[1][1] = WiFi.TunLL_y;
			break;
		case 4:
			obstacles[0][0] = WiFi.TunLL_x;
			obstacles[0][1] = WiFi.TunUR_y;
			obstacles[1][0] = WiFi.TunLL_x;
			obstacles[1][1] = WiFi.TunLL_y;
			break;
		}
		switch(opp_tunnelOrientation) {
		case 1:
			obstacles[2][0] = WiFi.opp_TunUR_x;
			obstacles[2][1] = WiFi.opp_TunUR_y;
			obstacles[3][0] = WiFi.opp_TunLL_x;
			obstacles[3][1] = WiFi.opp_TunUR_y;
			break;
		case 2:
			obstacles[2][0] = WiFi.opp_TunUR_x;
			obstacles[2][1] = WiFi.opp_TunLL_y;
			obstacles[3][0] = WiFi.opp_TunLL_x;
			obstacles[3][1] = WiFi.opp_TunLL_y;
			break;
		case 3:
			obstacles[2][0] = WiFi.opp_TunUR_x;
			obstacles[2][1] = WiFi.opp_TunUR_y;
			obstacles[3][0] = WiFi.opp_TunUR_x;
			obstacles[3][1] = WiFi.opp_TunLL_y;
			break;
		case 4:
			obstacles[2][0] = WiFi.opp_TunLL_x;
			obstacles[2][1] = WiFi.opp_TunUR_y;
			obstacles[3][0] = WiFi.opp_TunLL_x;
			obstacles[3][1] = WiFi.opp_TunLL_y;
			break;
		}
		obstacles[4][0] = WiFi.Tr_x;
		obstacles[4][1] = WiFi.Tr_y;
		obstacles[5][0] = WiFi.opp_Tr_x;
		obstacles[5][1] = WiFi.opp_Tr_y;
		
		return obstacles;
	}
	
	/**
	 * return whether a point contains an obstacle or is not within the main island
	 * @param x x position in grid tiles
	 * @param y y position in grid tiles
	 * @param obstacles list of coordinates the robot cannot travel on
	 * @return if the coordinate has an obstacle or is out of bounds
	 */
	private static boolean hasObstacle(int x, int y, int[][] obstacles) {
		for(int[] obstacle : obstacles) {
			if(x == obstacle[0] && y == obstacle[1])
				return true;
		}
		if(x < WiFi.Island_UR_x && x > WiFi.Island_LL_x && y < WiFi.Island_UR_y && y > WiFi.Island_LL_y)
			return false;
		else
			return true;
	}
	
	
	/**
	 * Returns a point by point path along the grid to get from one point to another on the main island. This method assumes that the end point is
	 * reachable, i.e. the path to it or the end point itself are not completely obstructed by obstacles; checking for this is
	 * a responsibility of the caller
	 * @param x1 starting x coordinate
	 * @param y1 starting y coordinate
	 * @param x2 destination x coordinate
	 * @param y2 destination y coordinate
	 * @param obstacles list of coordinates which the robot cannot travel on
	 * @param visited This is a 2d boolean array which indicates which parts of the island have already been visited. The array must be
	 * (island width - 1) x (island height - 1) in size. If no array has been made, the first call generates an empty one automatically.
	 * @return list of adjacent coordinates that form the shortest obstacle free path
	 */
	public static ArrayList<int[]> getShortestPath(int x1, int y1, int x2, int y2, int[][] obstacles, boolean[][] visited) {
		if(x1 == x2 && y1 == y2) {
			return new ArrayList<int[]>();
		}
		
		if(visited == null) {
			visited = new boolean[WiFi.Island_UR_x - WiFi.Island_LL_x - 1][WiFi.Island_UR_y - WiFi.Island_LL_y - 1];
		}
		
		visited[x1 - WiFi.Island_LL_x - 1][y1 - WiFi.Island_LL_y - 1] = true;
		
		int[] xOrder = new int[2];
		if(x2 >= x1) {
			xOrder[0] = 1;
			xOrder[1] = 3;
		}
		else {
			xOrder[0] = 3;
			xOrder[1] = 1;
		}
		
		int[] yOrder = new int[2];
		if(y2 >= y1) {
			yOrder[0] = 0;
			yOrder[1] = 2;
		}
		else {
			yOrder[0] = 2;
			yOrder[1] = 0;
		}
		
		int[] searchOrder = new int[4];
		
		//determine search order: 0 = up, 1 = right, 2 = down, 3 = left
		if(Math.abs(x2 - x1) >= Math.abs(y2 - y1)) {	//x has more to go than y
			searchOrder[0] = xOrder[0];
			searchOrder[1] = yOrder[0];
			searchOrder[2] = yOrder[1];
			searchOrder[3] = xOrder[1];
		}
		else {	//y more to go than x
			searchOrder[0] = yOrder[0];
			searchOrder[1] = xOrder[0];
			searchOrder[2] = xOrder[1];
			searchOrder[3] = yOrder[1];
		}
		
		int[] next = new int[2];
		
		for(int i = 0; i < searchOrder.length; i++) {
			
			switch(searchOrder[i]) {
			case 0:	//up
				next[0] = x1;
				next[1] = y1 + 1;
				break;
			case 1:	//right
				next[0] = x1 + 1;
				next[1] = y1;
				break;
			case 2:	//down
				next[0] = x1;
				next[1] = y1 - 1;
				break;
			case 3:	//left
				next[0] = x1 - 1;
				next[1] = y1;
				break;
			}

			if(!hasObstacle(next[0], next[1], obstacles)) {
				if(visited[next[0] - WiFi.Island_LL_x - 1][next[1] - WiFi.Island_LL_y - 1])
					continue;
				ArrayList<int[]> path = getShortestPath(next[0], next[1], x2, y2, obstacles, visited);
				if(path == null)
					continue;	//This move results in no possible paths, so try next move
				else {
					path.add(0, next);	//move results in a complete path
					return path;
				}
			}
			else {
				continue;	//For this move, an obstacle is in the way, so try next move
			}
		}
		visited[x1 - WiFi.Island_LL_x - 1][y1 - WiFi.Island_LL_y - 1] = false;	//no possible options from here, so take a step back
		return null;
	}
	

	/**
	 * This method navigates the robot to the closest available side on the tree (1 grid away).
	 * @param startCoords where the robot is when the method is called
	 * @param obstacles list of coordinates which the robot cannot travel on
	 * @param availableSides list of side of the tree (1 = north, 2 = east, 3 = south, 4 = west) that are accessible
	 * @return a boolean indicating if the robot made it to the tree immediately after exiting the tunnel, meaning localization is
	 * required to get it back on the grid
	 */
	public static boolean tunnelToTree(int[] startCoords, int[][] obstacles, ArrayList<Integer> availableSides) {
		ArrayList<int[]> path = new ArrayList<int[]>();
		
		for(int i = 0; i < availableSides.size(); i++) {
			int closestSide = RingSearcher.getClosestSide(availableSides);
			int[] endCoords = RingSearcher.getSideCoordinates(closestSide);
			path = getShortestPath(startCoords[0], startCoords[1], endCoords[0], endCoords[1], obstacles, null);
			if(path != null)
				break;
			availableSides.remove(availableSides.indexOf(closestSide));
			if(i == availableSides.size() - 1)	//tree inaccessible
				return true;
		}
		
		for(int[] waypoint : path)
			Navigation.travelTo(waypoint[0], waypoint[1], true);
		
		return path.size() == 0;
	}
	
	/**
	 * Navigates to the nearest accessible tree side. 
	 * @param obstacles list of coordinates which the robot cannot travel on
	 * @param availableSides list of side of the tree (1 = north, 2 = east, 3 = south, 4 = west) that are accessible
	 * @return side that the tree navigated to, and -1 if no side was available
	 */
	public int navAroundTree(int[][] obstacles, ArrayList<Integer> availableSides) {
		double[] XYT = odometer.getXYT();
		int[] startCoords = Navigation.getClosestCoordinates(XYT[0], XYT[1]);
		
		ArrayList<int[]> path = new ArrayList<int[]>();
		
		int closestSide = -1;
		
		for(int i = 0; i < availableSides.size(); i++) {
			closestSide = RingSearcher.getClosestSide(availableSides);
			int[] endCoords = RingSearcher.getSideCoordinates(closestSide);
			path = getShortestPath(startCoords[0], startCoords[1], endCoords[0], endCoords[1], obstacles, null);
			if(path != null)
				break;
			if(i == availableSides.size() - 1)	//tree inaccessible
				return -1;
		}
		if(closestSide == -1)	//availableSides is empty
			return closestSide;
		
		for(int[] waypoint : path)
			Navigation.travelTo(waypoint[0], waypoint[1], true);
		
		availableSides.remove(availableSides.indexOf(closestSide));
		return closestSide;
	}

	/**
	 * Navigates from the tree to the tunnel.
	 * @param destination grid coordinates of the robot after executing navInTunnelToIsland
	 * @param obstacles list of coordinates which the robot cannot travel on
	 */
	public static void treeToTunnel(int[] destination, int[][] obstacles) {
		int[] startCoords = Navigation.getClosestCoordinates(odometer.getXYT()[0], odometer.getXYT()[1]);
		ArrayList<int[]> path = getShortestPath(startCoords[0], startCoords[1], destination[0], destination[1], obstacles, null);
		for(int[] waypoint : path)
			Navigation.travelTo(waypoint[0], waypoint[1], true);
	}
	
	/**
	 * Navigates from a grid intersection near the entrance of the tunnel to the front of the tunnel, then to the main island
	 * through the tunnel, ending up 1 grid away from the exit
	 * @param tunnelOrientation 1 = vertical & LL on starting island, 2 = vertical & LL on island, 
	 * 3 = horizontal & LL on starting island, 4 = horizontal && LL on island
	 * @param obstacles list of coordinates which the robot cannot travel on
	 */
	public static void navInTunnelFromIsland(int tunnelOrientation, int[][] obstacles) {
		switch(tunnelOrientation) {
		case 1:
			Navigation.travelTo((WiFi.TunLL_x + 0.5) * RingChallenge.TILE_SIZE,
					(WiFi.TunUR_y + 1) * RingChallenge.TILE_SIZE, true);
			Navigation.travelTo((WiFi.TunLL_x + 0.5) * RingChallenge.TILE_SIZE,
					(WiFi.TunUR_y + 0.4) * RingChallenge.TILE_SIZE, true);
			Navigation.travelTo((WiFi.TunLL_x + 0.5) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y - 1) * RingChallenge.TILE_SIZE, false);
			break;
		case 2:
			Navigation.travelTo((WiFi.TunLL_x + 0.5) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y - 1) * RingChallenge.TILE_SIZE, true);
			Navigation.travelTo((WiFi.TunLL_x + 0.5) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y - 0.4) * RingChallenge.TILE_SIZE, true);
			Navigation.travelTo((WiFi.TunLL_x + 0.5) * RingChallenge.TILE_SIZE,
					(WiFi.TunUR_y + 1) * RingChallenge.TILE_SIZE, false);
			break;
		case 3:
			Navigation.travelTo((WiFi.TunUR_x + 1) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y + 0.5) * RingChallenge.TILE_SIZE, true);
			Navigation.travelTo((WiFi.TunUR_x + 0.4) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y + 0.5) * RingChallenge.TILE_SIZE, true);
			Navigation.travelTo((WiFi.TunLL_x - 1) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y + 0.5) * RingChallenge.TILE_SIZE, false);
			break;
		case 4:
			Navigation.travelTo((WiFi.TunLL_x - 1) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y + 0.5) * RingChallenge.TILE_SIZE, true);
			Navigation.travelTo((WiFi.TunLL_x - 0.4) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y + 0.5) * RingChallenge.TILE_SIZE, true);
			Navigation.travelTo((WiFi.TunUR_x + 1) * RingChallenge.TILE_SIZE,
					(WiFi.TunLL_y + 0.5) * RingChallenge.TILE_SIZE, false);
			break;
		}
	}

	/**
	 * Goes back to the start and then unloads any rings it is carrying; assumes that it is completely out of the tunnel and on the
	 * starting island
	 */
	public static void tunnelToStart() {
		Navigation.taxiTravelTo(WiFi.localizeX, WiFi.localizeY, true, true);
		switch(WiFi.corner) {
		case 0:
			Navigation.turnTo(225);
			break;
		case 1:
			Navigation.turnTo(135);
			break;
		case 2:
			Navigation.turnTo(45);
			break;
		case 3:
			Navigation.turnTo(315);
		}
	}
	
	/* This method celebrates the robots successful run */
	
	/**
	 * Plays a song
	 */
	@SuppressWarnings("unused")
	public static void weirdFlexButOk() {
		
		int c =261;
		int d =294;
		int e =329;
		int f =349;
		int g =391;
		int gS =415;
		int a =440;
		int aS =455;
		int b =466;
		int cH =523;
		int cSH =554;
		int dH =587;
		int dSH =622;
		int eH =659;
		int fH =698;
		int fSH =740;
		int gH =784;
		int gSH =830;
		int aH =880;
		
		while (true) {
			Sound.playTone(a, 500);
		    Sound.playTone(a, 500);
		    Sound.playTone(a, 500);
		    Sound.playTone(f, 350);
		    Sound.playTone(cH, 150);
		    Sound.playTone(a, 500);
		    Sound.playTone(f, 350);
		    Sound.playTone(cH, 150);
		    Sound.playTone(a, 650);
		 
		    Sound.playTone(100000, 150);
		    //end of first bit
		 
		    Sound.playTone(eH, 500);
		    Sound.playTone(eH, 500);
		    Sound.playTone(eH, 500);
		    Sound.playTone(fH, 350);
		    Sound.playTone(cH, 150);
		    Sound.playTone(gS, 500);
		    Sound.playTone(f, 350);
		    Sound.playTone(cH, 150);
		    Sound.playTone(a, 650);
		 
		    Sound.playTone(100000, 150);
		    //end of second bit...
		 
		    Sound.playTone(aH, 500);
		    Sound.playTone(a, 300);
		    Sound.playTone(a, 150);
		    Sound.playTone(aH, 400);
		    Sound.playTone(gSH, 200);
		    Sound.playTone(gH, 200);
		    Sound.playTone(fSH, 125);
		    Sound.playTone(fH, 125);
		    Sound.playTone(fSH, 250);
		 
		    Sound.playTone(1000000, 250);
		 
		    Sound.playTone(aS, 250);
		    Sound.playTone(dSH, 400);
		    Sound.playTone(dH, 200);
		    Sound.playTone(cSH, 200);
		    Sound.playTone(cH, 125);
		    Sound.playTone(b, 125);
		    Sound.playTone(cH, 250);
		 
		    Sound.playTone(10000000, 250);
		 
		    Sound.playTone(f, 125);
		    Sound.playTone(gS, 500);
		    Sound.playTone(f, 375);
		    Sound.playTone(a, 125);
		    Sound.playTone(cH, 500);
		    Sound.playTone(a, 375);
		    Sound.playTone(cH, 125);
		    Sound.playTone(eH, 650);
		 
		    //end of third bit... (Though it doesn't play well)
		    //let's repeat it
		 
		    Sound.playTone(aH, 500);
		    Sound.playTone(a, 300);
		    Sound.playTone(a, 150);
		    Sound.playTone(aH, 400);
		    Sound.playTone(gSH, 200);
		    Sound.playTone(gH, 200);
		    Sound.playTone(fSH, 125);
		    Sound.playTone(fH, 125);
		    Sound.playTone(fSH, 250);
		 
		    Sound.playTone(10000000, 250);
		 
		    Sound.playTone(aS, 250);
		    Sound.playTone(dSH, 400);
		    Sound.playTone(dH, 200);
		    Sound.playTone(cSH, 200);
		    Sound.playTone(cH, 125);
		    Sound.playTone(b, 125);
		    Sound.playTone(cH, 250);
		 
		    Sound.playTone(100000000, 250);
		 
		    Sound.playTone(f, 250);
		    Sound.playTone(gS, 500);
		    Sound.playTone(f, 375);
		    Sound.playTone(cH, 125);
		    Sound.playTone(a, 500);
		    Sound.playTone(f, 375);
		    Sound.playTone(cH, 125);
		    Sound.playTone(a, 650);
		    //end of the song
		    
		    Sound.playTone(10000, 2000);
		}
	}
	
	
	
}