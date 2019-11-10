import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import algo.CluMethod;
import algo.CoresetCache;
import algo.CoresetTree;
import algo.FirstKSeq;
import algo.HybridCache;
import algo.ThreeRecursiveCache;
import algo.TwoRecursiveCache;
import datastructure.Center;
import datastructure.Point;
import kmeans.Evaluate;
import util.Poisson;
import util.ReadData;

public class Main {
	
	public static final int MAX_ITERATIONS = 20;
	
	public static final int QUERY_TRIALS = 1; 
	
	public static final int EVAL_TIMES = 6;
	
	public static final String DATA_NAME = "synthetic";
	
	public static final String FILE_NAME = "/Users/yu/Documents/clustering data/dataset/clean/" + DATA_NAME + ".txt";
	
	/**
	 * Main Program
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
//		int lowK = 20;
//		
//		int highK = 30;
//		
//		int kStep = 5;
//		
//		// query after every queryInterval
//		int[] queryInterval = new int[]{100,500,1000,5000};
//		
//		// 200: 200 * k (num of centers)
//		int[] bucketSizes = new int[]{200,100,50,30,10,5,1};
//
//		for (int k = lowK; k <= highK; k += kStep) {
//			for (int i = 0; i < 1; i++) {
//			}
//		}

		int k = 30;
		// int[] k = new int[]{10, 20, 30, 40, 50};   // number of clusters, default 30
		
		// parameters of dataset
		// power:     d: 7  n:2049280
		// covtype:   d:54  n:581012  
		// intrusion: d:34  n:494021
		// synthetic: d:68  n:200000 
		int d = 54;   // dimensions
		int n = 581012; // total number of points
		
		if (DATA_NAME.equals("power")) {
			d = 7;	n = 2049280;
		}
		else if (DATA_NAME.equals("intrusion")) {
			d = 34;	n = 494021;
		}
		else if (DATA_NAME.equals("synthetic")) {
			d = 68;	n = 200000;
		}
		
		// double[] hybrid_thresholds = new double[]{1.2, 2.4, 3.6, 4.8, 6.0, 7.2};
		// double hybrid_threshold = 1.2;
		
		// int bucketSize = 20 * k;   // bucket size
		int[] bucketSizeRatio = new int[]{80, 100};
		
		int queryInterval = 100;  // query cluster centers every queryInterval points, default 100
		// int[] queryInterval = new int[]{50, 100, 200, 400, 800, 1600, 3200};
		
		// int r = 2; // merge threshold
		
		// posson process rate (lambda)
		// double[] lambdas = new double[]{50, 100, 200, 400, 800, 1600, 3200};
//		for (int i=0; i<lambdas.length; i++) {
//			lambdas[i] = 1.0 / lambdas[i];
//		}
		
		// firstseq, skmpp, cache, (tworcc), rcc, hybrid_12
		for (int i=0; i<bucketSizeRatio.length; i++) {
			// run(k, d, n, bucketSize, 2, queryInterval, FILE_NAME, DATA_NAME, "firstseq");
			int bucketSize = bucketSizeRatio[i] * k;
			run(k, d, n, bucketSize, 2, queryInterval, FILE_NAME, DATA_NAME, "skmpp");
			run(k, d, n, bucketSize, 2, queryInterval, FILE_NAME, DATA_NAME, "cache");
			run(k, d, n, bucketSize, (int)Math.sqrt(n/bucketSize), queryInterval, FILE_NAME, DATA_NAME, "rcc");
			run(k, d, n, bucketSize, 2, queryInterval, FILE_NAME, DATA_NAME, "hybrid_12");
		}
		
	}
	
	
	
	/**
	 * run stream clustering
	 * @param k
	 * @param d
	 * @param queryInterval
	 * @param evalInterval
	 * @param r
	 * @param n
	 * @param bucketSize
	 * @param fileName
	 * @param dataName
	 * @param cluMethod
	 * @throws Exception
	 */
	public static void run(int k, int d, int n, int bucketSize, int r, int queryInterval, String fileName,
			String dataName, String cluMethod) throws Exception {

		/******************* step 1. Initialize parameters  *******************/
		int numOfPoints = 0;   // number of received points so far
		List<Point> recvPoints = new ArrayList<Point>();   // received points
		
		// timing variables
		long start = 0, end = 0;
		double updateTime, queryTime = 0;
		double progress = 0;   // showing current progress
		
		// File to write the result
		// String prefix = dataName + "/" + "k-" + k + "/" + "queryinterval-" + queryInterval + "/" + cluMethod + "/";
		String prefix = dataName + "/" + "bucketsize-" + (bucketSize/k) + "/" + cluMethod + "/";
		// String prefix = dataName + "/" + "poisson-" + lambda + "/" + cluMethod + "/";
		File dir = new File(prefix);
		if (!dir.exists()) {
			dir.mkdirs();
		}		
		FileWriter fwAccuracy = new FileWriter(prefix + "accuracy.txt");
		FileWriter fwUpdate = new FileWriter(prefix + "updatetime.txt");
		FileWriter fwQuery = new FileWriter(prefix + "querytime.txt");
		FileWriter fwMemory = new FileWriter(prefix + "memory.txt");
		
		// read the points as input data stream
		ReadData readData = new ReadData(fileName);
		
		/*******************  step 2. Initialize model  *******************/
		CluMethod model = new FirstKSeq(k, d);
		
		// method 2: stream kmeans++
		if (cluMethod.equals("skmpp")) {
			model = new CoresetTree(k, bucketSize, r, MAX_ITERATIONS, QUERY_TRIALS);
		}
		
		// method 3: coreset cache
		if (cluMethod.equals("cache")) {
			model = new CoresetCache(k, bucketSize, r, MAX_ITERATIONS, QUERY_TRIALS);
		}
		
		// inner tree with cache
		if (cluMethod.equals("tworcc")) {
			model = new TwoRecursiveCache(k, bucketSize, r, MAX_ITERATIONS, QUERY_TRIALS);	
		}
		
		
		// method 4: recursive: three tiers (tree + cache)
		if (cluMethod.equals("rcc")) {
			 model = new ThreeRecursiveCache(k, bucketSize, r, MAX_ITERATIONS, QUERY_TRIALS);
		}
		
		// method 5: hybrid (sequential + cache)
		if (cluMethod.equals("hybrid_12")) {
			 model = new HybridCache(k, d, bucketSize, r, 1.2, MAX_ITERATIONS, QUERY_TRIALS);
		}
		
//		// Initialize Poisson Process
//		Poisson poisson = new Poisson(lambda, k);
//		int nextQueryPoint = poisson.nextPoisson();
		
		/*******************  step 3. Update the model (cluster centers)  *******************/
		// for each batch, run the clustering algorithm
		while (readData.hasNextLine()) {
			// read each point
			Point p = readData.nextPoint();  
			recvPoints.add(p);
			numOfPoints++;
			
			// update process: cluster the points
			start = System.nanoTime();
			model.cluster(p);
			end = System.nanoTime();
			updateTime = (end - start) / 1e9;  // in seconds
			fwUpdate.write(updateTime + "\n");
			
			// query by every queryInterval number of points
			if (numOfPoints % queryInterval == 0) {
				// run query method (but not evaluate)
				start = System.nanoTime();
				model.getCenters();
				end = System.nanoTime();
				queryTime = (end - start) / 1e9;
				fwQuery.write(queryTime + "\n");
				
				// record the memory cost
				fwMemory.write(model.computeMemory() + "\n");
			}
			
//			// query by Poisson Process
//			while (numOfPoints == nextQueryPoint) {
//				// run query method (but not evaluate)
//				start = System.nanoTime();
//				model.getCenters();
//				end = System.nanoTime();
//				queryTime = (end - start) / 1e9;
//				fwQuery.write(queryTime + "\n");
//				
//				// record the memory cost
//				fwMemory.write(model.computeMemory() + "\n");
//				
//				// generate next Poisson point
//				nextQueryPoint = poisson.nextPoisson();
//			}
			
//			// Sanity check: error condition
//			if (numOfPoints > nextQueryPoint) {  
//				System.out.println("Error Condition: numOfPoints is larger than nextQueryPoint");
//				while (numOfPoints >= nextQueryPoint) {
//					nextQueryPoint = poisson.nextPoisson();
//				}
//			}
			
			// output progress
			double ratio = (double)numOfPoints / n;
			if (ratio - progress >= 0.01) {
				progress = ratio;
				System.out.println("Progress: " + "k-" + k + " numOfPoints-" + numOfPoints + " progress-" + progress);
			}
		
			// evaluate
			if (numOfPoints % (n / EVAL_TIMES) == 0) {
				// query (compute k cluster centers) and the end of each query interval
				List<Center> centers = model.getCenters();
				
				// double kmeansCost = Evaluate.evaluate(clusterCenters, fileName, numOfPoints); 
				double kmeansCost = Evaluate.kmeansCost(recvPoints, centers);
				System.out.println(kmeansCost);
				fwAccuracy.write(kmeansCost + "\n");
			}
		}
		
		// compute kmeans cost at the end of stream
		List<Center> centers = model.getCenters();
		double kmeansCost = Evaluate.kmeansCost(recvPoints, centers);
		fwAccuracy.write(kmeansCost + "\n");
		
//		List<Center> centers2 = KMeansPlusPlus.multiKMeansPlusPlus(recvPoints, k, 5, 5);
//		System.out.println(Evaluate.kmeansCost(recvPoints, centers2));

		fwAccuracy.close();
		fwUpdate.close();
		fwQuery.close();
		fwMemory.close();
	}

}
