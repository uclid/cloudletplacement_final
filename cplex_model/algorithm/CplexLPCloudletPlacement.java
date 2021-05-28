package algorithm;
import java.util.ArrayList;
import base.*;

import ilog.concert.*;
import ilog.cplex.*;

public class CplexLPCloudletPlacement {
	
	public double[][] y_s;
	public double[][] a_s;
	
	//-1 signify no solution
	public int solution_cost = -1;
	public int solution_latency = -1;
	public int cloudlets_used = -1;
	
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
	 */
	public void cplexModel(ArrayList<Cloudlet> C, ArrayList<CandidatePoint> P , 
			ArrayList<EndDevice> E, int[][] cost, int[][] latency) {
		
		int w = C.size();
		int n = P.size();
		int v = E.size();
		
		y_s = new double[w][n];
		a_s = new double[v][n];
		
		try {
			//new model object
			IloCplex model = new IloCplex();
			
			//the decision variable y_{jk}
			IloNumVar[][] y = new IloNumVar[w][n];
			//specifying range for the decision variable, 0 or 1
			for(int j = 0; j < w; j++) {
				for(int k = 0; k < n; k++) {
					y[j][k] = model.numVar(0, 1);
				}
			}
			
			//the decision variable a_{ik}
			IloNumVar[][] a = new IloNumVar[v][n];
			//specifying range for the decision variable, 0 or 1
			for(int i = 0; i < v; i++) {
				for(int k = 0; k < n; k++) {
					a[i][k] = model.numVar(0, 1);
				}
			}
			
			//objective functions, enable only one of them for solving
			//cost minimization
			IloLinearNumExpr cost_obj = model.linearNumExpr();
			for(int j = 0; j < w; j++) {
				for(int k = 0; k < n; k++) {
					cost_obj.addTerm(y[j][k], cost[j][k]);
				}
			}
			
			//latency minimization
			/*
			IloLinearNumExpr latency_obj = model.linearNumExpr();
			for(int i = 0; i < v; i++) {
				for(int k = 0; k < n; k++) {
					latency_obj.addTerm(a[i][k], latency[i][k]);
				}
			}*/
			
			//minimize the objective function
			model.addMinimize(cost_obj);
			//model.addMinimize(latency_obj);
			
			/*
			 * Adding the constraints now. Note that we are creating 
			 * each constraint individually and adding to the model
			 * */
			
			/*Constraint 1: The total number of cloudlets placed 
			 * in the grid space should be less than or equal to 
			 * number of available cloudlets.*/
			IloLinearNumExpr num_cloudlets = model.linearNumExpr();
			for(int j = 0; j < w; j++) {
				for(int k = 0; k < n; k++) {
					num_cloudlets.addTerm(y[j][k], 1);
				}
			}
			model.addLe(num_cloudlets, w);
			
			/*Constraint 2: Each end device must be within coverage
			 * range of some cloudlet.*/
			for(int i = 0; i < v; i++) {
				for(int k = 0; k < n; k++) {
					double dist = distance(E.get(i).xlocation, E.get(i).ylocation, P.get(k).xlocation, P.get(k).ylocation);
					IloLinearNumExpr radius = model.linearNumExpr();
					for(int j = 0; j < w; j++) {
						radius.addTerm(y[j][k], C.get(j).radius);
					}
					IloLinearNumExpr covered = model.linearNumExpr();
					covered.addTerm(a[i][k], dist);
					model.addLe(covered, radius);
				}
			}
			
			
			/*Constraint 3: Sum of memory demand of served end 
			 * devices should be less than or equal to serving cloudlet.*/
			for(int k = 0; k < n; k++) {
				IloLinearNumExpr devices_mem = model.linearNumExpr();
				for(int i = 0; i < v; i++) {
					devices_mem.addTerm(a[i][k], E.get(i).memory);
				}
				IloLinearNumExpr cloudlet_mem = model.linearNumExpr();
				for(int j = 0; j < w; j++) {
					cloudlet_mem.addTerm(y[j][k], C.get(j).memory);
				}
				model.addLe(devices_mem, cloudlet_mem);
			}
			
			/*Constraint 4: Sum of storage demand of served end 
			 * devices should be less than or equal to serving cloudlet.*/
			for(int k = 0; k < n; k++) {
				IloLinearNumExpr devices_stor = model.linearNumExpr();
				for(int i = 0; i < v; i++) {
					devices_stor.addTerm(a[i][k], E.get(i).storage);
				}
				IloLinearNumExpr cloudlet_stor = model.linearNumExpr();
				for(int j = 0; j < w; j++) {
					cloudlet_stor.addTerm(y[j][k], C.get(j).storage);
				}
				model.addLe(devices_stor, cloudlet_stor);
			}

			/*Constraint 5: Sum of processing demand of served end devices
			 * should be less than or equal to serving cloudlet.*/
			for(int k = 0; k < n; k++) {
				IloLinearNumExpr devices_proc = model.linearNumExpr();
				for(int i = 0; i < v; i++) {
					devices_proc.addTerm(a[i][k], E.get(i).processor);
				}
				IloLinearNumExpr cloudlet_proc = model.linearNumExpr();
				for(int j = 0; j < w; j++) {
					cloudlet_proc.addTerm(y[j][k], C.get(j).processor);
				}
				model.addLe(devices_proc, cloudlet_proc);
			}
			
			/*Constraint 6: An end device can be served from a candidate 
			 * point only if there is at least one cloudlet placed there.*/
			for(int i = 0; i < v; i++) {
				for(int k = 0; k < n; k++) {
					IloLinearNumExpr cloudlet_placed = model.linearNumExpr();
					for(int j = 0; j < w; j++) {
						cloudlet_placed.addTerm(y[j][k], 1);
					}
					model.addLe(a[i][k], cloudlet_placed);
				}
			}
			
			/*Constraint 7: At most one cloudlet can be placed at
			 * a candidate point.*/
			for(int k = 0; k < n; k++) {
				IloLinearNumExpr cloudlet_placed = model.linearNumExpr();
				for(int j=0; j < w; j++) {
					cloudlet_placed.addTerm(y[j][k], 1);
				}
				model.addLe(cloudlet_placed, 1);
			}
			
			/*Constraint 8: A cloudlet can only be placed at a 
			 * single candidate point.*/
			for(int j = 0; j < w; j++) {
				IloLinearNumExpr point = model.linearNumExpr();
				for(int k=0; k < n; k++) {
					point.addTerm(y[j][k], 1);
				}
				model.addLe(point, 1);
			}
			
			/*Constraint 9: All end devices must be served, each 
			 * from exactly one candidate point.*/
			for(int i = 0; i < v; i++) {
				IloLinearNumExpr point = model.linearNumExpr();
				for(int k=0; k < n; k++) {
					point.addTerm(a[i][k], 1);
				}
				model.addEq(point, 1);
			}
			
			/*
			 * Now towards solving the model
			 * */
			boolean isSolved = model.solve();
			if(isSolved) {
				double objValue = model.getObjValue();
				System.out.println("\nObjective value is: " + objValue);
				this.solution_cost = (int) Math.ceil(objValue);
				//System.out.print("\nCloudlet Assignments\n");
				double counter = 0;
				double total_latency = 0;
				for(int j = 0; j < w; j++) {
					for(int k=0; k < n; k++) {
						y_s[j][k] = model.getValue(y[j][k]);
						//System.out.print(" y[" + j + "][" + k + "] = " + y_s[j][k] );
						counter += model.getValue(y[j][k]);
					}
					//System.out.println("\n");
				}
				this.cloudlets_used = (int) Math.ceil(counter);
				//System.out.print("\nDevice Assignments\n");
				for(int i = 0; i < v; i++) {
					for(int k=0; k < n; k++) {
						a_s[i][k] = model.getValue(a[i][k]);
						//System.out.print(" a[" + i + "][" + k + "] = " + a_s[i][k]);
						total_latency += a_s[i][k] * latency[i][k];
					}
					//System.out.println("\n");
				}
				this.solution_latency = (int) Math.ceil(total_latency);
				
				
			}
			else {
				System.out.println("Model has not been solved!");
			}
			
		
		}
		catch(IloException e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	public double distance(int x1, int y1, int x2, int y2) {
		int y_diff = y2-y1;
		int x_diff = x2-x1;
		
		double x_sqr = Math.pow(x_diff, 2);
		double y_sqr = Math.pow(y_diff, 2);
		
		double dist = Math.sqrt(x_sqr + y_sqr);
		
		return dist;
	}
	
	/*
	public static void main(String[] args){
		
		//5 cloudlets as per our optimization example
		int num_cloudlets = 14; //14
		//25 end devices as per our optimization example
		int num_devices = 343; //343
		//7 candidate points as per our optimization example
		int num_candidates = 18; //18
		
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
		
		//timing the CPLEX run
		long startTime = System.nanoTime();
		CplexLPCloudletPlacement place = new CplexLPCloudletPlacement();
		int[] results = place.cplexModel(cloudlets, points, devices, cost, latency);
		long endTime = System.nanoTime();
		
		long duration = (endTime - startTime)/1000000;
		
		System.out.println("Objective value: " + results[0] + " " + 
		"Cloudlets deployed: " + results[1] + " Latency: " + results[2]);
		System.out.println("Time = " + duration + " ms");
		
	}*/

	
}