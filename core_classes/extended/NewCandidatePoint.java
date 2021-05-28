package extended;
import java.util.ArrayList;
import base.CandidatePoint;

public class NewCandidatePoint extends CandidatePoint{
	
	public ArrayList<Integer> assn_devices = new ArrayList<Integer>();
	public int assn_cloudlet = -1;
	public int max_radius = 0;
	public int total_demand = 0;

	public NewCandidatePoint(int id, int xlocation, int ylocation) {
		super(id, xlocation, ylocation);
		// TODO Auto-generated constructor stub
	}
	
	public NewCandidatePoint getById(int id) {
		if(this.id == id) {
			return this;
		}
		return null;
	}

}
