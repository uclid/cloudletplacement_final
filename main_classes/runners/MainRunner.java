package runners;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import data_folder_reader.ReadDataFolder;
import base.*;
import extended.*;
import algorithm.*;

public class MainRunner {
	
	public static ArrayList<String> run(int approach, ArrayList<Cloudlet> cloudlets, ArrayList<CandidatePoint> points,
			ArrayList<EndDevice> devices, int[][] cost, int[][] latency, CplexLPCloudletPlacement lp_solve) {
		
		ArrayList<String> summary = new ArrayList<String>();
		
		//run only if LP is feasible
		//this is a prerequisite for all approaches
		
		if(approach == 1) {
			//runcplex: mode = cost
			boolean mode = true; //true means cost
			
			//timing the CPLEX run
			long startTime = System.nanoTime();
			CplexCloudletPlacement place = new CplexCloudletPlacement();
			place.cplexModel(cloudlets, points, devices, cost, latency, mode);
			long endTime = System.nanoTime();
			
			long duration = (endTime - startTime)/1000;
			
			summary.add(place.method);
			summary.add(place.solution_cost + "");
			summary.add(place.solution_latency + "");
			summary.add(duration+"");
			
			//System.out.println("\nTime = " + duration + " microsec");
		}
		else if(approach == 2) {
			//runcplex: mode = latency
			boolean mode = false; //false means latency
			
			//timing the CPLEX run
			long startTime = System.nanoTime();
			CplexCloudletPlacement place = new CplexCloudletPlacement();
			place.cplexModel(cloudlets, points, devices, cost, latency, mode);
			long endTime = System.nanoTime();
			
			long duration = (endTime - startTime)/1000;
			
			summary.add(place.method);
			summary.add(place.solution_cost + "");
			summary.add(place.solution_latency + "");
			summary.add(duration + "");

		}
		else if(approach == 3) {
			//run ACP
			//create instances of new extended types used in ACP
			ArrayList<NewCloudlet> new_cloudlets = new ArrayList<NewCloudlet>();
			ArrayList<NewEndDevice> new_devices = new ArrayList<NewEndDevice>();
			ArrayList<NewCandidatePoint> new_points = new ArrayList<NewCandidatePoint>();
			
			int cloudlets_size = cloudlets.size();
			int devices_size = devices.size();
			int points_size = points.size();
			
			for(int i = 0; i < cloudlets_size; i++) {
				Cloudlet c = cloudlets.get(i);
				new_cloudlets.add(new NewCloudlet(c.id,c.processor,c.memory,c.storage,c.radius,c.type));
			}
			
			for(int i = 0; i < devices_size; i++) {
				EndDevice e = devices.get(i);
				new_devices.add(new NewEndDevice(e.id, e.processor, e.memory, e.storage, e.xlocation, e.ylocation));
				
			}

			for(int i = 0; i < points_size; i++) {
				CandidatePoint p = points.get(i);
				new_points.add(new NewCandidatePoint(p.id, p.xlocation, p.ylocation));
			}
			
			//simply pass new instances with the LP solution
			// timing the ACP run
			long startTime = System.nanoTime();
			ApproxLPRounding approx_algo = new ApproxLPRounding();
			approx_algo.approximate(lp_solve.a_s, new_devices, new_points, new_cloudlets, cost, latency);
			long endTime = System.nanoTime();
			
			long duration = (endTime - startTime) / 1000;

			// System.out.println(Arrays.toString(place.y_s));
			// System.out.println(Arrays.toString(place.a_s));
			summary.add(approx_algo.method);
			summary.add(approx_algo.solution_cost + "");
			summary.add(approx_algo.solution_latency + "");
			summary.add(duration + "");
			
		}
		else {
			//run GACP
			//specify the number of cloudlet placements in
			//initial population, 10 for now
			int assignment_size = 10;
			//threshold value for coverage, 95% for now
			//100% for final experiments
			double threshold = 1.0;
			
			//use LP solution to guide, cost and number of cloudlets used
			int lp_cost = lp_solve.solution_cost;
			int num_cloudlets = lp_solve.cloudlets_used;
			
			long startTime = System.nanoTime();
			GeneticCloudletPlacement place = new GeneticCloudletPlacement(cloudlets, points, devices, cost, latency);
			
			place.geneticAlgorithm(assignment_size, threshold, lp_cost, num_cloudlets);
			long endTime = System.nanoTime();
			
			long duration = (endTime - startTime)/1000;
			summary.add(place.method);
			summary.add(place.solution_cost + "");
			summary.add(place.solution_latency + "");
			summary.add(duration + "");
			//System.out.println("Time = " + duration + " microsec");

		}
		
		return summary;
		
	}
	
	public static void main(String[] args) {
		
		//to hold the results of all runs
		ArrayList<ArrayList<String>> results_summary = new ArrayList<ArrayList<String>>();
		
		for(int i = 12; i <= 12; i++) {
		//Need to solve LP before anything
		String datasetPath = "datasets/staten_island/samples/"  + i + "/";
		
		//create all the objects needed for the approaches
		//cloudlets, points, devices, cost and latency
		ReadDataFolder dataset_reader = new ReadDataFolder();
		
		ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
		ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
		ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
		int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
		int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
		
		//System.out.println(cloudlets + "\n" + devices + "\n" + points);
		
		//Solve the LP to test feasibility
		//if not solved, then do not proceed 
		//with any of the approaches
		CplexLPCloudletPlacement lp_solve = new CplexLPCloudletPlacement();
		//results: 0 = cost, 1 = num_cloudlets_placed, 2 = latency
		lp_solve.cplexModel(cloudlets, points, devices, cost, latency);
		
		
		//System.out.println("Cost: " + lp_solve.solution_cost + " Latency: " + lp_solve.solution_latency 
				//+ " Cloudlets Placed: " + lp_solve.cloudlets_used);
		//System.out.println(lp_solve.a_s);
		
		//to run an approach with datasetPath
		//1. CPLEX OCP Model Cost
		//2. CPLEX OCP Model Latency
		//3. ACP
		//4. GACP
		//Give path as "dataset/folder/subfolder/"
		
		results_summary.add(run(4, cloudlets, points, devices, cost, latency, lp_solve));
		
		//System.out.println("Points: " + points.size() + " Devices: " + devices.size());
		//for(ArrayList<String> s: results_summary) {
			//System.out.println(s);
		//}
		}
		//writing the values to files
        String resultFile = "c:/users/dixit/desktop/result.csv";
        
        FileWriter writer;
		try {
			writer = new FileWriter(resultFile);
		
	        int file_size = results_summary.size();
			for(int i = 0; i < file_size; i++) {
				ArrayList<String> dummy = results_summary.get(i);
				for(String s: dummy) {
					writer.write(s + ",");
				}
				writer.write(System.lineSeparator());
	    	}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}