package datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class Cache {
	
	// merge threshold
	public int r;
	
	// number of buckets received
	public int numOfBuckets; 
	
	private HashMap<Integer, List<Point>> map;
	
	public Cache(int r) {
		this.r = r;
		numOfBuckets = 0;
		map = new HashMap<Integer, List<Point>>();
	}
	
	public int size() {
		return map.size();
	}
	
	public boolean containsCoreset(int key) {
		return map.containsKey(key);
	}
	
	public List<Point> getCoreset(int key) {
		return map.get(key);
	}
	
	public void insertCoreset(int num, List<Point> coreset) {
		map.put(num, coreset);
		
		// remove coresets not in the partsums
		HashSet<Integer> partSumList = partSum(num);
		Iterator<Integer> iter = map.keySet().iterator();
		while (iter.hasNext()) {
			int key = iter.next();
			if (!partSumList.contains(key)) {
				iter.remove();
			}
		}
	}
	
	/**
	 * return the part sums of number n
	 * TODO Use bit manipulation
	 * @param n
	 * @return
	 */
	private HashSet<Integer> partSum(int n) {
    	List<Integer> list = new ArrayList<Integer>();
    	HashSet<Integer> partSumSet = new HashSet<Integer>();
    	
    	int weight = 1;
    	while (n > 0) {
    		int a = n % r;
    		n = n / r;
    		if (a != 0) {
    			list.add(a * weight);
    		}
    		weight = weight * r;
    	}
    	int temp = 0;
		for (int i = list.size() - 1; i >= 0; i--) {
			temp += list.get(i);
			partSumSet.add(temp);
		}
		return partSumSet;
    }
    
	public int major(int n) {
		return n - minor(n);
	}
	
    public int minor(int n) {
    	if (n == 0) {
    		return 0;
    	}
    	int weight = 1;
    	while (n % r == 0) {
    		n = n / r;
    		weight = weight * r;
    	}
    	int minor =  weight * (n % r);
    	return minor;
    }
    
    /**
     * Return the least significant digit level, start from level 0
     * @param n
     * @return
     */
    public int minorLevel(int n) {
    	int level = 0;
    	while (n % r == 0) {
    		n = n / r;
    		level++;
    	}
    	return level;
    }
    
    
    /* for test */
    public static void main(String[] args) {
//    	System.out.println(new Cache(3).partSum(70).toString());
//    	System.out.println(new Cache(3).minor(69));
    	System.out.println(new Cache(4).partSum(69).toString());
    }

}
