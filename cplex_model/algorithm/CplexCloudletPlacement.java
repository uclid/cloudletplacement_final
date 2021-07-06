package algorithm;
import java.util.ArrayList;
import base.*;

import ilog.concert.*;
import ilog.cplex.*;

public class CplexCloudletPlacement {
	
	public String method = "OCP";
	public int solution_cost = 0;
	public int solution_latency = 0;
	
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
	 * @param mode 
	 */
	public void cplexModel(ArrayList<Cloudlet> C, ArrayList<CandidatePoint> P , 
			ArrayList<EndDevice> E, int[][] cost, int[][] latency, boolean mode) {
		
		int w = C.size();
		int n = P.size();
		int v = E.size();
		
		try {
			//new model object
			IloCplex model = new IloCplex();
			//set node limits for larger instances of OCP Cost
			//here based on number of devices
			if(mode == true && E.size() > 1000) { //Manhattan
				model.setParam(IloCplex.Param.MIP.Limits.Nodes, 5500);
			}
			else if(mode == true && E.size() > 400){ //Brooklyn
				model.setParam(IloCplex.Param.MIP.Limits.Nodes, 30000);
			}
			else if(mode == true && E.size() > 300) { //Queens
				model.setParam(IloCplex.Param.MIP.Limits.Nodes, 70000);
			}
			else if(mode == true && E.size() > 100) { //Bronx
				model.setParam(IloCplex.Param.MIP.Limits.Nodes, 75000);
			}
			//limit not required for staten island
			
			//the decision variable y_{jk}
			IloIntVar[][] y = new IloIntVar[w][n];
			//specifying range for the decision variable, 0 or 1
			for(int j = 0; j < w; j++) {
				for(int k = 0; k < n; k++) {
					y[j][k] = model.boolVar();
				}
			}
			
			//the decision variable a_{ik}
			IloIntVar[][] a = new IloIntVar[v][n];
			//specifying range for the decision variable, 0 or 1
			for(int i = 0; i < v; i++) {
				for(int k = 0; k < n; k++) {
					a[i][k] = model.boolVar();
				}
			}
			
			//objective
			IloLinearNumExpr obj = model.linearNumExpr(); 
			
			//if mode is true, objective is cost
			if(mode) {
				this.method += " Cost";
				for(int j = 0; j < w; j++){ 
					for(int k = 0; k < n; k++) { 
						obj.addTerm(y[j][k], cost[j][k]); 
					} 
				}
			}
			else { //minimize latency in false mode
				this.method += " Latency";
				for(int i = 0; i < v; i++) {
					for(int k = 0; k < n; k++) {
						obj.addTerm(a[i][k], latency[i][k]);
					}
				}
			}
			
			//add objective to model for minimization
			model.addMinimize(obj);
			
			//objective functions, enable only one of them for solving
			//cost minimization
			/*
			IloLinearNumExpr cost_obj = model.linearNumExpr(); 
			for(int j = 0; j < w; j++){ 
				for(int k = 0; k < n; k++) { 
					cost_obj.addTerm(y[j][k], cost[j][k]); 
				} 
			}*/
			 
			
			//latency minimization
			/*
			IloLinearNumExpr latency_obj = model.linearNumExpr();
			for(int i = 0; i < v; i++) {
				for(int k = 0; k < n; k++) {
					latency_obj.addTerm(a[i][k], latency[i][k]);
				}
			}*/
			
			//minimize the objective function
			//model.addMinimize(cost_obj);
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
			 * Optional constraints for sensitivity analysis
			 * */
			//threshold value for cost, used when minimizing latency.
			//sum(c in C) (sum (p in CandidatePoints) Cost[c][p] * select[c][p]) <= 14;
			/*IloLinearNumExpr sum_cost = model.linearNumExpr();
			for(int j = 0; j < w; j++) {
				for(int k=0; k < n; k++) {
					sum_cost.addTerm(y[j][k], cost[j][k]);
				}
				model.addLe(sum_cost, 127);
			}*/
		    
		    //threshold value for latency, used when minimizing cost
		    //sum(e in E) (sum (p in CandidatePoints) Latency[e][p] * select_end[e][p]) <= 190;
			/*IloLinearNumExpr sum_latency = model.linearNumExpr();
			for(int i = 0; i < v; i++) {
				for(int k=0; k < n; k++) {
					sum_latency.addTerm(a[i][k], latency[i][k]);
				}
				model.addLe(sum_latency, 48);
			}*/
		
			/*
			 * Now towards solving the model
			 * */
			boolean isSolved = model.solve();
			if(isSolved) {
				double objValue = model.getObjValue();
				double costVal = 0.0;
				double latVal = 0.0;
				//System.out.println("\nObjective value is: " + objValue);
				//System.out.print("\nCloudlet Assignments\n");
				for(int j = 0; j < w; j++) {
					for(int k = 0; k < n; k++) {
						if(model.getValue(y[j][k]) >= 0.99) {
							//System.out.print(" y[" + j + "][" + k + "] = " + model.getValue(y[j][k]));
							costVal += cost[j][k]*model.getValue(y[j][k]);
							//System.out.println("\t" + cost[j][k]);
						}
					}
					//System.out.println("\n");
				}
				//System.out.println("\nCost: " + costVal);
				this.solution_cost = (int)Math.round(costVal);
				//System.out.print("\nDevice Assignments\n");
				for(int i = 0; i < v; i++) {
					for(int k=0; k < n; k++) {
						if(model.getValue(a[i][k]) >= 0.99) {
							//System.out.print(" a[" + i + "][" + k + "] = " + model.getValue(a[i][k]));
							latVal += latency[i][k]*model.getValue(a[i][k]);
							//System.out.println("\t" + latency[i][k]);
						}
					}
					//System.out.println("\n");
				}
				//System.out.println("\nLatency: " + latVal);
				this.solution_latency = (int)Math.round(latVal);
				
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
}
