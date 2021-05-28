package algorithm;
import java.util.ArrayList;
import base.*;
import comparator.ArrayIndexComparator;
import java.util.Arrays;

public class DeviceAssignments {
	
	private ArrayList<Cloudlet> C = null;
	private ArrayList<CandidatePoint> P = null;
	private ArrayList<EndDevice> E = null;
	private int[][] cost = {{}};
	private int[][] latency = {{}};
	
	public DeviceAssignments(ArrayList<Cloudlet> cloudlets, ArrayList<CandidatePoint> points, 
			ArrayList<EndDevice> devices, int[][] cost, int[][] latency) {
		// TODO Auto-generated constructor stub
		this.C = cloudlets;
		this.P = points;
		this.E = devices;
		this.cost = cost;
		this.latency = latency;
	}
	
	/*
	public static void main(String[] args) {
		
		//5 cloudlets as per our optimization example
		int num_cloudlets = 5;
		//25 end devices as per our optimization example
		int num_devices = 25;
		//7 candidate points as per our optimization example
		int num_candidates = 7;
		
		ReadCSV reader = new ReadCSV();
		int[][] cloudlet_specs = reader.getCloudlets(num_cloudlets);
		int[][] device_specs = reader.getDevices(num_devices);
		int[][] cand_points = reader.getPoints(num_candidates);
		
		
		ArrayList<Cloudlet> cloudlets = new ArrayList<Cloudlet>();
		for(int i = 0; i < cloudlet_specs.length; i++) {
			cloudlets.add(new Cloudlet(cloudlet_specs[i][0], cloudlet_specs[i][1], 
					cloudlet_specs[i][2], cloudlet_specs[i][3], cloudlet_specs[i][4]));
		}
		
		ArrayList<EndDevice> devices = new ArrayList<EndDevice>();
		for(int i = 0; i < device_specs.length; i++) {
			devices.add(new EndDevice(device_specs[i][0], device_specs[i][1],
					device_specs[i][2], device_specs[i][3], device_specs[i][4], device_specs[i][5]));
		}
		
		ArrayList<CandidatePoint> points = new ArrayList<CandidatePoint>();
		for(int i = 0; i < cand_points.length; i++) {
			points.add(new CandidatePoint(cand_points[i][0], cand_points[i][1], cand_points[i][2]));
		}
		
		//Cost Matrix
		int[][] cost = reader.getCosts(num_cloudlets, num_candidates);
		
		//Latency Matrix
		int[][] latency = reader.getLatencies(num_devices, num_candidates);
		
		//System.out.println(num_large + " " + num_medium + " " + num_small);
		
		Cloudlet[] cloudlet_placement = new Cloudlet[num_candidates];
		//manually place cloudlets here
		cloudlet_placement[0] = cloudlets.get(1);
		cloudlet_placement[2] = cloudlets.get(2);
		cloudlet_placement[6] = cloudlets.get(4);
		
		DeviceAssignments device_assignment = new DeviceAssignments(cloudlets, points, devices, latency, latency);
		
		long startTime = System.nanoTime();
		
		device_assignment.fastFailGreedyAssignment(cloudlet_placement);
		
		long endTime = System.nanoTime();
		
		long duration = (endTime - startTime)/1000000;
		
		System.out.println("\nTime = " + duration + " ms");
		
	}*/
	
	
	public void fastFailGreedyAssignment(Cloudlet[] cloudlet_placement) {
		
		System.out.println(Arrays.toString(cloudlet_placement));
		
		EndDevice[][] device_groups = new EndDevice[P.size()][E.size()];
		Integer[] group_totals = new Integer[P.size()]; //1 candidate point = 1 group
		Arrays.fill(group_totals, 0);
		//Group devices that can be served from a candidate point.
		for(int i = 0; i < P.size(); i++) {
			Cloudlet x = cloudlet_placement[i];
			//if there is a cloudlet in that location
			if(x != null) {
				for(int j = 0; j < E.size(); j++) {
					double d = distance(P.get(i).xlocation, P.get(i).ylocation, 
							E.get(j).xlocation, E.get(j).ylocation);
					if(d <= x.radius ) {
						device_groups[i][j] = E.get(j);
						group_totals[i]++;
					}
				}
			}
		}
		
		System.out.println();
		for(int i = 0; i < P.size(); i++) {
			for(int j = 0; j < E.size(); j++) {
				if(device_groups[i][j] == null) {
					System.out.print("N ");
				}
				else {
					System.out.print(device_groups[i][j].id + " ");
				}
			}
			System.out.print(":"+ group_totals[i]);
			System.out.println();
		}
		
		Integer[] device_counts = new Integer[E.size()];
		Arrays.fill(device_counts, 0);
		//For every user, keep count of groups it occurs in
		for(int j = 0; j < E.size(); j++) {
			for(int i = 0; i < P.size(); i++) {
				if(device_groups[i][j] != null) {
					device_counts[j]++;
				}
			}
			//return as soon it is found a device cannot be
			//assigned to any cloudlet at all
			if(device_counts[j] == 0) {
				System.out.println("Infeasible");
				return;
			}
		}
		
		System.out.println(this.isFeasible(device_counts)?"Feasible":"Infeasible");
		
		int[] devices = new int[E.size()];
		Arrays.fill(devices, -1);
		double covered = 0;
		
		int[] processor = new int[cloudlet_placement.length];
		int[] memory = new int[cloudlet_placement.length];
		int[] storage = new int[cloudlet_placement.length];
		
		//copy of the cloudlet specifications so that
		//they do get reset for next coverage maximization
		for(int j = 0; j < cloudlet_placement.length; j++) {
			if(cloudlet_placement[j] != null) {
				processor[j] = cloudlet_placement[j].processor;
				memory[j] = cloudlet_placement[j].memory;
				storage[j] = cloudlet_placement[j].storage;
			}
		}
		
		//Sort devices from least group memberships to most
		ArrayIndexComparator comparator = new ArrayIndexComparator(device_counts);
		Integer[] device_indexes = comparator.createIndexArray();
		Arrays.sort(device_indexes, comparator);
		System.out.println(Arrays.toString(device_indexes));
		
		//Sort groups from most feasible members to least
		ArrayIndexComparator comparator2 = new ArrayIndexComparator(group_totals);
		Integer[] group_indexes = comparator2.createIndexArray();
		Arrays.sort(group_indexes, comparator2);
		System.out.println(Arrays.toString(group_indexes));
		
		//For each group starting with most total devices to least
		for(int i = group_indexes.length - 1; i >= 0; i--) {
			int group_index = group_indexes[i];
			Cloudlet cloudlet = cloudlet_placement[group_index];
			if(cloudlet != null) {
				//For each device starting with least group to most
				for(int j = 0; j < device_indexes.length; j++) {
					int device_index = device_indexes[j];
					//Assign if it meets capacity constraints
					if(inRangeAndCapacity(group_index, processor, memory, storage, 
							cloudlet, E.get(device_index)) && devices[device_index] == -1) {
						covered++;
						processor[group_index] -= E.get(device_index).processor;
						memory[group_index] -= E.get(device_index).memory;
						storage[group_index] -= E.get(device_index).storage;
						devices[device_index] = group_index;
					}
				}
			}
			
			
		}
		
		System.out.println(Arrays.toString(devices));
		System.out.println("Coverage: " + covered/E.size());
		
	}
	
	private boolean inRangeAndCapacity(int point, int[] processor, int[] storage, int[] memory, Cloudlet c1, EndDevice endDevice) {
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
	
	//for checking if assignment is still feasible in every step
	public boolean isFeasible(Integer[] device_counts) {
		System.out.println();
		for(int j = 0; j < device_counts.length; j++) {
			if(device_counts[j] == 0) {
				return false;
			}
			System.out.print(device_counts[j] + " ");
		}
		System.out.println();
		return true;
	}
	
	public double distance(int x1, int y1, int x2, int y2) {
		int y_diff = y2-y1;
		int x_diff = x2-x1;
		
		double x_sqr = Math.pow(x_diff, 2);
		double y_sqr = Math.pow(y_diff, 2);
		
		double dist = Math.sqrt(x_sqr + y_sqr);
		
		return dist;
	}

}
