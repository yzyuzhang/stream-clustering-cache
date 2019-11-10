package algo;
import java.util.ArrayList;
import java.util.List;

import datastructure.Bucket;
import datastructure.Center;
import datastructure.Point;
import kmeans.KMeansPlusPlus;

public class CoresetTree implements CluMethod {

	// number of clusters wanted
	private final int k;
	
	// bucket size
	private final int m;

	// merge threshold
	private final int r;
	
	// number of buckets received in the tree
	private int numOfBuckets;
	
	// number of max iterations for multi-kmeans++ (query process)
    private final int maxIter;
	
	// number of trials for multi-kmeans++ (query process)
	private final int trials;
	
	// coreset tree: each level of the tree is a list of buckets 
	private List<List<Bucket>> coresetTree;

	// initial bucket: size can be 0 to m-1 (both inclusive)
	private Bucket bucket_0;
	

	public CoresetTree(int k, int bucketSize, int mergeThreshold, int maxIterations, int queryTrials) {
		this.k = k;
		this.m = bucketSize;
		this.r = mergeThreshold;
		
		this.maxIter = maxIterations;
		this.trials = queryTrials;
		this.numOfBuckets = 0;
		bucket_0 = new Bucket(m);
		this.coresetTree = new ArrayList<>();
	}
	
	/**
	 * Retrieve the number of buckets received
	 * @return
	 */
	public int getNumOfBuckets() {
		return numOfBuckets;
	}
	 
	/**
	 * Retrieve bucket_0
	 * @return
	 */
	public Bucket getBucket0() {
		return bucket_0;
	}
	
	/**
	 * Retrieve the coreset tree
	 * @return 
	 */
	public List<List<Bucket>> getCoresetTree() {
		return coresetTree;
	}
	
	/**
	 * clustering method of Coreset Tree (for case r=2, it is StreamKM++)
	 * @param p
	 */
	public void cluster(Point p) {
		mergeReduce(p);
	}
	
	/**
	 * Runs the merge-reduce clustering algorithm. 
	 * It is like incrementing "one" to a number.
	 * @param p
	 */
	private void mergeReduce(Point p) {
		// add new point to the bucket 0
		bucket_0.addPoint(p);
		// when bucket 0 is full, update the coreset tree
		if (bucket_0.coresetSize() == m) {
			// a new bucket received
			numOfBuckets++;
			
			// carry digit
			Bucket bucketCarry = bucket_0;
			// empty bucket 0
			bucket_0 = new Bucket(m);
			
			for (int i=0; i<coresetTree.size(); i++) {
				List<Bucket> currentLevel = coresetTree.get(i);
				// number of buckets at level i is less than (r-1),
				// then no need to increment additionally
				if (currentLevel.size() < (r - 1)) {
					currentLevel.add(bucketCarry);
					return;
				}
				bucketCarry = bucketCarry.mergeBuckets(currentLevel);
				// empty this level (list of buckets)
				currentLevel.clear();
			}
			List<Bucket> nextLevel = new ArrayList<>();
			nextLevel.add(bucketCarry);
			coresetTree.add(nextLevel);
		}
	}
	
	/**
	 * Compute k centers from the coreset tree
	 * @return list of k points as cluster centers
	 */
	public List<Center> getCenters() {
		List<Point> coresets = getCoresets();
		
		// add coreset in bucket 0
		coresets.addAll(bucket_0.coreset);
		
		// run kmeans++ multiple times to get the best k centers
		return KMeansPlusPlus.multiKMeansPlusPlus(coresets, k, maxIter, trials);
	}
	
	/**
	 * Retrieve all the coresets from the coreset tree
	 * @return
	 */
	public List<Point> getCoresets() {
		List<Point> unionCoresets = new ArrayList<Point>();
		for (List<Bucket> level : coresetTree) {
			for (Bucket b : level) {
				unionCoresets.addAll(b.coreset);
			}
		}
		return unionCoresets;
	}
	
	/**
     * Compute the memory cost in words, each weighted point is one word
     * @return number of weighted points in the memory that maintained
     */
    public long computeMemory() {
    	int numOfBuckets = 0;
		for (List<Bucket> level : coresetTree) {
			numOfBuckets += level.size();
		}
		// each bucket contains m points
		return numOfBuckets * m + bucket_0.coresetSize();
    }
    
}

