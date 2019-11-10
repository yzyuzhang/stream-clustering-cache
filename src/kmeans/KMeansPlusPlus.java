package kmeans;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.util.Pair;

import datastructure.Center;
import datastructure.Point;


public class KMeansPlusPlus {

	
	/**
	 * run multiple kmeans++ to select the best k centers 
	 * Note: commons-math3 library does not support "weighted point"
	 * 
	 * @param points input points to be clustered (weighted points)
	 * @param k
	 * @param maxIterations
	 * @param numTrials
	 * @return
	 */
	public static List<Center> multiKMeansPlusPlus(List<Point> points, int k, int maxIterations, int numTrials) {
		// at first, we have not found any clusters list yet
        List<Center> best = null;
        double bestVarianceSum = Double.POSITIVE_INFINITY;

        // do several clustering trials
        for (int i = 0; i < numTrials; i++) {

            // compute a clusters list
            List<Center> clusters = cluster(points, k, maxIterations);

            // compute the variance of the current list
            final double varianceSum = Evaluate.kmeansCost(points, clusters);

            if (varianceSum < bestVarianceSum) {
                // this one is the best we have found so far, remember it
                best = clusters;
                bestVarianceSum = varianceSum;
            }
        }

        // return the best clusters list found
        return best;
	}
	
	
	/**
	 * Runs the K-means++ clustering algorithm.
	 * 
	 * @param points the points to cluster (weighted points)
	 * @param k
	 * @param maxIterations
	 * @return a list of clusters containing the points
	 */
	public static List<Center> cluster(final List<Point> points, int k, int maxIterations) {

	        // number of clusters has to be smaller or equal the number of data points
	        if (points.size() < k) {
	            throw new NumberIsTooSmallException(points.size(), k, false);
	        }

	        // create the initial clusters (fast kmeans++ seeding)
	        List<Point> seedingPoints = fastSeeding(points, k, new Random());
	        List<Center> clusters = new ArrayList<Center>();
	        for (Point p : seedingPoints) {
	        	clusters.add(new Center(p));
	        }

	        // create an array containing the latest assignment of a point to a cluster
	        int[] assignments = new int[points.size()];
	        assignPointsToClusters(clusters, points, assignments);

	        // iterate through updating the centers until we're done
	        final int max = Math.min(30, maxIterations);
	        for (int count = 0; count < max; count++) {
	            boolean emptyCluster = false;
	            List<Center> newClusters = new ArrayList<Center>();
	            for (final Center cluster : clusters) {
	                final Center newCenter;
	                if (cluster.members.isEmpty()) {
	                	// split the largest variance cluster
	                    Point newPoint = getPointFromLargestVarianceCluster(clusters, new Random());
	                    newCenter = new Center(newPoint);
	                    emptyCluster = true;
	                } else {
	                	// compute centroid of member points in the cluster
	                    newCenter = centroidOf(cluster.members, cluster.position.length);
	                }
	                newClusters.add(new Center(newCenter));
	            }
	            int changes = assignPointsToClusters(newClusters, points, assignments);
	            clusters = newClusters;

	            // if there were no more changes in the point-to-cluster assignment
	            // and there are no empty clusters left, return the current clusters
	            if (changes == 0 && !emptyCluster) {
	                return clusters;
	            }
	        }
	        return clusters;
	    }
	
	
	/**
     * Adds the given points to the closest cluster.
     *
     * @param clusters the centers to add the points to
     * @param points the points to add to the given clusters
     * @param assignments points assignments to clusters
     * @return the number of points assigned to different clusters as the iteration before
     */
	private static int assignPointsToClusters(final List<Center> clusters, final List<Point> points,
			final int[] assignments) {
		int assignedDifferently = 0;
		int pointIndex = 0;
		for (final Point p : points) {
			int clusterIndex = p.getNearestCluster(clusters);
			if (clusterIndex != assignments[pointIndex]) {
				assignedDifferently++;
			}

			Center cluster = clusters.get(clusterIndex);
			cluster.addMemberPoint(p);
			assignments[pointIndex++] = clusterIndex;
		}

		return assignedDifferently;
	}
    
    
    /**
     * Computes the centroid for a set of points.
     *
     * @param points the set of points (weighted points)
     * @param dimension the point dimension
     * @return the computed centroid for the set of points
     */
    private static Center centroidOf(final List<Point> points, final int dimension) {
        double[] centroid = new double[dimension];
        double weight = 0;
        for (final Point p : points) {
            final double[] pos = p.getPoint();
            for (int i = 0; i < centroid.length; i++) {
                centroid[i] += pos[i] * p.weight;
            }
            weight += p.weight;
        }
        // normalize
        for (int i = 0; i < centroid.length; i++) {
            centroid[i] /= weight;
        }
        return new Center(centroid, weight);
    }
    
    
    /**
     * Get a random point from the cluster with the largest distance variance.
     *
     * @param clusters the clusters to search
     * @return a random point from the selected cluster
     * @throws ConvergenceException if clusters are all empty
     */
    private static Point getPointFromLargestVarianceCluster(final List<Center> clusters, Random random)
            throws ConvergenceException {

        double maxVariance = Double.NEGATIVE_INFINITY;
        Center selected = null;
        for (final Center cluster : clusters) {
            if (!cluster.members.isEmpty()) {
                // compute the distance variance of the current cluster
                final Variance stat = new Variance();
                for (final Point point : cluster.members) {
                    stat.increment(point.euclidDistTo(cluster) * point.weight);
                }
                final double variance = stat.getResult();

                // select the cluster with the largest variance
                if (variance > maxVariance) {
                    maxVariance = variance;
                    selected = cluster;
                }
            }
        }

        // did we find at least one non-empty cluster?
        if (selected == null) {
            throw new ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
        }

        // extract a random point from the cluster
        final List<Point> selectedPoints = selected.members;
        return selectedPoints.remove(random.nextInt(selectedPoints.size()));
    }

    
    /**
	 * fast D^2 sampling acceleration by coreset tree
	 * Note our input points are weighted
	 * (StreamKM++: A Clustering Algorithm for Data Streams, by Ackermann et al)
	 * @param points
	 * @param m
	 * @param randSeed
	 * @return
	 */

    public static List<Point> fastSeeding(final List<Point> points, int m, Random randSeed) {
		if (points.size() < m) {
			throw new NumberIsTooSmallException(points.size(), m, false);
		}

		// choose first center uniformly at random from points
        // Note: as each input point is weighted, we sample by each point's weight,
		// so higher weight means higher probability to be selected
		double sumOfWeights = 0;
		double[] pointWeights = new double[points.size()];
		for (int i=0; i<points.size(); i++) {
    		pointWeights[i] = points.get(i).weight;
    		sumOfWeights += pointWeights[i];
    	}
		int firstCenterIndex = sampleByWeight(pointWeights, sumOfWeights * randSeed.nextDouble());
		Point firstCenter = points.get(firstCenterIndex);
		
		// create first center and root node
		TreeNode root = new TreeNode(firstCenter); 
		double sumOfCost = 0;
		for (int i=0; i<points.size(); i++) {
			if (i == firstCenterIndex) {
				continue;
			}
			// Note: we can have a shallow copy here
			Point p = points.get(i);
			// distance to new center
			double dist2Center = p.euclidDistTo(firstCenter);  
			// weighted-cost (D^2) to new center
			double cost = dist2Center * dist2Center * p.weight;
			sumOfCost += cost;
			Pair<Point, Double> pair = new Pair<>(p, cost);
			root.members.add(pair);
		}
		root.weight = sumOfCost;
		// all the points remove one selected as center
		root.numMembers = points.size() - 1;

		// generate 2 to m centers
		int numOfCenters = 1;
		while (numOfCenters < m) {
			// find leaf node
			TreeNode node = root;
			while (node.left != null && node.right != null) {
				// first check if left child or right child has a 
				// "free" member point (not assigned as center),
				// if not, we should choose the other child anyway :(
				if (node.left.numMembers == 0) {
					node = node.right;
					continue;
				}
				if (node.right.numMembers == 0) {
					node = node.left;
					continue;
				}
				
				double leftNodeWeight = node.left.weight;
				double rightNodeWeight = node.right.weight;
				// sample by weights of two child nodes
				if (randSeed.nextDouble() < (leftNodeWeight / (leftNodeWeight + rightNodeWeight))) {
					node = node.left;
				}
				else {
					node = node.right;
				}
			}
			
			// choose one point in the leaf node P_l based on 
			// the D^2 sampling to the center of P_l
			Point leafCenter = node.center;
			List<Pair<Point, Double>> leafPoints = node.members;
			
			// compute weighted-squared-distance to the center,
			// which is equal to the cost to the center and we
			// have already computed
			double distSqSum = node.weight;
	        double[] squaredDist = new double[leafPoints.size()];
	        for (int i = 0; i < leafPoints.size(); i++) {
	        	squaredDist[i] = leafPoints.get(i).getSecond();
            }
            
            // sample next center
            int nextCenterIndex = sampleByWeight(squaredDist, distSqSum * randSeed.nextDouble());
			Point nextCenter = leafPoints.get(nextCenterIndex).getFirst();
			
			// generate left child TreeNode with previous leaf center (see Fig 2. in the paper)
			TreeNode leftChildNode = new TreeNode(leafCenter);
			// generate right child TreeNode with new center
			TreeNode rightChildNode = new TreeNode(nextCenter);
			double leftChildWeight = 0;
			double rightChildWeight = 0;
			for (int i=0; i<leafPoints.size(); i++) {
				if (i == nextCenterIndex) {
					continue;
				}
				
				// cost to the previous center is already computed
				Point p = leafPoints.get(i).getFirst();
				double cost = leafPoints.get(i).getSecond();
				// compute weighted-cost of each point to the new center
				double dist2Center = p.euclidDistTo(nextCenter);
				double cost2Center = dist2Center * dist2Center * p.weight;
				if (cost < cost2Center) {
					// add point p to left child TreeNode
					// shallow copy
					leftChildNode.members.add(new Pair<Point, Double>(p, cost));
					leftChildWeight += cost;
				}
				else {
					// add point p to right child TreeNode
					// shallow copy
					rightChildNode.members.add(new Pair<Point, Double>(p, cost2Center));
					rightChildWeight += cost2Center;
				}
			}
			// assign weight and numMembers of two child nodes
			leftChildNode.weight = leftChildWeight;
			leftChildNode.numMembers = leftChildNode.members.size();
			
			rightChildNode.weight = rightChildWeight;
			rightChildNode.numMembers = rightChildNode.members.size();
			
			// link tree nodes
			node.left = leftChildNode;
			node.right = rightChildNode;
			leftChildNode.parent = node;
			rightChildNode.parent = node;
			
			// propagate update of weight and numMembers attribute upwards to node root
			while (node != null) {
				// weight attribute of an inner node is defined as 
				// the sum of the weights of its child nodes
				node.weight = node.left.weight + node.right.weight;
				node.numMembers--;  // one member point has been selected as center
				node = node.parent;
			}
			
			// new center found (Don't forget)
			numOfCenters++;
		}
		
		// all leaf nodes in the tree are centers
		List<Point> resultSet = new ArrayList<Point>();
		dfs(root, resultSet);
		return resultSet;
	}
	
	
	/**
	 * sum through the nums, stopping when sum >= r.
	 * @param nums
	 * @param r
	 * @return
	 */
	private static int sampleByWeight(double[] nums, double r) {
		double sum = 0.0;
		for (int i=0; i<nums.length; i++) {
			sum += nums[i];
			if (sum >= r) {
				return i;
			}
		}
		// the point wasn't found in the previous for loop, 
		// probably because distances are extremely small.  
		// Just pick the last available point.
		return (nums.length-1);
	}
	
	
	/**
	 * retrieve all leaf nodes centers
	 * Note: we can not have a shallow copy for returned center
	 * @param node
	 * @param resultSet
	 */
	private static void dfs(TreeNode node, List<Point> resultSet) {
		if (node == null) {
			return;
		}
		
		// leaf node
		if (node.left==null && node.right==null) {
			double weight = node.center.weight;
			for (Pair<Point, Double> pair : node.members) {
				weight += pair.getFirst().weight;
			}
			Point center = new Point(node.center.position, weight);
			resultSet.add(center);
			return;
		}
		
		// inner node
		dfs(node.left, resultSet);
		dfs(node.right, resultSet);
	}
	
}
