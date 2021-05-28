package comparator;
import java.util.Comparator;
import base.Cloudlet;

/***
 * The comparator compares two candidate clouldet assignments in
 * genetic algorithm and returns the one with the lower cost.
 * This is particularly useful in implementing the priority queue.
 * @author Dixit
 *
 */
public class AssignmentsComparator implements Comparator<Cloudlet[]> {
	public int[][] cost;
	
	public AssignmentsComparator(int[][] cost) {
		// TODO Auto-generated constructor stub
		this.cost = cost;
	}

	@Override
	public int compare(Cloudlet[] a, Cloudlet[] b) {
		// TODO Auto-generated method stub
		if(placementCost(a) < placementCost(b)) {
			return -1;
		}
		else if(placementCost(a) > placementCost(b)) {
			return 1;
		}
		
		return 0;
	}
	
	private int placementCost(Cloudlet[] b) {
		// TODO Auto-generated method stub
		int total_cost = 0;
		
		for(int i = 0; i < b.length; i++) {
			if(b[i] != null) {
				total_cost += cost[b[i].id - 1][i];
			}
		}
		return total_cost;
	}

}
