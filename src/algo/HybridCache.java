package algo;

import java.util.ArrayList;
import java.util.List;

import datastructure.Center;
import datastructure.Point;
import kmeans.Evaluate;
import kmeans.KMeansPlusPlus;


public class HybridCache implements CluMethod {

    private final int k;  // number of clusters

    private final int d;  // dimension
    
    private int numOfPoints;  // number of points received
    
    private int initThreshold;
    
    private CoresetCache cacheModel;
   
    private List<Center> centers;
    
    private List<Point> initPoints;
    
    private double cost_0;   // last fall back to cache
    
    private double estCost;  // estimated current kmeans cost
     
    private double threshold;
    
    private int maxIterations;
    
    private int numTrials;
    
    
    public HybridCache(int k, int d, int bucketSize, int r, double threshold, int maxIterations, int numTrials) {
        this.k = k;
        this.d = d;
        this.numOfPoints = 0;
        this.threshold = threshold;
        this.maxIterations = maxIterations;
        this.numTrials = numTrials;
        
        initThreshold = 10 * k;
        initPoints = new ArrayList<Point>();
        this.cacheModel = new CoresetCache(k, bucketSize, r, maxIterations, numTrials);
    }  
    
    
    /**
     * Compute the memory cost in words, each weighted point is one word
     */
    public long computeMemory() {
    	return k + cacheModel.computeMemory();
    }
    
    
    @Override
	public void cluster(Point p) {
    	
    	numOfPoints++;
    	
    	// send point p to caching method
    	cacheModel.cluster(new Point(p));
    			
		// collect first k points as initial centers
		if (numOfPoints <= initThreshold) {
			initKCenters(p);
			return;
		}
		
		// assign point p to the nearest center
		Point nearestCenter = centers.get(p.getNearestCluster(centers));

		// update estCost
		double dist = p.euclidDistTo(nearestCenter);
		estCost += dist * dist * p.weight;

		// update nearestCenter position
		double[] prevPos = nearestCenter.position;
		double prevWeight = nearestCenter.weight;
		double updatedWeight = prevWeight + p.weight;

		for (int i = 0; i < d; i++) {
			prevPos[i] = (prevPos[i] * prevWeight + p.position[i] * p.weight) / updatedWeight;
		}

		// update nearestCenter weight
		nearestCenter.weight = updatedWeight;
	}

    
    /**
     * Generate initial k centers by clustering first 100k points
     *
     * @return the initial centers
     */
    private void initKCenters(Point p) {
    	if (numOfPoints < initThreshold) {
    		initPoints.add(p);
    	}
    	else {
    		centers = KMeansPlusPlus.multiKMeansPlusPlus(initPoints, k, maxIterations, numTrials);
			cost_0 = Evaluate.kmeansCost(initPoints, centers);
			estCost = cost_0;
    	}
    }
    
    
	@Override
	public List<Center> getCenters() {
		// switch the cluster centers to the cache method
		// when the current cost is above threshold
		if (estCost > threshold * cost_0) {
			// get the coreset cache
			List<Point> coreset = cacheModel.getCoresets();
			
			// run the query method of coreset cache, get k centers
			centers = cacheModel.getCenters();
			
			// estimate the current k-means cost using new cluster centers
			cost_0 = Evaluate.kmeansCost(coreset, centers);
			estCost = cost_0;
		}

		return centers;
	}

}
