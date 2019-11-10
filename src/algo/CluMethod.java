package algo;
import java.util.List;

import datastructure.Center;
import datastructure.Point;


public interface CluMethod {
	
	public void cluster(Point p);
	
	public long computeMemory();
	
	public List<Center> getCenters();
	
}
