package algorithm;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import base.*;
import extended.*;

public class ApproxLPRounding {

	/**
	 * An LP Based approximation of cloudlet placement problem
	 * @author Dixit
	 * @param a_s Solution of LP Cost
	 * @param devices Set of end devices
	 * @param points Set of Candidate points
	 * @param cloudlets Set of Cloudlets
	 * @param cost Deployment Cost matrix
	 * @param latency Latency Matrix
	 */
	
	private static int upgrade_cap = -1;
	private static int upgrade_rad = -1;
	private static int upgrade_cost = -1;
	public String method = "ACP";
	public int solution_cost = 0;
	public int solution_latency = 0;
	
	public void approximate(double[][] a_s, ArrayList<NewEndDevice> devices, ArrayList<NewCandidatePoint> points,
			ArrayList<NewCloudlet> cloudlets, int[][] cost, int[][] latency) {

		//set it first before cloudlet capacities are changed
		Cloudlet smallest = cloudlets.get(0);
		upgrade_cap = smallest.processor;
		upgrade_rad = smallest.radius;
		upgrade_cost = (smallest.processor + smallest.memory 
				+ smallest.storage + smallest.radius/1000);
		
		// find 2L_i for all
		for (int i = 0; i < devices.size(); i++) {
			double L_i = 0;
			for (int j = 0; j < points.size(); j++) {
				if (a_s[i][j] > 0) {
					L_i += a_s[i][j] * latency[i][j];
					// System.out.print(latency[i][j] + " ");
				}
			}
			devices.get(i).TwoDi = 2 * L_i;
			//System.out.print(Math.round(devices.get(i).TwoDi) + ", ");

			// Enlist feasible fractionally assigned
			// candidate points based on 2L_i
			for (int k = 0; k < points.size(); k++) {
				if (((2 * L_i) - latency[i][k]) > 0 && (a_s[i][k] > 0)) {
					devices.get(i).N.add(k + 1);
				}
			}
		}
		//System.out.println();
		
		//for (NewEndDevice d : devices) { System.out.print(d.N + " "); }
		 

		//create new temp set for iteration
		ArrayList<NewEndDevice> temp_devices = (ArrayList<NewEndDevice>) devices.clone();
		ArrayList<NewEndDevice> skipped = new ArrayList<NewEndDevice>();
		HashMap<Integer, Integer> device_assn = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> cloudlet_assn = new HashMap<Integer, Integer>();

		//until all devices are either assigned or skipped
		while(temp_devices.size() > 0) {
			//get device with largest Li
			NewEndDevice currDevice = getMaxDevice(temp_devices);
			//System.out.println("\nDevice Selected: " + currDevice.id);
			
			//find the best candidate point (least latency) for that device
			//and place smallest cloudlet feasible to that point
			int currCandidate = getBestCandidate(currDevice, latency, cost);
			//System.out.println("Candidate Point: " + currCandidate);
			
			NewCloudlet currCloudlet = null;
			if(currCandidate != -1) {
				currCloudlet = getSmallestCloudlet(currDevice, currCandidate, points, 
					cloudlets, cloudlet_assn);
				//System.out.println("Cloudlet selected: " + currCloudlet);
			}
			
			//follow these steps if a feasible cloudlet is found
			if(currCloudlet != null) {
				//System.out.println("Cloudlet: " + currCloudlet.id);
				
				//assign the device to the point
				device_assn.put(currDevice.id, currCandidate);
				
				//assign the cloudlet to the point
				cloudlet_assn.put(currCandidate, currCloudlet.id);
				
				//mark the cloudlet as used
				currCloudlet.used = true;
				
				//adjust capacities
				currCloudlet.memory -= currDevice.memory;
				//System.out.println("Memory"+ currCloudlet.id +" : " + currCloudlet.memory);
				
				//find extended neighborhood of currDevice
				ArrayList<NewEndDevice> extended = getExtendedSet(temp_devices,currCandidate, currDevice); 

				//System.out.println(extended);
				
				//assign all unassigned devices from extended set
				//as long as cloudlet capacity allows
				
				while(extended.size() > 0) {
					NewEndDevice d = getMaxDevice(extended);
					//if(!device_assn.containsKey(d.id)) {
					//get the candidates for coordinates, to get distance
					NewCandidatePoint cand = points.get(currCandidate - 1); 
					int dist = (int)Math.round(distance(cand.xlocation, cand.ylocation, d.xlocation,d.ylocation));
					
					//int lat = latency[d.id -1][currCandidate -1];
					if(dist <= currCloudlet.radius && 
							currDevice.memory <= currCloudlet.memory) { 
						currCloudlet.memory -= d.memory;
						//System.out.println("Memory"+ currCloudlet.id +" : " + currCloudlet.memory);
						device_assn.put(d.id, currCandidate);
						temp_devices.remove(d);
					}
					else {
						//System.out.print(d.toString() + "->" + lat + ", ");
					}
					//}
					extended.remove(d);
				}
				
				
				/*
				for(NewEndDevice d: extended) {
					//making sure the device is not already assigned
					//evaluate only if it is not already assigned
					if(!device_assn.containsKey(d.id)) {
						int lat = latency[d.id -1][currCandidate -1];
						if(lat <= currCloudlet.radius*5 && 
								currDevice.memory <= currCloudlet.memory) { 
							currCloudlet.memory -= d.memory;
							//System.out.println("Memory"+ currCloudlet.id +" : " + currCloudlet.memory);
							device_assn.put(d.id, currCandidate);
							temp_devices.remove(d);
						}
					}
				}
				*/
				
				
			}else {
				skipped.add(currDevice);
			}
			
			//remove the device
			temp_devices.remove(currDevice);
			
		}
		
		//System.out.println("Candidate->Cloudlet: " + cloudlet_assn);
		//System.out.println("Device->Candidate: " + device_assn);
		//System.out.println("Skipped: " + skipped);
		//System.out.println("Skipped Size: " + skipped.size());
		
		//assign all the skipped devices now
		//assign to the closest point with an already placed cloudlet
		ArrayList<NewEndDevice> fixedWithUpg = new ArrayList<NewEndDevice>(); 
		//skipped devices remaining
		for(NewEndDevice d: skipped) {
			//System.out.println("Device: " + d.id);
			int least_lat = Integer.MAX_VALUE;
			int best_lat = Integer.MAX_VALUE;
			int selected_point = -1;
			int best_point = -1;
			for (Entry<Integer, Integer> entry : cloudlet_assn.entrySet()) {
				int k = entry.getKey();
				//System.out.println("Point: " + k);
				int lat = latency[d.id -1 ][k - 1];
				//System.out.println("Latency: " + lat);
				 //assign to a feasible point with the least
				 //System.out.println("Point " + k);
				if(least_lat > lat) { 
					//System.out.println("Here"); 
					//cloudlet should have sufficient capacity and radius 
					NewCloudlet temp = cloudlets.get(entry.getValue()-1);
					/*
					 * System.out.println(temp.id + " Memory remaining: " + temp.memory +
					 * " Radius*5: " + temp.radius*5);
					 */
					//distance to compare with radius
					NewCandidatePoint cand = points.get(k - 1); 
					int dist = (int)Math.round(distance(cand.xlocation, cand.ylocation, d.xlocation,d.ylocation));
					
					if(temp.memory >= d.memory && 
							dist <= temp.radius){
						//System.out.println("here");
						least_lat = lat; 
						selected_point = k;
					}
					
				}
				if(best_lat > lat) {
					best_lat = lat;
					best_point = k;
				}
				
			}
			if(selected_point != -1) {
				device_assn.put(d.id, selected_point);
				cloudlets.get(cloudlet_assn.get(selected_point)-1).memory -= d.memory;
			}
			else {
				fixedWithUpg.add(d);
				//System.out.println("Cloudlet map" + cloudlet_assn);
				//System.out.println("Best point" + best_point);
				NewCloudlet temp = cloudlets.get(cloudlet_assn.get(best_point)-1);
				//int lat = latency[d.id -1 ][best_point - 1];
				/*
				System.out.println("Capacity before: " + temp.memory + 
						" radius before: " + temp.radius);
				*/
				
				//int[] updates = upgradeCloudlet(temp, lat);
				//temp.memory += updates[0];
				//temp.radius += updates[1];
				temp.memory += upgrade_cap;
				temp.radius += upgrade_rad;
				//System.out.println("Cloudlet Upgraded: " + temp.id);
				
				
				System.out.println("Cloudlet Upgraded: " + temp.id + 
						" for device: " + d.id);
				System.out.println("Capacity: " + temp.memory + 
						" radius: " + temp.radius);
			
				//update cost for the upgraded cloudlet
				cost[temp.id-1][best_point-1] += upgrade_cost;
				device_assn.put(d.id, best_point);
				temp.memory -= d.memory;
				/*
				System.out.println("Capacity after: " + temp.memory + 
						" radius after: " + temp.radius);
						*/
			}
		}
		
		
		this.solution_cost = totalCost(cloudlet_assn, cost);
		this.solution_latency = totalLatency(device_assn, latency);
		System.out.println("\nFINAL RESULTS");
		System.out.println("Candidate->Cloudlet: " + cloudlet_assn);
		System.out.println("Device->Candidate: " + device_assn);
		System.out.println("Total Cost: " + this.solution_cost);
		System.out.println("Total Latency: " + this.solution_latency);
		
		System.out.println("Skipped device fixed by upgrade: " + fixedWithUpg);
		//System.out.println(cloudlets.get(cloudlet_assn.get(4)-1).memory);
		//System.out.println(cloudlets.get(cloudlet_assn.get(2)-1).memory);

	}

	/*
	private static int[] upgradeCloudlet(NewCloudlet temp, int lat) {
		// TODO Auto-generated method stub
		int[] updates = {0,0};
		
		int upgrade_factor = lat/(temp.radius*5);
		
		if(upgrade_factor <= 2) {
			updates[0] = 50;
			updates[1] = 1;
		}
		else {
			updates[0] = 100;
			updates[1] = 2;
		}
		
		System.out.println("Cloudlet" + temp.id + " upgraded by factor " 
		+ upgrade_factor);
		return updates;
	}*/

	private static int totalLatency(HashMap<Integer, Integer> device_assn, int[][] latency) {
		// TODO Auto-generated method stub
		int total_latency = 0;
		for (Entry<Integer, Integer> entry : device_assn.entrySet()) {
			int d = entry.getKey(); //device
			int p = entry.getValue(); //point
			
			total_latency += latency[d-1][p-1];
			
		}
		return total_latency;
	}

	private static int totalCost(HashMap<Integer, Integer> cloudlet_assn, int[][] cost) {
		// TODO Auto-generated method stub
		int total_cost = 0;
		for (Entry<Integer, Integer> entry : cloudlet_assn.entrySet()) {
			int p = entry.getKey(); //point
			int c = entry.getValue(); //cloudlet
			
			total_cost += cost[c-1][p-1];
			
		}
		return total_cost;
		
	}

	private static ArrayList<NewEndDevice> getExtendedSet(ArrayList<NewEndDevice> devices, int currCandidate, NewEndDevice currDevice) {
		// TODO Auto-generated method stub
		ArrayList<NewEndDevice> extended = new ArrayList<NewEndDevice>();
		for(NewEndDevice d : devices) { 
			if(d.N.contains(currCandidate) && d.equals(currDevice) == false) {
				extended.add(d); 
			 }
		}
		
		return extended;
	}

	private static NewCloudlet getSmallestCloudlet(NewEndDevice currDevice, 
			int currCandidate, ArrayList<NewCandidatePoint> points, ArrayList<NewCloudlet> cloudlets,
			HashMap<Integer, Integer> cloudlet_assn) {
		// TODO Auto-generated method stub
		NewCloudlet smallestCloudlet = null;
		
		//if cloudlet is already placed at candidate point
		//return immediately if radius and capacity are satisfied
		if(cloudlet_assn.containsKey(currCandidate)) {
			NewCloudlet c = cloudlets.get(cloudlet_assn.get(currCandidate)-1);
			NewCandidatePoint cand = points.get(currCandidate - 1); 
			int dist = (int)Math.round(distance(cand.xlocation, cand.ylocation, currDevice.xlocation , currDevice.ylocation));
			if(dist <= c.radius && currDevice.memory <= c.memory) {
				return c;
			}
			//System.out.println("Not enough capacity");
		}
		//otherwise, choose a different one
		//return as soon as a suitable cloudlet is found
		//cloudlets are pre-sorted in dataset
		else {
			NewCandidatePoint cand = points.get(currCandidate - 1); 
			//System.out.println(cand.xlocation + "," + cand.ylocation + "-" + currDevice.xlocation + "," + currDevice.ylocation);
			int dist = (int)Math.round(distance(cand.xlocation, cand.ylocation, currDevice.xlocation , currDevice.ylocation));
			//System.out.println("Distance: " + dist);
			for(NewCloudlet c: cloudlets) {
				if(dist <= c.radius && currDevice.memory <= c.memory && 
						c.used == false) { 
					return c;
				}
				//System.out.println("Not suitable cloudlet");
			}
		}
		
		//only null is returned here
		return smallestCloudlet;
	}

	private static int getBestCandidate(NewEndDevice currDevice, int[][] latency, int[][] cost) {
		// TODO Auto-generated method stub
		
		ArrayList<Integer> neighbors = currDevice.N;
		int i = currDevice.id - 1;
		int assigned = -1; //nothing assigned
		int least_lat = Integer.MAX_VALUE;
		int lowCost = Integer.MAX_VALUE;
		
		for (Integer index : neighbors) {
			int lat = latency[i][index - 1];
			//System.out.print("-lat:" + lat + "-");
			int pCost = cost[0][index - 1];
			//no issue if latency strictly smaller
			if (lat < least_lat) {
				least_lat = lat;
				assigned = index;
				lowCost = pCost;
				//System.out.print("-Cand" + (assigned) + "-");
			}
			//if latency is equal
			else if(lat == least_lat) {
				//tie break with cost
				if(pCost < lowCost) {
					assigned = index;
					lowCost = pCost;
				}
			}
		}

		return assigned;
	}

	private static NewEndDevice getMaxDevice(ArrayList<NewEndDevice> temp_devices) {
		// TODO Auto-generated method stub
		NewEndDevice maxDevice = null;
		double maxTwoDi = -1.0;
		for(NewEndDevice d: temp_devices) {
			if(d.TwoDi > maxTwoDi) {
				maxDevice = d;
				maxTwoDi = d.TwoDi;
			}
		}
		
		return maxDevice;
	}
	
	private static NewEndDevice getMinDevice(ArrayList<NewEndDevice> temp_devices) {
		// TODO Auto-generated method stub
		NewEndDevice minDevice = null;
		double minTwoDi = Integer.MAX_VALUE;
		for(NewEndDevice d: temp_devices) {
			if(d.TwoDi < minTwoDi) {
				minDevice = d;
				minTwoDi = d.TwoDi;
			}
		}
		
		return minDevice;
	}
	
	/***
	 * Returns euclidean distance between two 2D points
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public static double distance(int x1, int y1, int x2, int y2) {
		int y_diff = y2-y1;
		int x_diff = x2-x1;
		
		double x_sqr = Math.pow(x_diff, 2);
		double y_sqr = Math.pow(y_diff, 2);
		
		double dist = Math.sqrt(x_sqr + y_sqr);
		
		return dist;
	}

	/*
	public static void main(String[] args) {

		// 5 cloudlets as per our optimization example
		int num_cloudlets = 5; //3 //14
		// 25 end devices as per our optimization example
		int num_devices = 25; //15  //343
		// 7 candidate points as per our optimization example
		int num_candidates = 7; //4  //18

		ReadDataFolder reader = new ReadDataFolder();
		int[][] cloudlet_specs = reader.getCloudlets(num_cloudlets);
		int[][] device_specs = reader.getDevices(num_devices);
		int[][] cand_points = reader.getPoints(num_candidates);

		ArrayList<NewCloudlet> cloudlets = new ArrayList<NewCloudlet>();
		//for cplex
		ArrayList<Cloudlet> cloudlets2 = new ArrayList<Cloudlet>();
		for (int i = 0; i < cloudlet_specs.length; i++) {
			cloudlets.add(new NewCloudlet(cloudlet_specs[i][0], cloudlet_specs[i][1], cloudlet_specs[i][2],
					cloudlet_specs[i][3], cloudlet_specs[i][4]));
			//for cplex
			cloudlets2.add(new Cloudlet(cloudlet_specs[i][0], cloudlet_specs[i][1], cloudlet_specs[i][2],
					cloudlet_specs[i][3], cloudlet_specs[i][4]));
		}

		ArrayList<NewEndDevice> devices = new ArrayList<NewEndDevice>();
		// for cplex model
		ArrayList<EndDevice> devices2 = new ArrayList<EndDevice>();
		for (int i = 0; i < device_specs.length; i++) {
			devices.add(new NewEndDevice(device_specs[i][0], device_specs[i][1], device_specs[i][2], device_specs[i][3],
					device_specs[i][4], device_specs[i][5]));
			// for cplex model
			devices2.add(new EndDevice(device_specs[i][0], device_specs[i][1], device_specs[i][2], device_specs[i][3],
					device_specs[i][4], device_specs[i][5]));
		}

		ArrayList<CandidatePoint> points = new ArrayList<CandidatePoint>();
		for (int i = 0; i < cand_points.length; i++) {
			points.add(new CandidatePoint(cand_points[i][0], cand_points[i][1], cand_points[i][2]));
		}

		// Cost Matrix
		int[][] cost = reader.getCosts(num_cloudlets, num_candidates);

		// Latency Matrix
		int[][] latency = reader.getLatencies(num_devices, num_candidates);

		// timing the CPLEX run
		long startTime = System.nanoTime();
		CplexLPCloudletPlacement place = new CplexLPCloudletPlacement();
		int[] results = place.cplexModel(cloudlets2, points, devices2, cost, latency);
		approximate(place.a_s, devices, points, cloudlets, cost, latency);
		long endTime = System.nanoTime();

		long duration = (endTime - startTime) / 1000000;

		// System.out.println(Arrays.toString(place.y_s));
		// System.out.println(Arrays.toString(place.a_s));

		System.out.println("LP Objective Value (rounded): " + results[0] + " " + 
		"Cloudlets deployed: " + results[1] + " Latency: " + results[2]);
		System.out.println("Time = " + duration + " ms");

	}*/

}
