
package datastructure;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.util.MathArrays;

public class Point implements Clusterable {
	
	public double weight;
	
	public double[] position;
	

	public Point(double[] position, double weight) {
		this.weight = weight;
		this.position = Arrays.copyOf(position, position.length);
	}
	
	public Point(Point p) {
		this.weight = p.weight;
		this.position = Arrays.copyOf(p.position, p.position.length);
	}
	
	
	@Override
	public double[] getPoint() {
		return position;
	}
	

	/**
	 * compute Euclidian distance to point q
	 * @param q
	 * @return
	 * @throws DimensionMismatchException
	 */
	public double euclidDistTo(Point q) throws DimensionMismatchException {
		return MathArrays.distance(position, q.position);
	}
	
	
	/**
	 * compute squared distance to point q
	 * @param q
	 * @return
	 * @throws DimensionMismatchException
	 */
	public double squaredDistance(Point q) throws DimensionMismatchException {
		int lenP = position.length;
		int lenQ = q.position.length;
		if (lenP != lenQ) {
    		throw new DimensionMismatchException(lenQ, lenP);
    	}
    	double sum = 0.0;
    	for (int i=0; i<lenP; i++) {
    		double dif = position[i] - q.position[i];
    		sum = sum + dif * dif;
    	}
    	return sum;
	}
	
	
	/**
	 * returns the nearest center to the given point
	 * @param centers
	 * @param point
	 * @return
	 */
    public int getNearestCluster(List<Center> centers) {
        double minDistance = Double.POSITIVE_INFINITY;
        int minCluster = 0;
        for (int i=0; i<centers.size(); i++) {
        	Point c = centers.get(i);
            double distance = euclidDistTo(c);
            if (distance < minDistance) {
                minDistance = distance;
                minCluster = i;
            }
        }
        return minCluster;
    }
    
    
    /**
     * returns the distance to the nearest center
     * @param clusters
     * @param point
     * @return
     */
    public double getNearestDistance(List<Center> centers) {
    	double minDist = Double.POSITIVE_INFINITY;
        for (Point c : centers) {
        	double d = euclidDistTo(c);
			minDist = Math.min(minDist, d);
        }
        return minDist;
    }
    
}
