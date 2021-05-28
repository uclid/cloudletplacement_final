package extended;
import base.Cloudlet;

public class NewCloudlet extends Cloudlet{
	
	public boolean used = false;
	public int assn_point = -1;

	public NewCloudlet(int id, int processor, int memory, int storage, int radius, String type) {
		super(id, processor, memory, storage, radius, type);
		// TODO Auto-generated constructor stub
	}
	
	public NewCloudlet getById(int id) {
		if(this.id == id) {
			return this;
		}
		return null;
	}

}
