package algo;
import java.util.ArrayList;
import java.util.List;

import datastructure.Center;
import datastructure.Point;


public class FirstKSeq implements CluMethod {
	
    private final int k;   // number of clusters

    private final int d;   // number of dimensions
    
    private List<Center> centers;   // cluster centers
    
    private int numOfPoints;

    
    public FirstKSeq(int k, int d) {
        this.k = k;
        this.d = d;
        this.numOfPoints = 0;
        this.centers = new ArrayList<Center>();
    }
    

    @Override
    public List<Center> getCenters() {
    	return centers;
    }
    
    
    @Override
    public long computeMemory() {
    	return k;
    }

    
    /**
     * run the sequential clustering algorithm
     * @param p
     */
    @Override
	public void cluster(Point p) {
    	
    	numOfPoints++;
    	
		// collect first k points as initial centers
		if (numOfPoints <= k) {
			// use new point as initial center
			Center c = new Center(p);
			centers.add(c);
			return;
		}
		
		// assign point p to the nearest center
        Point nearestCenter = centers.get(p.getNearestCluster(centers));
        
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
	
}

