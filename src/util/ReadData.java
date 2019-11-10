package util;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import datastructure.Point;


public class ReadData {
	
	Scanner scanner;
	
	public ReadData(String fileName) throws FileNotFoundException {
		scanner = new Scanner(new File(fileName));
	}
	
	/** 
	 * read next data point
	 * @return new data point
	 * @throws FileNotFoundException
	 */
	public Point nextPoint() throws FileNotFoundException {
		String str = scanner.nextLine();
		String[] strs = str.split(",");
		double[] pos = new double[strs.length];
		for (int i=0; i<strs.length; i++) {
			pos[i] = Double.parseDouble(strs[i]);
		}
		
		// weight is 1
		Point p = new Point(pos, 1);
		return p;
	}
	
	/**
	 * decide if there is more data points to read
	 * @return
	 */
	public boolean hasNextLine() {
		if (scanner.hasNextLine()) {
			return true;
		}
		else {
			scanner.close();
			return false;
		}
	}

}
