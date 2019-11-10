package algo;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import datastructure.Bucket;
import datastructure.Cache;
import datastructure.Center;
import datastructure.Point;
import kmeans.KMeansPlusPlus;

public class CoresetCache implements CluMethod {

	// number of clusters wanted
	private final int k;
		
	// merge threshold
	private final int r;

	// bucket size
	private final int m;
	
	// number of max iterations for multi-kmeans++ (query process)
	private final int maxIter;
	
	// number of trials for multi-kmeans++ (query process)
	private final int trials;

	// coreset-tree (CT) model
	private CoresetTree ct;

	// coreset cache
	private Cache cache;
	
	public CoresetCache(int k, int bucketSize, int mergeThreshold, int maxIterations, int numTrials) {
		this.k = k;
		this.m = bucketSize;
		this.r = mergeThreshold;
		
		this.maxIter = maxIterations;
		this.trials = numTrials;
		cache = new Cache(r);
		ct = new CoresetTree(k, m, r, maxIter, trials);
	}


	/**
	 * Clustering with coreset cache, the update process is exactly same as coreset tree
	 * @param p
	 */
	@Override
	public void cluster(Point p) {
		ct.cluster(p);
	}

	
	@Override
	public List<Center> getCenters() {
		// get coresets from the corset tree and cache
		List<Point> coresets = getCoresets();
		
		// Don't forget to add the bucket_0
		coresets.addAll(ct.getBucket0().coreset);
				
		// run kmeans++ multiple times to get the best k centers
		return KMeansPlusPlus.multiKMeansPlusPlus(coresets, k, maxIter, trials);
	}
	
	
	/**
	 * Retrieve the coresets: coreset tree (minor) + coreset cache (major)
	 * @return
	 */
	public List<Point> getCoresets() {
		// number of buckets (N) received till now
		int numOfBuckets = ct.getNumOfBuckets();
		
		// directly retrieve coreset if we just have it in the cache
		if (cache.containsCoreset(numOfBuckets)) {
			return cache.getCoreset(numOfBuckets);
		}
		
		// coreset collection to be returned
		List<Point> coresets = new ArrayList<Point>();
		
		if (numOfBuckets == 0) {
			return coresets;
		}
		
		// compute major and minor of N
		int minor = cache.minor(numOfBuckets);
		int major = numOfBuckets - minor;
		
		// when the cache does not have the "major" coreset we want,
		// retreat back to the coreset tree
		if (major == 0 || !cache.containsCoreset(major)) {
			coresets = ct.getCoresets();
		}
		else {
			// otherwise, we can get the "major" coreset from the cache
			List<List<Bucket>> coresetTree = ct.getCoresetTree();
			
			// add major coreset from cache
			coresets.addAll(cache.getCoreset(major));
			
			int minorLevel = cache.minorLevel(numOfBuckets);
			List<Bucket> minorLevelBuckets = coresetTree.get(minorLevel);
			// add minor coresets from coreset tree
			for (Bucket b : minorLevelBuckets) {
				coresets.addAll(b.coreset);
			}
			
		}
		
		// shrink coresets size to m (single coreset)
		// add coreset to cache
		List<Point> singleCoreset = KMeansPlusPlus.fastSeeding(coresets, m, new Random());
		cache.insertCoreset(numOfBuckets, singleCoreset);
		
		return singleCoreset;
//		return coresets;
	}
	
	/**
     * Compute the memory cost in words, each weighted point is one word
     * @return number of weighted points in the memory that maintained
     */
    public long computeMemory() {
    	return cache.size() * m + ct.computeMemory();
    }
}

