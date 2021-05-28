package alternate_approx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import base.*;

public class ApproxJMS {
	
	//the final mappings of all cloudlets to devices
	public HashMap<Cloudlet, ArrayList<EndDevice>> mappings = new HashMap<Cloudlet, ArrayList<EndDevice>>();
	
	/*
	public static void main(String[] args) {
		
		//5 cloudlets as per our optimization example
		//THIS IS NOT USED IN JMS
		//We are just assuming homogeneous cloudlets
		int num_cloudlets = 5;
		
		//25 end devices as per our optimization example
		int num_devices = 25;
		
		//7 candidate points as per our optimization example
		//number of cloudlets is the same as num candidate points
		//in JMS because each cloudlet has the same specs
		int num_candidates = 7;
		
		ReadCSV reader = new ReadCSV();
		int[][] cloudlet_specs = reader.getCloudlets(num_cloudlets);
		int[][] device_specs = reader.getDevices(num_devices);
		int[][] cand_points = reader.getPoints(num_candidates);
		
		//All medium sized cloudlets (for homogeneity) and
		//equal to number of candidate points
		ArrayList<Cloudlet> cloudlets = new ArrayList<Cloudlet>();
		for(int i = 0; i < cand_points.length; i++) {
			cloudlets.add(new Cloudlet(i, cloudlet_specs[2][1],
					cloudlet_specs[2][2], cloudlet_specs[2][3], cloudlet_specs[2][4]));
		}
		
		//All devices are homogeneous too, so that we can have fixed capacities
		//for the cloudlets
		ArrayList<EndDevice> devices = new ArrayList<EndDevice>();
		for(int i = 0; i < device_specs.length; i++) {
			devices.add(new EndDevice(i, device_specs[1][1],
					device_specs[1][2], device_specs[1][3], device_specs[i][4], device_specs[i][5]));
		}
		
		ArrayList<CandidatePoint> points = new ArrayList<CandidatePoint>();
		for(int i = 0; i < cand_points.length; i++) {
			points.add(new CandidatePoint(i, cand_points[i][1], cand_points[i][2]));
		}
		
		//Cost Matrix (facility cost)
		int[][] cost = reader.getCosts(num_cloudlets, num_candidates);
		
		//Latency Matrix (connection cost)
		int[][] latency = reader.getLatencies(num_devices, num_candidates);
		
		//System.out.println(num_large + " " + num_medium + " " + num_small);
		
		ApproxJMS device_assignment = new ApproxJMS();
		
		long startTime = System.nanoTime();
		
		device_assignment.devicesJMS(cloudlets, points, devices, cost, latency);
		
		long endTime = System.nanoTime();
		
		long duration = (endTime - startTime)/1000000;
		
		System.out.println("\nRunning Time = " + duration + " ms");
		
	}*/
	
	
	public void devicesJMS(ArrayList<Cloudlet> cloudlets, 
			ArrayList<CandidatePoint> points, ArrayList<EndDevice> devices, int[][] cost, int[][] latency) {
		
		//create a copy to track unconnected devices so far
		ArrayList<EndDevice> unconnectedDevices = new ArrayList<EndDevice>();
		for(EndDevice e: devices) {
			unconnectedDevices.add(e);
		}
		
		//create a copy to track unconnected devices so far
		ArrayList<Cloudlet> unopenedCloudlets = new ArrayList<Cloudlet>();
		for(Cloudlet c: cloudlets) {
			unopenedCloudlets.add(c);
		}
		
		//budget of every device (city) is initialized to zero
		HashMap<EndDevice, Integer> budget = new HashMap<EndDevice, Integer>();
		for(int i = 0; i < devices.size(); i++) {
			budget.put(devices.get(i),0);
		}
		
		//find the capacities of each cloudlet (facility)
		ArrayList<Integer> capacity = getCapacities(cloudlets, devices, cost);
		//System.out.println(capacity.toString());
		
		ArrayList<Cloudlet> openCloudlets = new ArrayList<Cloudlet>();
		
		boolean unOpened = false;
		boolean unConnected = false;
		
		int time = 0;
		//while three is an unconnected device
		while(unconnectedDevices.size() != 0) {
			System.out.println("Time/Round = " + time + "\n");
			/*try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			
			//for some unplaced cloudlet i, if the sum of offers from devices equal
			//the placement cost, for every device j with offer >= latency, connect it
			//to cloudlet i, and remove it from the unconnected devices list
			if(unopenedCloudlets.size() != 0) {
				unOpened = false;
				System.out.println("Budget " + budget.values());
				System.out.println("\nDevice offers for each cloudlet.");
				for(Cloudlet c: unopenedCloudlets) {
					int sum_offers = 0;
					int[] offer_vector = new int[devices.size()];
					int proc_capacity = c.processor; //addition of capacity constraint
					for(EndDevice e: devices) {
						//Offer from devices
						int offer = 0;
						//Check if the device can be placed with the constraints
						if(inRangeAndCapacity(points.get(c.id), proc_capacity, c, e)) {
							//If cloudlet is unplaced and device unconnected: max(budget - latency, 0)
							if(unconnectedDevices.contains(e)) {
								offer = Math.max(budget.get(e) - latency[e.id][c.id], 0);
								if(offer > 0) {
									//reduce processing capacity of the cloudlet
									proc_capacity -= e.processor;
								}
							}
							//If cloudlet is unplaced and device connected to other: max(other latency - latency, 0)
							else {
								Cloudlet previous = findConnectedCloudlet(e);
								offer = Math.max(latency[e.id][previous.id] - latency[e.id][c.id], 0);
								//replenishing budget just for balancing later deduction
								if(offer > 0) {
									budget.replace(e, latency[e.id][previous.id]);
									//reduce processing capacity of the cloudlet
									proc_capacity -= e.processor;
								}
								//System.out.print("E=" + offer + " ");
							}
						}
						System.out.print(offer + " ");
						offer_vector[e.id] = offer;
						sum_offers += offer;
					}
					//System.out.println("Remaining processing: " + proc_capacity);
					
					//Capacitated cost: f_i(k) = f_i*ceil(k/u_i), u_i is capacity
					int cloudlet_cost = getSoftCapacitatedCost(offer_vector, cost[2][c.id], capacity.get(c.id));
					
					//Basic Cost: f_i(k) = 0 if k==0, f_i(k) = f_i if k > 0
					//int cloudlet_cost = cost[2][c.id];
					System.out.println("Cloudlet" + c.id + "=> Cost: " + cloudlet_cost + " Total offer: " + sum_offers);
					
					if(sum_offers >= cloudlet_cost) {
						//open the cloudlet
						unOpened = true;
						System.out.println("Cloudlet opened " + c.id);
						openCloudlets.add(c);
						unopenedCloudlets.remove(c);
						
						for(EndDevice e: devices) {
							if(offer_vector[e.id] > 0) {
								//reduce the budget, i.e. offer is paid
								int current_budget = budget.get(e);
								current_budget -= offer_vector[e.id];
								budget.replace(e, current_budget);
								
								//reduce the cloudlet capacity for
								//each confirmed device assignment
								c.processor -= e.processor;
								c.memory -= e.memory;
								c.storage -= e.storage;
								
								System.out.println("Device connected " + e.id + " to cloudlet " + c.id);
								addToList(c,e); //add device to mappings
								unconnectedDevices.remove(e); //remove the device
								
							}
						}
						System.out.println("Devices not connected " + unconnectedDevices.toString());
						break;
					}
				}
			}
			else {
				unOpened = false;
			}
			
			//for some unconnected device j, and some already placed cloudlet i, 
			//if offer = latency connect that device j with the cloudlet i, 
			//and remove j from unconnected devices list
			if(openCloudlets.size() != 0) {
				unConnected = false;
				System.out.println(budget.values());
				for(EndDevice e: unconnectedDevices) {
					for(Cloudlet c: openCloudlets) {
						//Check if the device can be placed with the constraints
						if(inRangeAndCapacity(points.get(c.id), c.processor, c, e)) {
							//If cloudlet already placed: budget should be = latency
							if(latency[e.id][c.id] == budget.get(e)) {
								unConnected = true;
								
								//reduce the budget, i.e. offer is paid
								int current_budget = budget.get(e);
								current_budget -= latency[e.id][c.id];
								budget.replace(e, current_budget);
								
								//reduce the cloudlet capacity for
								//each confirmed device assignment
								c.processor -= e.processor;
								c.memory -= e.memory;
								c.storage -= e.storage;
								
								System.out.println("Device connected " + e.id + " to cloudlet " + c.id);
								addToList(c, e); //add device to mappings
								unconnectedDevices.remove(e); //remove the device
								break;
							}
						}
					}
					if(unConnected) {
						System.out.println("Devices not connected " + unconnectedDevices.toString());
						break;
					}
				}
			}
			else {
				unConnected = false;
			}
			
			//Increase budget by 1 for every unconnected device, until 
			//one of the above events occur (i.e. none of above events
			//occur). Above events can occur simultaneously, and are 
			//processed in arbitrary order when they do so.
			if(!unOpened && !unConnected) {
				//increase budget of unconnected devices by fixed amount:
				//by 1 since all values in the problem are integers
				for(EndDevice e: unconnectedDevices) {
					int current_budget = budget.get(e);
					current_budget++;
					budget.replace(e, current_budget);
				}
				//System.out.println(unconnectedDevices.toString());
				//System.out.println(budget.values());
			}
			
			System.out.print("Cloudlets Placed: ");
			for(Cloudlet c: openCloudlets)
				System.out.print("Cloudlet" + c.id + " ");
			System.out.println("\n");
			time++;
		}
		
		int total_cost = 0;
		int total_latency = 0;
		for(Cloudlet key: mappings.keySet()) {
			System.out.println("Cloudlet" + key.id + " " + mappings.get(key));
			double cap = capacity.get(key.id);
			int cloudlet_cost = cost[2][key.id]*(int)Math.ceil(mappings.get(key).size()/cap);
			total_cost += cloudlet_cost;
			for(EndDevice e: mappings.get(key)) {
				total_latency += latency[e.id][key.id];
			}
		}
		System.out.println("Total cost: " + total_cost + " Total latency: " + total_latency);
		
		
		
	}
	
	//check if a device can be served by a cloudlet
	private boolean inRangeAndCapacity(CandidatePoint point, int processor, Cloudlet c, EndDevice e) {
		// TODO Auto-generated method stub
		double d = distance(point.xlocation, point.ylocation,
				e.xlocation, e.ylocation);
		if(d <= c.radius ){
			if(e.processor <= processor) {
						return true;
			}
		}
		return false;
	}
	
	//get the euclidean distance between two points
	public double distance(int x1, int y1, int x2, int y2) {
		int y_diff = y2-y1;
		int x_diff = x2-x1;
		
		double x_sqr = Math.pow(x_diff, 2);
		double y_sqr = Math.pow(y_diff, 2);
		
		double dist = Math.sqrt(x_sqr + y_sqr);
		
		return dist;
	}

	//implementing the cost function for soft capacitated
	//version of JMS
	private int getSoftCapacitatedCost(int[] offer_vector, int i, double capacity) {
		// TODO Auto-generated method stub
		int cost = 0;
		int count = 0; //number of devices served
		
		for(int a: offer_vector) {
			if(a>0) {
				count++;
			}
		}
		
		if(count == 0) {
			cost = i;
			//System.out.println("Cost " + cost);
		}
		else {
			//Capacitated cost: f_i(k) = f_i*ceil(k/u_i), u_i is capacity
			cost = i*(int)Math.ceil(count/capacity);
			System.out.print("Cost factor " + Math.ceil(count/capacity) + " ");
		}
		
		return cost;
	}

	//find if a device is already connected to other cloudlet
	//return that cloudlet if it is
	private Cloudlet findConnectedCloudlet(EndDevice e) {
		// TODO Auto-generated method stub
		for (Cloudlet c : mappings.keySet() ) {
			ArrayList<EndDevice> list = mappings.get(c);
			if(list.contains(e)) {
				return c;
			}
		}
		return null;
	}

	//returns the max number of devices each cloudlet can serve i.e. capacity
	private ArrayList<Integer> getCapacities(ArrayList<Cloudlet> cloudlets, ArrayList<EndDevice> devices, int[][] cost) {
		
		ArrayList<Integer> capacity = new ArrayList<Integer>();
		
		double total_device_demand = devices.get(0).processor*devices.size();
		
		for(int i = 0; i < cloudlets.size(); i++) {
			//device demand by cloudlet capacity to get max number of devices that can be served
			capacity.add((int) Math.floor(total_device_demand/cloudlets.get(i).processor));
		}
		
		return capacity;
	}
	
	//adds the device to the cloudlet mapping list
	public void addToList(Cloudlet c, EndDevice e) {
	    ArrayList<EndDevice> deviceList = mappings.get(c);
	 
	    // if list does not exist create it
	    if(deviceList == null) {
	    	deviceList = new ArrayList<EndDevice>();
	    	deviceList.add(e);
	    	mappings.put(c, deviceList);
	    } 
	    else {
	    	deviceList.add(e);
	    }
	}

}
