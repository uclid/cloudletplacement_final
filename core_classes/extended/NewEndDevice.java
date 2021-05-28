package extended;
import java.util.ArrayList;
import base.EndDevice;

public class NewEndDevice extends EndDevice{
	
	public double TwoDi = 0.0;
	public ArrayList<Integer> N = new ArrayList<Integer>();
	public int assn_point = -1;

	public NewEndDevice(int id, int processor, int memory, int storage, int xlocation, int ylocation) {
		super(id, processor, memory, storage, xlocation, ylocation);
		// TODO Auto-generated constructor stub
	}
	
	public NewEndDevice getById(int id) {
		if(this.id == id) {
			return this;
		}
		return null;
	}

}
