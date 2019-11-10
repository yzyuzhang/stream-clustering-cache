
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import datastructure.Center;
import datastructure.Point;
import kmeans.Evaluate;
import kmeans.KMeansPlusPlus;
import util.ReadData;

public class KMeansDriver {
	
	public static final int MAX_ITERATIONS = 30;
	
	public static final int QUERY_TRIALS = 5; 
	
	public static final String DATA_NAME = "synthetic";

	public static void main(String[] args) throws Exception {
		
		int[] k = new int[]{10, 20, 30, 40, 50}; 
		
		String fileName = "E:/dataset/clean/" + DATA_NAME + ".txt";
		
		// read the points as input data stream
		ReadData readData = new ReadData(fileName);
		List<Point> recvPoints = new ArrayList<Point>();   // received points
		while (readData.hasNextLine()) {
			// read each point
			Point p = readData.nextPoint();  
			recvPoints.add(p);
		}

		for (int i=0; i<k.length; i++) {
			// File to write the result
			String prefix = DATA_NAME + "/" + "k-" + k[i] + "/kmpp/";
			File dir = new File(prefix);
			if (!dir.exists()) {
				dir.mkdirs();
			}		
			FileWriter fwAccuracy = new FileWriter(prefix + "accuracy.txt");
			
			List<Center> centers = KMeansPlusPlus.multiKMeansPlusPlus(recvPoints, k[i], MAX_ITERATIONS, QUERY_TRIALS);
			double kmeansCost = Evaluate.kmeansCost(recvPoints, centers);
			fwAccuracy.write(kmeansCost + "\n");
			fwAccuracy.close();
		}
	}
	
	
}
