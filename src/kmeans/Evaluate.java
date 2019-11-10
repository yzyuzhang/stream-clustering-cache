package kmeans;
import java.io.File;
import java.util.List;
import java.util.Scanner;

import datastructure.Center;
import datastructure.Point;

public class Evaluate {
	
	/**
	 * compute kmeans cost (within cluster sum of squares by cluster)
	 * @param points 
	 * @param centers cluster centers
	 * @return kmeans cost
	 */
	public static double kmeansCost(List<Point> points, List<Center> centers) {
        double cost = 0;
        for (Point p : points) {
        	// compute distance to the nearest center
        	double minDist = p.getNearestDistance(centers);
        	cost += minDist * minDist * p.weight;
        }
        return cost;
    } 

	
	/**
	 * compute the kmeans cost, read file from the beginning
	 * this method is for the very large data when data can not 
	 * fit in the memory
	 * @param clusters
	 * @param fileName
	 * @param numOfPoints number of points to read
	 * @return
	 * @throws Exception
	 */
	public static double evaluate(List<Center> clusters, String fileName, int numOfPoints) throws Exception {
		// number of points received
		int numOfPointsRead = 0;
		// k-means cost
		double cost = 0.0;
		Scanner scanner = new Scanner(new File(fileName));
		
		while (scanner.hasNextLine() && numOfPointsRead < numOfPoints) {
			String str = scanner.nextLine();
			String[] strs = str.split(",");
			double[] pos = new double[strs.length];
			for (int i=0; i<strs.length; i++) {
				pos[i] = Double.parseDouble(strs[i]);
			}
			Point p = new Point(pos, 1);
			
            double minDist = p.getNearestDistance(clusters);
            cost += minDist *  minDist;
            numOfPointsRead++;
        }
		scanner.close();
        return cost;
	}
}
