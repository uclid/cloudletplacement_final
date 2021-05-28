package base;
public class Cloudlet {
	public int id;
	public int processor;
	public int memory;
	public int storage;
	public int radius;
	public String type;
	
	/**
	 * @author Dixit Bhatta
	 * @param id identifier
	 * @param processor processing capacity
	 * @param memory memory capacity
	 * @param storage storage capacity
	 * @param radius coverage radius
	 * @param type type of cloudlet: small, medium, large 
	 */
	public Cloudlet(int id, int processor, int memory, int storage, int radius, String type) {
		this.id = id;
		this.processor = processor;
		this.memory = memory;
		this.storage = storage;
		this.radius = radius;
		this.type = type;
	}
	
	public String toString() {
		return this.type;
	}

}
