package dataset_creation;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CalcCandidateCosts {
	
	public static void setCloudletsAndCandidateCosts(String source_path) {
		
		String csvFile = source_path + "device.csv";
        String line = "";
        String csvSplitBy = ",";
        
        //to capture total demand
    	int total_proc = 0;
    	int total_mem = 0;
    	int total_sto = 0;
    	
    	int device_count = 0;
    	
        //get the individual capacity demands
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        	
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] item = line.split(csvSplitBy);
                
                total_proc += Integer.parseInt(item[1]);
                total_mem += Integer.parseInt(item[2]);
                total_sto += Integer.parseInt(item[3]);
                device_count++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //now get the coverage radius estimate based on distribution of
        //the latency (5 number summary: min, q1, median, q3, max)
        //one can use software like minitab to get better estimate
        //of the actual distribution and then decide the coverage radius
        csvFile = source_path + "latencies.csv";
        //hold all latencies to get summary
        ArrayList<Double> latencies = new ArrayList<Double>();
        
        //to hold number of candidates for later use
      	int num_candidates = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        	
            while ((line = br.readLine()) != null) {
            	// use comma as separator
                String[] item = line.split(csvSplitBy);
                num_candidates = item.length;
                
                for(int i = 0; i < num_candidates; i++) {
                	latencies.add(Double.parseDouble(item[i]));
                }
                
            }
        }catch (Exception e) {
        	//do nothing here, should not be any exception
        }
        
        
        //use the coverage radius estimate and total demands
        //to determine clouldet specifications
        //System.out.println(medians);
        
        //sort and find median of the median
        Collections.sort(latencies);
        //remove outliers
        ArrayList<Double> outliers = getOutliers(latencies);
        System.out.println("Outliers: " + outliers);
        latencies.removeAll(outliers);
        System.out.println("Valid latencies: " + latencies);
        int size = latencies.size();
        double min_latency = latencies.get(0);
        double max_latency = latencies.get(size-1);
        double q1 = (size % 4) != 0 ? (latencies.get(size/4) + latencies.get(size/4 - 1))/2 : latencies.get(size/4);
        double q3 = ((3*size) % 4) != 0 ? (latencies.get(3*(size/4)) + latencies.get(3*(size/4) - 1))/2 : latencies.get(3*(size/4));
        double median = getMedian(latencies);
        System.out.println("5 number summary: " + min_latency + " " + q1 + " " + median + " " + q3 + " " + max_latency);
        
        //reverse the log function to get an estimated distance and radius
        //radius of the medium is the median value
        int mid_radius = (int)Math.pow(10, median);
        int small_radius = (int)Math.pow(10, q1);
        int large_radius = (int)Math.pow(10, q3);
        System.out.println("Cloudlet radii: " + small_radius + " " + mid_radius + " " + large_radius);
        
        System.out.println("Total candidates: " + num_candidates);
        //using 75% of candidate num as num of cloudlets
        int num_cloudlets = (int)Math.round((0.75 * num_candidates));
        System.out.println("Total cloudlets: " + num_cloudlets);
        
        int num_small =(int)Math.round((0.25 * num_cloudlets));
        int num_medium =(int)Math.round((0.5 * num_cloudlets));
        int num_large = num_small;
        
        //num of cloudlets
        System.out.println("Cloudlet numbers: " + num_small + " " + num_medium + " " + num_large);
        
        //we divide the demand into three parts
        //based on the ratio of devices below radius
        //threshold of small, medium and large
        //note that 25% cases are between min and q1
        //50% are between q1 and q3
        //25% are between max and q3
        
        //total capacities are set 125% of the demands
        total_mem = (int)Math.round(total_mem * 1.25);
        total_proc = (int)Math.round(total_proc * 1.25);
        total_sto = (int)Math.round(total_sto * 1.25);
        System.out.println("Total demand: " + total_proc + " " + total_mem + " " + total_sto);
        
        //for p tp divided into a:b:c, we have formula
        //ap/a+b+c, bp/a+b+c, cp/a+b+c
        //large is same as small due to ratio
        //sizes are x:2x:3x -> small:medium:large
        //so the final ratio of capacities would be
        //num_small * 1 : num_medium*2 : num_large*3
        
        int divisor = num_small + 2*num_medium + 3*num_large;
        
        int small_mem = (num_small*total_mem)/divisor;
        int mid_mem = ((2*num_medium)*total_mem)/divisor;
        int large_mem = ((3*num_large)*total_mem)/divisor;
        System.out.println("Cloudlet shares: " + small_mem + " " + mid_mem + " " + large_mem);
        
        //int small_sto = (num_small*total_sto)/divisor;
        //int mid_sto = ((2*num_medium)*total_sto)/divisor;
        //int large_sto = ((3*num_large)*total_sto)/divisor;
        //System.out.println(small_sto + " " + mid_sto + " " + large_sto);
        
        //int small_proc = (num_small*total_proc)/divisor;
        //int mid_proc = ((2*num_medium)*total_proc)/divisor;
        //int large_proc = ((3*num_large)*total_proc)/divisor;
        //System.out.println(small_proc + " " + mid_proc + " " + large_proc);
        
        int small_size = (int)Math.round(small_mem*1.0/ num_small);
        int medium_size = (int)Math.round(mid_mem*1.0/ num_medium);
        int large_size = (int)Math.round(large_mem*1.0/ num_large);
        
        ArrayList<String> cloudlet_specs = new ArrayList<String>();
        int index = 1;
        String small = "";
        for(int i = 0; i < num_small; i++) {
        	small = index+","+small_size+","+small_size+","+small_size+","+small_radius+","+"s";
        	cloudlet_specs.add(small);
        	index++;
        }
        String medium = "";
        for(int i = 0; i < num_medium; i++) {
        	medium = index+","+medium_size+","+medium_size+","+medium_size+","+mid_radius+","+"m";
        	cloudlet_specs.add(medium);
        	index++;
        }
        String large = "";
        for(int i = 0; i < num_large; i++) {
        	large = index+","+large_size+","+large_size+","+large_size+","+large_radius+","+"l";
        	cloudlet_specs.add(large);
        	index++;
        }
        
        System.out.println(cloudlet_specs.toString());
        
        /*
         *Cloudlet specs completed here. ArrayList maintained and will
         *be used to determine candidate costs.
         *Memory, Storage, Processing and Radius are weighted equally in
         *the linear cost function.
         */
        
        //randomly generating premium indexes with 40% probability
        Random r = new Random();
        ArrayList<Integer> premium = new ArrayList<Integer>();
        for(int i = 0; i < num_candidates; i++) {
        	double number = r.nextDouble();
        	if(number <= 0.4) {
        		premium.add(i);
        	}
        }
        
        //arraylist to hold cost matrix
        ArrayList<String> cost_matrix = new ArrayList<String>();
        
        //repeat for every cloudlet
        for(String c: cloudlet_specs) {
        	//calculate the cost first
        	int cost = 0;
    		if(c.contains("s")) {
    			cost += small_size*3 + small_radius/1000;
    		}
    		else if(c.contains("m")) {
    			cost += medium_size*3 + mid_radius/1000;
    		}
    		else {
    			cost += large_size*3 + large_radius/1000;
    		}
    		
    		String cost_line = "";
    		
    		//then consider the candidate point and list its cost
        	for(int i = 0; i< num_candidates; i++) {
        		int new_cost = cost;
        		if(premium.contains(i)) {
        			new_cost += 5;
        		}
        		
        		if(i > 0) {
        			cost_line = cost_line + "," + new_cost;
        			//System.out.print("," + new_cost);
        		}
        		else {
        			cost_line = cost_line + "" + new_cost;
        			//System.out.print(new_cost);
        		}
        	}
        	cost_matrix.add(cost_line);
        	//System.out.println();
        }
        
        System.out.println(cost_matrix.toString());
        
        //writing the values to files
        //first cloudlet
        String cloudletFile = source_path + "cloudlet.csv";
        
        FileWriter writer;
		try {
			writer = new FileWriter(cloudletFile);
		
	        int cloudlet_file_size = cloudlet_specs.size();
			for(int i = 0; i < cloudlet_file_size; i++) {
				if(i>0) {
					writer.write(System.lineSeparator());
				}
	    		writer.write(cloudlet_specs.get(i));
	    	}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        //now costs
        String costsFile = source_path + "costs.csv";
        try {
			writer = new FileWriter(costsFile);
		
	        int costs_file_size = cost_matrix.size();
			for(int i = 0; i < costs_file_size; i++) {
				if(i>0) {
					writer.write(System.lineSeparator());
				}
	    		writer.write(cost_matrix.get(i));
	    	}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	public static ArrayList<Double> getOutliers(ArrayList<Double> input) {
		ArrayList<Double> output = new ArrayList<Double>();
		ArrayList<Double> data1 = new ArrayList<Double>();
		ArrayList<Double> data2 = new ArrayList<Double>();
        if (input.size() % 2 == 0) {
            data1 = new ArrayList<Double>(input.subList(0, input.size() / 2));
            data2 = new ArrayList<Double>(input.subList(input.size() / 2, input.size()));
        } else {
            data1 = new ArrayList<Double>(input.subList(0, input.size() / 2));
            data2 = new ArrayList<Double>(input.subList(input.size() / 2 + 1, input.size()));
        }
        double q1 = getMedian(data1);
        double q3 = getMedian(data2);
        double iqr = q3 - q1;
        double lowerFence = q1 - 1.5 * iqr;
        double upperFence = q3 + 1.5 * iqr;
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i) < lowerFence || input.get(i) > upperFence)
                output.add(input.get(i));
        }
        return output;
    }

    private static double getMedian(ArrayList<Double> data) {
        if (data.size() % 2 == 0)
            return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2;
        else
            return data.get(data.size() / 2);
    }

	public static void main(String[] args) {
		
		String base_path = "datasets/staten_island/samples/";
		
		//get the cloudlet specs
		//auto-generates cloudlet specification files 
		for(int i = 1; i <= 30; i++) { 
			setCloudletsAndCandidateCosts(base_path + i + "/");
		}
	
	}

}
