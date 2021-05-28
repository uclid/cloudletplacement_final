package base;
public class EndDevice {
	public int id;
	public int processor;
	public int memory;
	public int storage;
	public int xlocation;
	public int ylocation;
	
	/**
	 * @author Dixit Bhatta
	 * @id identifier
	 * @param processor processing demand
	 * @param memory memory demand
	 * @param storage storage demand
	 * @param xlocation x-coordinate in the grid
	 * @param ylocation y-coordinate in the grid
	 */
	public EndDevice(int id, int processor, int memory, int storage, int xlocation, int ylocation) {
		this.id = id;
		this.processor = processor;
		this.memory = memory;
		this.storage = storage;
		this.xlocation = xlocation;
		this.ylocation = ylocation;
	}
	
	public String toString() {
		return (this.id + "=>" + this.processor);
	}

}
