package datastructure;

import java.util.ArrayList;
import java.util.List;

/**
 * Center class contains the member points
 *
 */
public class Center extends Point {
	
	// member points of the cluster
	public List<Point> members;
	
	public double avgRadius;
	
	public Center(double[] position, double weight) {
		super(position, weight);
		members = new ArrayList<Point>();
		avgRadius = 0;
	}
	
	public Center(Point p) {
		super(p);
		members = new ArrayList<Point>();
		avgRadius = 0;
	}
	
	public void addMemberPoint(Point p) {
		members.add(p);
	}
	
	public void resetMembers() {
		members = new ArrayList<Point>();
	}
}
