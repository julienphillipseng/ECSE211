package ca.mcgill.ecse211.sensor;

import java.util.ArrayList;

/**
 * This class takes a stream of light sensor values and determines when a light sensors detects a line
 * 
 * @author Matthew Kourlas
 */
public class LineDetector {
	
	private int window_size;	//must be large enough to contain the entire drop in intensity when crossing a line
	private int threshold;	//magnitude in drop in intensity which indicates a detected line
	private ArrayList<Integer> window = new ArrayList<Integer>();
	
	/**
	 * constructor for the MedianFilter class
	 * @param window_size range of values to be considered
	 */
	public LineDetector(int window_size, int threshold) {
		this.window_size = window_size;
		this.threshold = threshold;
	}
	
	
	/**
	 * This method takes the next value in a series of intensity values and returns if the magnitude of the maximum drop
	 * in intensity is equal to or greater than threshold
	 * @param nextValue next value read by light sensor
	 * @return whether a line was detected
	 */
	public boolean lineCrossed(int nextValue) {
		//shift window
		window.add(0, nextValue);
		if(window.size() > window_size)
			window.remove(window.size() - 1);
		
		//find minimum
		int minVal = window.get(0);
		int minIndex = 0;
		for(int i = 1; i < window.size(); i++) {
			if(window.get(i) < minVal) {
				minVal = window.get(i);
				minIndex = i;
			}
		}
		
		//find maximum left of minimum
		int maxVal = window.get(0);
		for(int i = 1; i < minIndex; i++) {
			if(window.get(i) > maxVal)
				maxVal = window.get(i);
		}
		
		//return if maximum drop is greater than or equal to threshold
		return maxVal - minVal >= threshold;
	}
}
