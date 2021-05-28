package algorithm;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;
import base.*;

import comparator.AssignmentsComparator;

public class GeneticCloudletPlacement {

	private ArrayList<Cloudlet> C = null;
	private ArrayList<CandidatePoint> P = null;
	private ArrayList<EndDevice> E = null;
	private int[][] cost = {{}};
	private int[][] latency = {{}};
	private int estimate_optimal_cost = 0;
	private int min_needed_cloudlets = 0;
	
	private GeneticMethods geneticMethods = null;
	
	public String method = "GACP";
	public int solution_cost = 0;
	public int solution_latency = 0;

	public GeneticCloudletPlacement(ArrayList<Cloudlet> cloudlets, ArrayList<CandidatePoint> points, 
			ArrayList<EndDevice> devices, int[][] cost, int[][] latency) {
		// TODO Auto-generated constructor stub
		this.C = cloudlets;
		this.P = points;
		this.E = devices;
		this.cost = cost;
		this.latency = latency;
		
		this.geneticMethods = new GeneticMethods(C,P,E,cost,latency);
		
	}

	/**
	 * @author Dixit Bhatta
	 * The method takes the decision variables, constraints,
	 * and solution arrays as arguments and displays the
	 * solution if there a feasible one.
	 * @param C set of cloudlets
	 * @param P set of candidate points
	 * @param V set of end devices
	 * @param cost placement cost matrix
	 * @param latency latency matrix
	 * @param threshold 
	 * @param num_cloudlets 
	 * @param lp_cost 
	 */
	public void geneticAlgorithm(int assignment_size, double threshold, int lp_cost, int num_cloudlets) {
		
		int n = P.size();
		int v = E.size();
		int m = assignment_size;
		HashMap<Cloudlet[], Double> cover_map = new HashMap<Cloudlet[], Double>();
		HashMap<Cloudlet[], Integer> fitness_map = new HashMap<Cloudlet[], Integer>();
		
		//CplexLPCloudletPlacement place = new CplexLPCloudletPlacement();
		//int[] results = place.cplexModel(C, P, E, cost, latency);
		estimate_optimal_cost = lp_cost;
		min_needed_cloudlets = num_cloudlets;
		System.out.println("LP Optimal cost: " + estimate_optimal_cost + " Placed Cloudlets: " + min_needed_cloudlets);
		
		//variable holding assignments for the cloudlets
		Cloudlet[][] cloudlets = new Cloudlet[m][n];
		cloudlets = geneticMethods.randomAssignments(n, m, min_needed_cloudlets);
		//for(int i = 0; i < m; i++) {
			//System.out.println(Arrays.toString(cloudlets[i]));
		//}
		
		//variable holding assignment for devices
		int[] devices = new int[v];
		devices = geneticMethods.deviceAssignments();
		//int null_counter = 0;
		
		//System.out.println(Arrays.toString(devices));
		do {
			ArrayList<Cloudlet[]> B = new ArrayList<Cloudlet[]>();
			PriorityQueue<Cloudlet[]> pq = new PriorityQueue<Cloudlet[]>(m, new AssignmentsComparator(cost));
			for(int i = 0; i < m; i++) {
				pq.add(cloudlets[i]);
			}
			
			while(B.size() <= m) {
				Cloudlet[] c1 = new Cloudlet[n];
				Cloudlet[] c2 = new Cloudlet[n];
				Cloudlet[] a1 = new Cloudlet[n];
				Cloudlet[] a2 = new Cloudlet[n];
				
				if(!pq.isEmpty()) {
					c1 = pq.remove();
				}
				else {
					c1 = geneticMethods.oneRandomCloudletAssignment(min_needed_cloudlets);
				}
				if(!pq.isEmpty()) {
					c2 = pq.remove();
				}
				else {
					c2 = geneticMethods.oneRandomCloudletAssignment(min_needed_cloudlets);
				}
				//System.out.println(Arrays.toString(pq.remove()));
				
				
				//for crossover probability
				Random rand = new Random();
				int x = rand.nextInt(10);
				//System.out.println(x);
				
				/*System.out.println("Before Crossover");
				System.out.println("c1 " + Arrays.toString(c1) + "= " + fitness(c1, cost));
				System.out.println("c2 " + Arrays.toString(c2) + "= " + fitness(c2, cost));*/
				
				//crossover probability is 0.5 for now
				if(x >= 5) {
					//System.out.println("Crossover happend!");
					a1 = geneticMethods.crossOver(c1.clone(),c2.clone())[0];
					a2 = geneticMethods.crossOver(c1.clone(),c2.clone())[1];
				}
				else {
					a1 = c1.clone();
					a2 = c2.clone();
				}
	
				/*System.out.println("\nAfter Crossover, before mutation");
				System.out.println("a1 " + Arrays.toString(a1)+ "= " + fitness(a1, cost));
				System.out.println("a2 " + Arrays.toString(a2)+ "= " + fitness(a2, cost));*/
				
				//for mutation probability
				x = rand.nextInt(10);
				
				//mutation probability is 0.1
				if(x < 1) {
					a1 = geneticMethods.mutate(a1);
					a2 = geneticMethods.mutate(a2);
				}
				
				/*System.out.println("\nAfter mutation");
				System.out.println("a1 " + Arrays.toString(a1)+ "= " + fitness(a1, cost));
				System.out.println("a2 " + Arrays.toString(a2)+ "= " + fitness(a2, cost));
				System.out.println("c1 " + Arrays.toString(c1) + "= " + fitness(c1, cost));
				System.out.println("c2 " + Arrays.toString(c2) + "= " + fitness(c2, cost));*/
				
				int c1_fit = geneticMethods.fitness(c1, estimate_optimal_cost);
				int c2_fit = geneticMethods.fitness(c2, estimate_optimal_cost);
				int a1_fit = geneticMethods.fitness(a1, estimate_optimal_cost);
				int a2_fit = geneticMethods.fitness(a2, estimate_optimal_cost);
				
				int fC = Math.min(c1_fit, c2_fit);
				int fA = Math.min(a1_fit, a2_fit);
				
				//System.out.println(fC + " " + fA);
				
				/*System.out.println("\nBefore coverage");
				System.out.println("a1 " + Arrays.toString(a1) + " = " + coverage(a1.clone(), devices, E, P));
				System.out.println("a2 " + Arrays.toString(a2) + " = " + coverage(a2.clone(), devices, E, P));
				System.out.println("c1 " + Arrays.toString(c1) + " = " + coverage(c1.clone(), devices, E, P));
				System.out.println("c2 " + Arrays.toString(c2) + " = " + coverage(c2.clone(), devices, E, P));*/
				
				//System.out.println("1" + Arrays.toString(devices));
				double c1_cover = geneticMethods.maxCoverage(devices, c1.clone());
				//System.out.println("2" + Arrays.toString(devices));
				double c2_cover = geneticMethods.maxCoverage(devices, c2.clone());
				//System.out.println("3" + Arrays.toString(devices));
				double a1_cover = geneticMethods.maxCoverage(devices, a1.clone());
				double a2_cover = geneticMethods.maxCoverage(devices, a2.clone());
				//cover_map.put(c1, c1_cover);
				//cover_map.put(c2, c2_cover);
				//cover_map.put(a1, a1_cover);
				//cover_map.put(a2, a2_cover);
				//System.out.println("Here==" + cover_map.get(a1));
				/*if(fitness(c1) == 38) {
					null_counter++;
					if(null_counter > 3)
						System.exit(0);
				}*/
				
				double Vc = Math.min(c1_cover, c2_cover);
				double Va = Math.min(a1_cover, a2_cover);
				
				
				/*System.out.println("\nAfter coverage");
				System.out.println("a1 " + Arrays.toString(a1) + " = " + (fitness(a1) + this.estimate_optimal_cost) + " " + maxCover(devices, a1.clone()));
				System.out.println("a2 " + Arrays.toString(a2) + " = " + (fitness(a2) + this.estimate_optimal_cost) + " " + maxCover(devices, a2.clone()));
				System.out.println("c1 " + Arrays.toString(c1) + " = " + (fitness(c1) + this.estimate_optimal_cost) + " " + maxCover(devices, c1.clone()));
				System.out.println("c2 " + Arrays.toString(c2) + " = " + (fitness(c2) + this.estimate_optimal_cost) + " " + maxCover(devices, c2.clone()));
				System.out.println(Vc + " " + Va);*/
				
				if((fA < fC) || ((fA == fC) && (Va >= Vc))) {
						//System.out.println(cover_map.get(a1a2[i]));
						if(a1_cover >= threshold) {
							B.add(a1);
							cover_map.put(a1, a1_cover);
							fitness_map.put(a1, a1_fit);
						}
						if(a2_cover >= threshold) {
							B.add(a2);
							cover_map.put(a2, a2_cover);
							fitness_map.put(a2, a2_fit);
						}
				}
				else {
					if(c1_cover >= threshold) {
						B.add(c1);
						cover_map.put(c1, c1_cover);
						fitness_map.put(c1, c1_fit);
					}
					if(c2_cover >= threshold) {
						B.add(c2);
						cover_map.put(c2, c2_cover);
						fitness_map.put(c2, c2_fit);
					}
				}
			}
			//System.out.println(B.toString());
			Cloudlet[][] temp = new Cloudlet[B.size()][n];
			for(int i = 0; i<B.size(); i++) {
				temp[i] = B.get(i);
			}
			cloudlets = temp;
			/*System.out.println("\nBest so far");
			for(int i = 0; i<B.size(); i++) {
				System.out.println(Arrays.toString(B.get(i)) + " " + fitness(B.get(i), cost) + " " + cover_map.get(B.get(i)));
			}*/
			
		} while(!underThreshold(cloudlets, cover_map, threshold));
		
		//System.out.println(Arrays.toString(devices));
		
		//int[][] devices_new = new int[cloudlets.length][v];
		//devices_new = maximizeCover(devices, cloudlets);
		//System.out.println(Arrays.toString(devices));
		
		//int index = selectLeastLatency(devices_new, cloudlets);
		int index = geneticMethods.selectLeastCost(cloudlets);
		int[] devices_max = new int[v];
		devices_max = geneticMethods.maxCoverageIndexes(devices, cloudlets[index]);
		//this.final_cost = totalCost(cloudlets[index]);
		geneticMethods.finalValues(index, devices, devices_max, cloudlets[index]);
		this.solution_cost = geneticMethods.getCost();
		this.solution_latency = (int)Math.round(geneticMethods.getLatency());
		System.out.println(index + ">" + Arrays.toString(cloudlets[index]) + " " + 
				this.solution_cost+ " " + geneticMethods.getCoverage() + " " 
				+ this.solution_latency + "\n" + index + ">" + Arrays.toString(devices_max));
	}

	/*private boolean underFitnessThreshold(Cloudlet[][] cloudlets, HashMap<Cloudlet[], Integer> fitness_map) {
		for(Cloudlet[] c: cloudlets) {
			if(fitness_map.get(c) > 5) {
				return false;
			}
		}
		return true;
	}*/

	/**
	 * Returns if the coverage values for each clouldet assignment
	 * is under the specified threshold value
	 * @param cloudlets set of cloudlets i.e. clouldet assignments/placement
	 * @param cover_map coverage values stored in the HashMap
	 * @param threshold coverage threhold value
	 */
	private boolean underThreshold(Cloudlet[][] cloudlets, HashMap<Cloudlet[], Double> cover_map, double threshold) {
		// TODO Auto-generated method stub
		for(Cloudlet[] c: cloudlets) {
			if(cover_map.get(c) < threshold) {
				return false;
			}
		}
		return true;
	}


}
