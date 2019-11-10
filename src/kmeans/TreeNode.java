
package kmeans;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import datastructure.Point;

public class TreeNode {

	// cluster center
	Point center;

	// <member point, weighted-cost to center>
	List<Pair<Point, Double>> members;

	// number of member points
	int numMembers;

	// weighted-cost of all members to center
	double weight;

	TreeNode left;

	TreeNode right;

	TreeNode parent;

	public TreeNode(Point center) {
		this.center = new Point(center);
		this.members = new ArrayList<>();
	}
}
