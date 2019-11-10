
package algo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import datastructure.Bucket;
import datastructure.Cache;
import datastructure.Center;
import datastructure.Point;
import kmeans.KMeansPlusPlus;

public class TwoRecursiveCache implements CluMethod {

	// number of clusters wanted
	private final int k;

	// merge threshold (outter)
	private final int r1;

	// merge threshold (inner)
	private final int r2;

	// bucket size
	private final int bucketSize;

	// number of max iterations for multi-kmeans++ (query process)
	private final int maxIterations;

	// number of trials for multi-kmeans++ (query process)
	private final int numTrials;

	// coreset-cache (CC) model
	private List<CoresetCache> ccList;

	// coreset tree: each level of the tree is a list of buckets
	private List<List<Bucket>> coresetTree;
	
	// coreset cache
	private Cache cache;

	// initial bucket: size can be 0 to m-1 (both inclusive)
	private Bucket bucket_0;

	// number of buckets received in the tree
	private int numOfBuckets;

	
	/**
	 * 
	 * @param k
	 * @param bucketSize
	 * @param r1 merge threshold of outter tree
	 * @param r2 merge threshold of inner tree (each level of outter tree)
	 * @param maxIterations
	 * @param numTrials
	 */
	public TwoRecursiveCache(int k, int bucketSize, int r1, int maxIterations, int numTrials) {
		this.k = k;
		this.bucketSize = bucketSize;
		this.r1 = r1;    // outter merge threshold (coreset tree)
		this.r2 = Math.max(2, (int)Math.sqrt(r1));  // square root of outter merge threshold (r1)

		this.maxIterations = maxIterations;
		this.numTrials = numTrials;
		
		bucket_0 = new Bucket(bucketSize);
		coresetTree = new ArrayList<>();
		cache = new Cache(r1);
		
		ccList = new ArrayList<CoresetCache>();
	}

	
	@Override
	public void cluster(Point p) {
		// add new point to the bucket 0
		bucket_0.addPoint(p);
		// when bucket 0 is full, update the coreset tree
		if (bucket_0.coresetSize() == bucketSize) {
			// a new bucket received
			numOfBuckets++;

			// carry digit
			Bucket bucketCarry = bucket_0;
			// empty bucket 0
			bucket_0 = new Bucket(bucketSize);

			for (int i = 0; i < coresetTree.size(); i++) {
				List<Bucket> currentLevel = coresetTree.get(i);
				// Recursive Cache: coreset-cache model (CC) of current level
				CoresetCache ccModel = ccList.get(i);
				
				// number of buckets at level i is less than (r1 - 1),
				// then no need to increment additionally
				if (currentLevel.size() < (r1 - 1)) {
					currentLevel.add(bucketCarry);
					
					// Recursive Cache: add to coreset-cache model (CC)
					for (Point p1 : bucketCarry.coreset) {
						ccModel.cluster(new Point(p1));  // deep copy
					}
					
					return;
				}
				
				bucketCarry = bucketCarry.mergeBuckets(currentLevel);
				// empty this level (list of buckets)
				currentLevel.clear();
				// Recursive Cache: empty the coreset cache model
				ccModel = new CoresetCache(k, bucketSize, r2, maxIterations, numTrials);
			}
			
			List<Bucket> nextLevel = new ArrayList<>();
			nextLevel.add(bucketCarry);
			coresetTree.add(nextLevel);
			
			// Recursive Cache: add to coreset-cache model (CC)
			CoresetCache ccModel = new CoresetCache(k, bucketSize, r2, maxIterations, numTrials);
			for (Point p1 : bucketCarry.coreset) {
				ccModel.cluster(new Point(p1));  // deep copy
			}
			ccList.add(ccModel);
		}
	}


	@Override
	public List<Center> getCenters() {
		// get coresets from the corset tree and cache
		List<Point> coresets = getCoresets();

		// Don't forget to add the bucket_0
		coresets.addAll(bucket_0.coreset);

		// run kmeans++ multiple times to get the best k centers
		return KMeansPlusPlus.multiKMeansPlusPlus(coresets, k, maxIterations, numTrials);
	}
	
	
	/**
	 * Retrieve the coresets: coreset tree (minor) + coreset cache (major)
	 * @return
	 */
	public List<Point> getCoresets() {
		
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
		// retreat back to the coreset tree, 
		// Recursive Cache: use coreset-cache every level
		if (major == 0 || !cache.containsCoreset(major)) {
			for (CoresetCache ccModel : ccList) {
				coresets.addAll(ccModel.getCoresets());
			}
		}
		else {
			// add major coreset from cache
			coresets.addAll(cache.getCoreset(major));
			
			// Recursive Cache: add minor coresets from coreset-cache at minor level
			int minorLevel = cache.minorLevel(numOfBuckets);
			CoresetCache minorLevelCacheModel = ccList.get(minorLevel);
			// add minor coresets to the collection
			coresets.addAll(minorLevelCacheModel.getCoresets());
		}
		
		// shrink coresets size to m (single coreset)
		// add coreset to cache
		List<Point> singleCoreset = KMeansPlusPlus.fastSeeding(coresets, bucketSize, new Random());
		cache.insertCoreset(numOfBuckets, singleCoreset);
		
		return singleCoreset;
//		return coresets;
	}
	
	
	@Override
	public long computeMemory() {
		long memory = 0;
		
		// add coreset tree size
		for (List<Bucket> level : coresetTree) {
			memory += level.size() * bucketSize;
		}
		
		// add cache size
		numOfBuckets += cache.size() * bucketSize;
		
		// Recursive Cache: add each level coreset-cache model size
		for (CoresetCache ccModel : ccList) {
			numOfBuckets += ccModel.computeMemory();
		}
		
		return memory + bucket_0.coresetSize();
	}
	
}
