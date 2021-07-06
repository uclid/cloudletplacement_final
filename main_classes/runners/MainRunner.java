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
			ArrayList<EndDevice> devices, int[][] cost, int[][] latency, int lp_cost, int num_cloudlets, double coverage_limit) {
		
		ArrayList<String> summary = new ArrayList<String>();
		
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
			long lp_startTime = System.nanoTime();
			CplexLPCloudletPlacement lp_solve = new CplexLPCloudletPlacement();
			//results: 0 = cost, 1 = num_cloudlets_placed, 2 = latency
			lp_solve.cplexModel(cloudlets, points, devices, cost, latency);
			long lp_endTime = System.nanoTime();
			ApproxLPRounding approx_algo = new ApproxLPRounding();
			approx_algo.approximate(lp_solve.a_s, new_devices, new_points, new_cloudlets, cost, latency);
			long endTime = System.nanoTime();
			
			long lp_duration = (lp_endTime - lp_startTime) / 1000;
			long duration = (endTime - startTime) / 1000;

			// System.out.println(Arrays.toString(place.y_s));
			// System.out.println(Arrays.toString(place.a_s));
			summary.add(approx_algo.method);
			summary.add(approx_algo.solution_cost + "");
			summary.add(approx_algo.solution_latency + "");
			summary.add(duration + "");
			summary.add(lp_duration + "");
			
		}
		else {
		
			//run GACP
			//specify the number of cloudlet placements in
			//initial population, 10 for now
			int assignment_size = 10;
			//threshold value for coverage, 95% for now
			//100% for final experiments
			double threshold = coverage_limit;
			
			long startTime = System.nanoTime();
			GeneticCloudletPlacement place = new GeneticCloudletPlacement(cloudlets, points, devices, cost, latency);
			
			place.geneticAlgorithm(assignment_size, threshold, lp_cost, num_cloudlets);
			long endTime = System.nanoTime();
			
			long duration = (endTime - startTime)/1000;
			summary.add(place.method);
			summary.add(place.solution_cost + "");
			summary.add(place.solution_latency + "");
			summary.add(duration + "");
			summary.add(place.solution_coverage + "");
			//System.out.println("Time = " + duration + " microsec");
		}
		
		return summary;
		
		
	}
	
	public static void main(String[] args) {
		
		//runACP();
		//runGACP();
		//runOCPLatency();
		runOCPCost();
		
		
	}
	
	public static void runACP() {
		//to hold the results of all runs
		ArrayList<ArrayList<String>> results_summary = new ArrayList<ArrayList<String>>();
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/staten_island/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(3, cloudlets, points, devices, cost, latency, 0, 0, 0.0);
			solution.add("Staten Island");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
			
		}
		
		System.out.println("Method  Cost  Latency  Runtime LP Time"
				+ "  Dataset  Sample No.  Points  Devices "
				+ "LP Cost Cloudlets Placed");
		//writing results to console
		int file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		
		file_size = results_summary.size();
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/bronx/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(3, cloudlets, points, devices, cost, latency, 0, 0, 0.0);
			solution.add("Bronx");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
		}
		
		//writing results to console
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/queens/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(3, cloudlets, points, devices, cost, latency, 0, 0, 0.0);
			solution.add("Queens");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
			
		}
		
		results_summary.clear();
		
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/brooklyn/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(3, cloudlets, points, devices, cost, latency, 0, 0, 0.0);
			solution.add("Brooklyn");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);

		}
		
		results_summary.clear();
		
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/manhattan/samples/"  + i + "/";
			
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(3, cloudlets, points, devices, cost, latency, 0, 0, 0.0);
			solution.add("Manhattan");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
			
		}
		
		results_summary.clear();
		
		//writing results to console
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
	}
	
	public static void runGACP() {
		//to hold the results of all runs
		ArrayList<ArrayList<String>> results_summary = new ArrayList<ArrayList<String>>();
		
		//LP solutions created here and provided as
		//input like this to save experiment time
		//Add lp_solve time from ACP to GACP runtime
		int[][] lp_solutions = {
				{2400,4},{2460,4},{2357,5},{2246,4},{2356,5},
				{2355,5},{2301,5},{2651,6},{2669,6},{2356,4},
				{2538,6},{2419,6},{2498,6},{2511,5},{2384,4},
				{2436,6},{2504,5},{2358,5},{2486,5},{2546,4},
				{2425,6},{2267,6},{2618,5},{2248,4},{2626,6},
				{2398,6},{2561,6},{2724,5},{2733,5},{2506,4},
				
				{8131,19},{8001,19},{7722,19},{8365,19},{8468,19},
				{8219,17},{8314,21},{8391,19},{8604,20},{8234,19},
				{8759,19},{8909,19},{8448,19},{8059,19},{8391,17},
				{9017,22},{8096,17},{8208,19},{8423,20},{8330,21},
				{8371,21},{8116,20},{7964,19},{8119,19},{7644,17},
				{8157,20},{7970,17},{8049,19},{8288,19},{8079,17},
				
				{13551,33},{13827,30},{13835,33},{13936,33},{13801,32},
				{13688,32},{13651,33},{12943,33},{13615,31},{13975,34},
				{13733,30},{13940,30},{13794,33},{13770,34},{13698,33},
				{12953,36},{13561,30},{13319,32},{13548,34},{13642,33},
				{13609,31},{14110,33},{13714,30},{13498,33},{13723,33},
				{13957,33},{13517,33},{13990,30},{14032,32},{13344,30},
				
				{17425,45},{17619,44},{17346,44},{17928,48},{17970,47},
				{17476,48},{18139,44},{18264,47},{17965,43},{17717,48},
				{17372,43},{17559,46},{17886,45},{17626,47},{17891,46},
				{18002,46},{17842,45},{17767,45},{17161,44},{17880,44},
				{17657,47},{18331,47},{17582,50},{18139,49},{17391,45},
				{17928,48},{17633,46},{17788,47},{17581,42},{17777,46},
				
				{42315,69},{41133,72},{41514,69},{40911,71},{41919,74},
				{41360,69},{41460,63},{39933,69},{41655,76},{41013,65},
				{41227,68},{41235,78},{40778,67},{41565,76},{41669,76},
				{41970,69},{41532,71},{41680,73},{42420,62},{41230,58},
				{42249,67},{41976,74},{41633,71},{42316,80},{41812,63},
				{41665,76},{40867,62},{41944,71},{41817,65},{41790,76}				
		};
		
		double[] thresholds = {
					1.0,1.0,0.90,0.95,0.95,0.95,0.95,0.95,0.95,1.0,
					0.95,0.95,0.95,0.95,1.0,0.95,1.0,0.95,0.95,1.0,
					0.95,1.0,0.95,0.95,0.95,0.95,0.95,0.95,0.95,0.95,
					
					0.92,0.92,0.92,0.92,0.92,0.92,0.92,0.92,0.92,0.92,
					0.92,0.92,0.92,0.92,0.92,0.92,0.92,0.92,0.92,0.92,
					0.92,0.92,0.92,0.92,0.92,0.92,0.92,0.92,0.92,0.92,
					
					0.90,0.90,0.90,0.90,0.90,0.90,0.90,0.90,0.90,0.90,
					0.90,0.90,0.90,0.90,0.90,0.90,0.90,0.90,0.90,0.90,
					0.90,0.90,0.90,0.90,0.90,0.90,0.90,0.90,0.90,0.90,
					
					0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,
					0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,
					0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,
					
					0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,
					0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,
					0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87,0.87
				};
		
		for(int i = 1; i <= 30; i++) {
		
			String datasetPath = "datasets/staten_island/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(4, cloudlets, points, devices, cost, latency, lp_solutions[i-1][0], lp_solutions[i-1][1], thresholds[i-1]);
			solution.add("Staten Island");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
			
		}
		
		System.out.println("Method  Cost  Latency  Runtime"
				+ "  Dataset  Sample No.  Points  Devices  LP Time "
				+ "LP Cost Cloudlets Placed");
		//writing results to console
		int file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		
		file_size = results_summary.size();
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/bronx/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(4, cloudlets, points, devices, cost, latency, lp_solutions[30+i-1][0], lp_solutions[30+i-1][1], thresholds[30+i-1]);
			solution.add("Bronx");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
		}
		
		//writing results to console
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/queens/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(4, cloudlets, points, devices, cost, latency, lp_solutions[60+i-1][0], lp_solutions[60+i-1][1], thresholds[60+i-1]);
			solution.add("Queens");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
			
		}
		
		results_summary.clear();
		
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/brooklyn/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(4, cloudlets, points, devices, cost, latency, lp_solutions[90+i-1][0], lp_solutions[90+i-1][1], thresholds[90+i-1]);
			solution.add("Brooklyn");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);

		}
		
		results_summary.clear();
		
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/manhattan/samples/"  + i + "/";
			
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(4, cloudlets, points, devices, cost, latency, lp_solutions[120+i-1][0], lp_solutions[120+i-1][1], thresholds[120+i-1]);
			solution.add("Manhattan");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
			
		}
		
		results_summary.clear();
		
		//writing results to console
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}

	}
	
	public static void runOCPLatency() {
		//to hold the results of all runs
		ArrayList<ArrayList<String>> results_summary = new ArrayList<ArrayList<String>>();
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/staten_island/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(2, cloudlets, points, devices, cost, latency, 0, 0, 0.0);
			solution.add("Staten Island");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
		}
		
		System.out.println("Method  Cost  Latency  Runtime"
				+ "  Dataset  Sample No.  Points  Devices  LP Time "
				+ "LP Cost Cloudlets Placed");
		//writing results to console
		int file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		
		file_size = results_summary.size();
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/bronx/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(2, cloudlets, points, devices, cost, latency, 0, 0, 0.0);
			solution.add("Bronx");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
		}
		
		//writing results to console
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/queens/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(2, cloudlets, points, devices, cost, latency, 0, 0, 0.0);
			solution.add("Queens");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
			
		}
		
		results_summary.clear();
		
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/brooklyn/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(2, cloudlets, points, devices, cost, latency, 0, 0, 0.0);
			solution.add("Brooklyn");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);

		}
		
		results_summary.clear();
		
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/manhattan/samples/"  + i + "/";
			
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(2, cloudlets, points, devices, cost, latency, 0, 0, 0.0);
			solution.add("Manhattan");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
			
		}
		
		results_summary.clear();
		
		//writing results to console
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
	}
	
	public static void runOCPCost() {
		//to hold the results of all runs
		ArrayList<ArrayList<String>> results_summary = new ArrayList<ArrayList<String>>();
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/staten_island/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(1, cloudlets, points, devices, cost, latency, 0, 0, 0.0);
			solution.add("Staten Island");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
		}
		
		System.out.println("Method  Cost  Latency  Runtime"
				+ "  Dataset  Sample No.  Points  Devices  LP Time "
				+ "LP Cost Cloudlets Placed");
		//writing results to console
		int file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		
		file_size = results_summary.size();
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/bronx/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(1, cloudlets, points, devices, cost, latency, 0, 0, 0.0);
			solution.add("Bronx");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
		}
		
		//writing results to console
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/queens/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(1, cloudlets, points, devices, cost, latency, 0, 0, 0.0);
			solution.add("Queens");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
			
		}
		
		results_summary.clear();
		
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/brooklyn/samples/"  + i + "/";
			
			//create all the objects needed for the approaches
			//cloudlets, points, devices, cost and latency
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(1, cloudlets, points, devices, cost, latency, 0, 0, 0.0);
			solution.add("Brooklyn");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);

		}
		
		results_summary.clear();
		
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}
		
		results_summary.clear();
		
		for(int i = 1; i <= 30; i++) {
			
			String datasetPath = "datasets/manhattan/samples/"  + i + "/";
			
			ReadDataFolder dataset_reader = new ReadDataFolder();
			
			ArrayList<Cloudlet> cloudlets = dataset_reader.getCloudlets(datasetPath);
			ArrayList<EndDevice> devices = dataset_reader.getDevices(datasetPath);
			ArrayList<CandidatePoint> points = dataset_reader.getPoints(datasetPath);
			int[][] cost = dataset_reader.getCosts(datasetPath, cloudlets.size(), points.size());
			int[][] latency = dataset_reader.getLatencies(datasetPath, devices.size(), points.size());
			
			
			ArrayList<String> solution = new ArrayList<String>();
			solution = run(1, cloudlets, points, devices, cost, latency, 0, 0, 0.0);
			solution.add("Manhattan");
			solution.add(i + "");
			solution.add(points.size() + "");
			solution.add(devices.size() + "");
			
			results_summary.add(solution);
			
		}
		
		results_summary.clear();
		
		//writing results to console
		file_size = results_summary.size();
		for(int i = 0; i < file_size; i++) {
			ArrayList<String> dummy = results_summary.get(i);
			for(String s: dummy) {
				System.out.print(s + ",");
			}
			System.out.println();
    	}

	}

}
