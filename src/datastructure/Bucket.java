package datastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kmeans.KMeansPlusPlus;


public class Bucket {
	
	public int m;   // bucket capacity
	
	// Bucket B_0 can store any number between 0 and m points,
	// for i>=1, bucket B_i is either empty or contains exactly m points.
	// Coreset is a list of weighted data points.
	public List<Point> coreset;
	
	public Bucket(int m) {
		this.m = m;
		this.coreset = new ArrayList<>();
	}
	
	public Bucket(int m, List<Point> coreset) {
		this.m = m;
		this.coreset = coreset;
	}
	
	/**
	 * clone bucket b to return a new bucket
	 * @param b
	 */
	public Bucket(Bucket b) {
		this.m = b.m;
		this.coreset = new ArrayList<>();
		// deep copy
		for (Point p : b.coreset) {
			this.coreset.add(new Point(p));
		}
	}
	
	/**
	 * add a new point to the coreset
	 * @param p new point
	 */
	public void addPoint(Point p) {
		coreset.add(p);
	}
	
	/**
	 * get coreset size
	 * @return
	 */
	public int coresetSize() {
		return coreset.size();
	}
	
	/**
	 * Merge the coreset in this bucket (size m) with other buckets (coresets)
	 * @return return a new bucket with merged coreset (size m)
	 */
	public Bucket mergeBuckets(List<Bucket> bucketList) {
		// the coreset in this bucket
		List<Point> unionSet = coreset;
		// union all the coresets in the bucketList
		for (Bucket b : bucketList) {
			unionSet.addAll(b.coreset);
		}
		List<Point> mergedCoreset = KMeansPlusPlus.fastSeeding(unionSet, m, new Random());	
		Bucket mergedBucket = new Bucket(m, mergedCoreset);
		return mergedBucket;
	}
}
