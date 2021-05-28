package algorithm;

import java.util.ArrayList;
import java.util.Random;

import base.CandidatePoint;
import base.Cloudlet;
import base.EndDevice;

/***
 * A collection of methods that are used in the genetic
 * algorithm steps. All methods are organized here to avoid
 * lengthy algorithm class.
 * @author Dixit
 *
 */
public class GeneticMethods {
	
	private ArrayList<Cloudlet> C = null;
	private ArrayList<CandidatePoint> P = null;
	private ArrayList<EndDevice> E = null;
	private int[][] cost = {{}};
	private int[][] latency = {{}};
	
	private int final_cost = 0;
	private double final_latency = 0;
	private double final_coverage = 0;

	public GeneticMethods(ArrayList<Cloudlet> cloudlets, ArrayList<CandidatePoint> points, ArrayList<EndDevice> devices, int[][] cost,
			int[][] latency) {
		// TODO Auto-generated constructor stub
		this.C = cloudlets;
		this.P = points;
		this.E = devices;
		this.cost = cost;
		this.latency = latency;
	}

	//common utility methods
	
	/***
	 * Returns euclidean distance between two 2D points
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public double distance(int x1, int y1, int x2, int y2) {
		int y_diff = y2-y1;
		int x_diff = x2-x1;
		
		double x_sqr = Math.pow(x_diff, 2);
		double y_sqr = Math.pow(y_diff, 2);
		
		double dist = Math.sqrt(x_sqr + y_sqr);
		
		return dist;
	}
	
	/***
	 * Returns if a device is in range and within capacity of a cloudlet
	 * @param point
	 * @param processor
	 * @param storage
	 * @param memory
	 * @param c1
	 * @param endDevice
	 */
	public boolean inRangeAndCapacity(int point, int[] processor, int[] storage, int[] memory, Cloudlet c1, EndDevice endDevice) {
		// TODO Auto-generated method stub
		double d = distance(P.get(point).xlocation, P.get(point).ylocation,
				endDevice.xlocation, endDevice.ylocation);
		if(d <= c1.radius ){
			if(endDevice.processor <= processor[point]) {
				if(endDevice.memory <= memory[point]) {
					if(endDevice.storage <= storage[point]){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	//Initial Assignments of Cloudlets and Devices
	//Also for creating initial population
	
	/**
	 * Returns a set of random assignments as initial population
	 * @param n number of candidate points
	 * @param m number of random assignments
	 */
	public Cloudlet[][] randomAssignments(int n, int m, int min_needed_cloudlets) {
		// TODO Auto-generated method stub
		Cloudlet[][] cloudlets = new Cloudlet[m][n];
		int cloudlets_size = C.size();
		
		while(m > 0) {
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			for(int i = 0; i < n; i++) {
				indexes.add(i);
			}
			for(int j = cloudlets_size-1; j >= (cloudlets_size - min_needed_cloudlets); j--) {
				Random rand = new Random();
				int x = rand.nextInt(indexes.size());
				//System.out.println(indexes.size() + " " + c.id);
				cloudlets[m-1][indexes.get(x)] = C.get(j);
				indexes.remove(x);
			}
			//System.out.println(Arrays.toString(cloudlets[m-1]));
			m -= 1;
		}
		
		return cloudlets;
	}
	
	/***
	 * Returns one randomly generated array of cloudlet assignments.
	 * @param min_needed_cloudlets
	 */
	public Cloudlet[] oneRandomCloudletAssignment(int min_needed_cloudlets) {
		// TODO Auto-generated method stub
		Cloudlet[] cloudlets = new Cloudlet[P.size()];
		
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		for(int i = 0; i < P.size(); i++) {
			indexes.add(i);
		}
		for(int j = C.size()-1; j >= (C.size() - min_needed_cloudlets); j--) {
			Random rand = new Random();
			int x = rand.nextInt(indexes.size());
			//System.out.println(indexes.size() + " " + c.id);
			cloudlets[indexes.get(x)] = C.get(j);
			indexes.remove(x);
		}
		return cloudlets;
	}
	
	/**
	 * Returns initial assignment of devices
	 * Each device is assigned to its closest candidate point
	 */
	public int[] deviceAssignments() {
		
		int[] devices = new int[E.size()];
		for(int i = 0; i < E.size(); i++) {
			double min_dist = Double.MAX_VALUE;
			int min_dist_index = 0;
			for(int j = 0; j < P.size(); j++) {
				double d = distance(E.get(i).xlocation, E.get(i).ylocation, P.get(j).xlocation, P.get(j).ylocation);
				if(d < min_dist) {
					min_dist = d;
					min_dist_index = j;
				}
				devices[i] = min_dist_index;
			}
		}
		return devices;
	}
	
	
	//Genetic operations such as fitness calculation,
	//crossover and mutation
	
	/**
	 * Returns the fitness values i.e. displacement from cost of placement.
	 * Lower the better.
	 * @param b cloudlet assignment
	 */
	public int fitness(Cloudlet[] b, int estimate_optimal_cost) {
		// TODO Auto-generated method stub
		int total_cost = 0;
		//System.out.println("cloudlets length: " + b.length);
		for(int i = 0; i < b.length; i++) {
			if(b[i] != null) {
				//System.out.println(b[i].id + " " + b[i].processor);
				total_cost += cost[b[i].id - 1][i];
			}
		}
		
		int dist_from_total = Math.abs(total_cost - estimate_optimal_cost);
		
		return dist_from_total;
	}
	
	/**
	 * Returns a validated crossover of two cloudlet assignments
	 * @param c1
	 * @param c2
	 * @return
	 */
	public Cloudlet[][] crossOver(Cloudlet[] c1, Cloudlet[] c2) {
		// TODO Auto-generated method stub
		Cloudlet[][] crossed = new Cloudlet[c1.length][2];
		int mid_point = (int)Math.round(Math.ceil(c1.length/2.0));
		
		for(int i = mid_point; i < c1.length; i++) {
			Cloudlet buffer = c1[i];
			c1[i] = c2[i];
			c2[i] = buffer;
		}
		
		crossed[0] = validate(c1.clone());
		crossed[1] = validate(c2.clone());
		
		return crossed;
	}
	
	/**
	 * Mutation function. Returns a mutated cloudlet placement.
	 * @param b cloudlet placement
	 */
	public Cloudlet[] mutate(Cloudlet[] b) {
		// TODO Auto-generated method stub
		Cloudlet[] mutated = b;
		int assignment_size = b.length;
		
		//create an arraylist to get used cloudlets
		ArrayList<Cloudlet> used = new ArrayList<Cloudlet>();
		for(int i = 0; i < assignment_size; i++) {
			if(b[i] != null) {
				used.add(b[i]);
			}
		}
		
		//Arraylist of unused cloudlets, get all available cloudlets first
		ArrayList<Cloudlet> unused = (ArrayList<Cloudlet>) C.clone();
		unused.removeAll(used); //remove all used cloudlets, unused remaining
		
		//For picking a random unused cloudlet
		Random rand = new Random();
		
		//System.out.println("Mutate0 " + Arrays.toString(mutated));
		for(int i = 0; i < assignment_size; i++) {
			//+1 to coount for null case, i.e. removing cloudlet
			int x = rand.nextInt(unused.size()+1);
			
			//probability of making a cloudlet position null increases
			//as more cloudlet positions are mutated to a different cloudlet
			if(x == unused.size()) {
				mutated[i] = null;
			}
			else {
				mutated[i] = unused.get(x);
				unused.remove(x);
			}
			
		}
		
		//System.out.println("Mutate2 " + Arrays.toString(A));
		
		return mutated;
	}
	
	/**
	 * Validates a crossed over placement and returns a valid instance
	 * @param b cloudlet placement
	 */
	public Cloudlet[] validate(Cloudlet[] b) {
		/*correct the cloudlet numbers if they 
		became greater than available cloudlets 
		it's possible after both crossover*/
		
		int assignment_size = b.length;
		
		//create an arraylist to get used cloudlets
		ArrayList<Cloudlet> used = new ArrayList<Cloudlet>();
		for(int i = 0; i < assignment_size; i++) {
			//do not add duplicates or nulls
			if(b[i] != null && !used.contains(b[i])) {
				used.add(b[i]);
			}
		}
		
		//Arraylist of unused cloudlets, get all available cloudlets first
		ArrayList<Cloudlet> unused = (ArrayList<Cloudlet>) C.clone();
		unused.removeAll(used); //remove all used cloudlets, unused remaining
		
		//If a cloudlet is duplicated within a crossover placement
		//replace it with similar sized cloudlet if available in 
		//unused set; make it null if unavailable. Otherwise, continue.
		for(int i = 0; i < assignment_size; i++) {
			//remove the cloudlet from used arraylist
			used.remove(b[i]);
			//if duplicate still exists
			if(used.contains(b[i])) {
				//replace if a similar sized cloudlet is unused
				int unused_size = unused.size();
				boolean replaced = false;
				for(int j = 0; j < unused_size; j++) {
					if(b[i].toString() == unused.get(j).toString()) {
						b[i] = unused.get(j);
						replaced = true;
						break;
					}
				}
				
				//make null otherwise
				if(!replaced) {
					b[i] = null;
				}
				else {
					//the cloudlet is now used for replacement
					//so remove it from unused arraylist
					unused.remove(b[i]);
				}
			}
		}
					
		return b;
		
	}
	
	//Greedy coverage, total cost calculations and selections
	
	
	/**
	 * Return the placement/assignment with the least cost
	 * @param cloudlets collection of cloudlet placements
	 */
	public int selectLeastCost(Cloudlet[][] cloudlets) {
		// TODO Auto-generated method stub
		int min_cost = Integer.MAX_VALUE;
		int min_cost_index = 0;
		
		for(int i = 0; i < cloudlets.length; i++) {
			int sum_cost = totalCost(cloudlets[i]);
			if(sum_cost < min_cost) {
				min_cost = sum_cost;
				//TODO: Consider this calculation
				this.final_cost = min_cost;
				min_cost_index = i;
			}
		}
		
		return min_cost_index;
	}
	
	/*public int selectLeastLatency(int[][] devices_new, Cloudlet[][] cloudlets) {
		// TODO Auto-generated method stub
		int min_latency_index = 0;
		int min_latency = Integer.MAX_VALUE;
		for(int i = 0; i < cloudlets.length; i++) {
			int sum_latency = totalLatency(devices_new[i], cloudlets[i]);
			//System.out.println();
			if(sum_latency < min_latency) {
				min_latency = sum_latency;
				this.final_latency = min_latency;
				//System.out.println(min_dist);
				min_latency_index = i;
			}
		}
		return min_latency_index;
	}*/
	
	/**
	 * Returns total cost of a cloudlet placement
	 * @param b cloudlet placement/assignment
	 */
	public int totalCost(Cloudlet[] b) {
		// TODO Auto-generated method stub
		int total_cost = 0;
		
		for(int i = 0; i < b.length; i++) {
			if(b[i] != null) {
				total_cost += cost[b[i].id - 1][i];
			}
		}
		
		return total_cost;
	}
	
	/**
	 * Returns the total latency value based on placed cloudlets
	 * @param devices set of devices
	 * @param cloudlets set of placed cloudlets
	 */
	public int totalLatency(int[] devices, Cloudlet[] cloudlets) {
		// TODO Auto-generated method stub
		int sum_latency = 0;
		for(int j = 0; j < devices.length; j++) {
			int point_index = devices[j];
			if(cloudlets[point_index] != null) {
				sum_latency += latency[j][point_index];
				//System.out.print(latency[j][point_index] + " ");
			}
		}
		return sum_latency;
	}
	
	/**
	 * Returns comprehensive coverage value of devices by placed cloudlets
	 * @param devices
	 * @param cloudlets
	 */
	public double maxCoverage(int[] devices, Cloudlet[] cloudlets) {
		// TODO Auto-generated method stub
		int[] devices_new = devices.clone();
		double covered = 0;
		
		//System.out.println("->" + Arrays.toString(devices_new[i]));
		int[] processor = new int[cloudlets.length];
		int[] memory = new int[cloudlets.length];
		int[] storage = new int[cloudlets.length];
		
		//copy of the cloudlet specifications so that
		//they do get reset for next coverage maximization
		for(int j = 0; j < cloudlets.length; j++) {
			if(cloudlets[j] != null) {
				processor[j] = cloudlets[j].processor;
				memory[j] = cloudlets[j].memory;
				storage[j] = cloudlets[j].storage;
			}
		}
		
		for(int j = 0; j < E.size(); j++) {
			int index = devices_new[j];
			//System.out.println(index + " " +cloudlets[index]);
			if(cloudlets[index] == null) {
				double min_dist = Double.MAX_VALUE;
				int min_dist_index = index;
				for(int k = 0; k < cloudlets.length; k++) {
					if(cloudlets[k] != null && inRangeAndCapacity(k, processor, memory, storage, cloudlets[k], E.get(j))) {
						double d = distance(P.get(k).xlocation, P.get(k).ylocation,
								E.get(j).xlocation, E.get(j).ylocation);
						if(d < min_dist) {
							min_dist = d;
							min_dist_index = k;
						}
					}
				}
				//System.out.println(devices_new[j] + " " + min_dist_index);
				devices_new[j] = min_dist_index;
				if(cloudlets[min_dist_index] != null) {
					//System.out.println("There");
					covered++;
					processor[min_dist_index] -= E.get(j).processor;
					memory[min_dist_index] -= E.get(j).memory;
					storage[min_dist_index] -= E.get(j).storage;
				}
			}
			else {
				//System.out.println("Here");
				if(inRangeAndCapacity(index, processor, storage, memory, cloudlets[index], E.get(j))) {
					covered++;
					//System.out.println(processor[point] + " - " + cloudlets[index].processor);
					processor[index] -= E.get(j).processor;
					memory[index] -= E.get(j).memory;
					storage[index] -= E.get(j).storage;
				}
			}
			//System.out.println("Covered: " + covered/E.size());
			//System.out.println(">-" + Arrays.toString(devices_new[i]));
		}
		return covered/E.size();
	}
	
	/**
	 * Returns indexes of the devices related with max coverage value
	 * @param devices
	 * @param cloudlets
	 */
	public int[] maxCoverageIndexes(int[] devices, Cloudlet[] cloudlets) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
				int[] devices_new = devices.clone();
				
				//System.out.println("->" + Arrays.toString(devices_new[i]));
				int[] processor = new int[cloudlets.length];
				int[] memory = new int[cloudlets.length];
				int[] storage = new int[cloudlets.length];
				
				//copy of the cloudlet specifications so that
				//they do get reset for next coverage maximization
				for(int j = 0; j < cloudlets.length; j++) {
					if(cloudlets[j] != null) {
						processor[j] = cloudlets[j].processor;
						memory[j] = cloudlets[j].memory;
						storage[j] = cloudlets[j].storage;
					}
				}
				
				for(int j = 0; j < E.size(); j++) {
					int index = devices_new[j];
					//System.out.println(index + " " +cloudlets[index]);
					if(cloudlets[index] == null) {
						double min_dist = Double.MAX_VALUE;
						int min_dist_index = index;
						for(int k = 0; k < cloudlets.length; k++) {
							if(cloudlets[k] != null && inRangeAndCapacity(k, processor, memory, storage, cloudlets[k], E.get(j))) {
								double d = distance(P.get(k).xlocation, P.get(k).ylocation,
										E.get(j).xlocation, E.get(j).ylocation);
								if(d < min_dist) {
									min_dist = d;
									min_dist_index = k;
								}
							}
						}
						//System.out.println(devices_new[j] + " " + min_dist_index);
						devices_new[j] = min_dist_index;
						if(cloudlets[min_dist_index] != null) {
							//System.out.println("There");
							processor[min_dist_index] -= E.get(j).processor;
							memory[min_dist_index] -= E.get(j).memory;
							storage[min_dist_index] -= E.get(j).storage;
						}
					}
					else {
						//System.out.println("Here");
						if(inRangeAndCapacity(index, processor, storage, memory, cloudlets[index], E.get(j))) {
							//System.out.println(processor[point] + " - " + cloudlets[index].processor);
							processor[index] -= E.get(j).processor;
							memory[index] -= E.get(j).memory;
							storage[index] -= E.get(j).storage;
						}
					}
					//System.out.println("Covered: " + covered/E.size());
					//System.out.println(">-" + Arrays.toString(devices_new[i]));
				}
				return devices_new;
	}
	
	//An example cost estimation method
	//Can be used to replace LP solution as cost estimate
	//Is faster but returns a less robust estimated value
	
	/*private int estimateOptimal() {
		// TODO Auto-generated method stub
		int total_proc_demand = 0;
		int total_mem_demand = 0;
		int total_stor_demand = 0;
		int estimate_optimal_cost = 0;
		//this.min_needed_cloudets = 0;
		
		for(EndDevice e: E) {
			total_proc_demand += e.processor;
			total_mem_demand += e.memory;
			total_stor_demand += e.storage;
		}
		
		int[] estimated_all = new int[3];
		estimated_all[0] = estimateByType(total_proc_demand, 'p');
		estimated_all[1] = estimateByType(total_mem_demand, 'm');
		estimated_all[2] = estimateByType(total_stor_demand, 's');
		estimate_optimal_cost = Math.max(estimated_all[0], Math.max(estimated_all[1], estimated_all[2]));
		//System.out.println(estimated_all[0]+ " " + Math.max(estimated_all[1], estimated_all[2]));
		
		return estimate_optimal_cost;
	}
	
	private int estimateByType(int total_demand, char type) {
		// TODO Auto-generated method stub
		int optimal_for_type = 0;
		int counter = C.size()-1;
		int capacity = 0;
		
		while(counter >= 0) {
			if(type == 'p') {
				capacity = C.get(counter).processor;
			}
			else if(type == 'm') {
				capacity = C.get(counter).memory;
			}
			else if(type == 's') {
				capacity = C.get(counter).storage;
			}
			if(total_demand >= capacity) {
				//System.out.println(total_proc_demand + " " + C.get(counter).processor + " " + cost[C.get(counter).id - 1][0]);
				total_demand -= capacity;
				int min_val = Integer.MAX_VALUE;
				for (int element : cost[C.get(counter).id - 1]) {
				    min_val = Math.min(min_val, element);
				}
				//System.out.println(min_val);
				optimal_for_type  += min_val;
				//this.min_needed_cloudets++;
			}
			else if(total_demand > 0) {
				//System.out.println(total_proc_demand + " " + C.get(counter).processor + " " + cost[C.get(counter).id - 1][0]);
				total_demand -= capacity;
				int min_val = Integer.MAX_VALUE;
				for (int element : cost[C.get(counter).id - 1]) {
				    min_val = Math.min(min_val, element);
				}
				//System.out.println(min_val);
				optimal_for_type  += min_val;
				//this.min_needed_cloudets++;
			}
			counter--;
		}
		
		return optimal_for_type;
	}*/
	
	
	//calculate final values
	/**
	 * Calculate final coverage and latency values based on
	 * index of the solution
	 * @param index
	 * @param cloudlets 
	 * @param devices_max 
	 * @param devices 
	 */
	public void finalValues(int index, int[] devices, int[] devices_max, Cloudlet[] cloudlets) {
		final_latency = totalLatency(devices_max, cloudlets);
		final_coverage = maxCoverage(devices, cloudlets);
	}
	
	//getters
	
	public int getCost() {
		return this.final_cost;
	}
	
	public double getLatency() {
		return this.final_latency;
	}
	
	public double getCoverage() {
		return this.final_coverage;
	}
	
}
