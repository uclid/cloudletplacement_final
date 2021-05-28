package base;

public class CandidatePoint {
	public int id;
	public int xlocation;
	public int ylocation;
	
	/**
	 * @author Dixit Bhatta
	 * @param id identifier
	 * @param xlocation x-coordinate in the grid
	 * @param ylocation y-coordinate in the grid
	 */
	public CandidatePoint(int id, int xlocation, int ylocation) {
		this.id = id;
		this.xlocation = xlocation;
		this.ylocation = ylocation;
	}

}
