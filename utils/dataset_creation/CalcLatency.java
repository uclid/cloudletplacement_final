package dataset_creation;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CalcLatency {
    
    public static ArrayList<ArrayList<Integer>> getDevices(String path) {
    	//device specifications have 5 fields
    	ArrayList<ArrayList<Integer>> device_specs = new ArrayList<ArrayList<Integer>>();
    	
    	String csvFile = path;
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        	
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] item = line.split(cvsSplitBy);
                
                //adding only device location coordinates
                ArrayList<Integer> dummy = new ArrayList<Integer>();
                dummy.add(Integer.parseInt(item[4]));
                dummy.add(Integer.parseInt(item[5]));
                
                device_specs.add(dummy);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    	return device_specs;
    }
    
    public static ArrayList<ArrayList<Integer>> getPoints(String path) {
    	//points specifications have 3 fields
    	ArrayList<ArrayList<Integer>> cand_points = new ArrayList<ArrayList<Integer>>();
    	
    	String csvFile = path;
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        	
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] item = line.split(cvsSplitBy);
                
                //a dummy array list just to hold coordinates
                ArrayList<Integer> dummy = new ArrayList<Integer>();
                dummy.add(Integer.parseInt(item[1]));
                dummy.add(Integer.parseInt(item[2]));
                
                cand_points.add(dummy);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    	return cand_points;
    }
    
    public static void main(String args[]) {
    	
    	String base_path = "datasets/manhattan/";
    	
    	for(int looper = 1; looper <= 30; looper++) {
	    	String device_path = base_path + "samples/" + looper + "/" + "device.csv";
	    	String points_path = base_path + "samples/" + looper + "/" + "points.csv";
	    	
	    	ArrayList<ArrayList<Integer>> cand_points = getPoints(points_path);
	    	ArrayList<ArrayList<Integer>> device_specs = getDevices(device_path);
	    	int num_devices = device_specs.size(); 
	    	int num_candidates = cand_points.size();
	    	
	    	String outpath = base_path + "samples/" + looper + "/" + "latencies.csv";
	    	try {
				FileWriter writer = new FileWriter(outpath);
				for(int i = 0; i < num_devices; i++) {
					if(i>0) {
						writer.write(System.lineSeparator());
					}
		    		for(int j = 0; j < num_candidates; j++) {
		    			int dev_x = device_specs.get(i).get(0);
		    			int dev_y = device_specs.get(i).get(1);
		    			int point_x = cand_points.get(j).get(0);
		    			int point_y = cand_points.get(j).get(1);
		    			
		    			//System.out.println("x: " +  dev_x + " y: " + dev_y);
		    			
		    			double dist = Math.sqrt(Math.pow(dev_y - point_y, 2) + 
		    					Math.pow(dev_x - point_x, 2));
		    			
		    			//Checked that log is negative when distance is zero
		    			//So, making latency zero if the log value is negative 
		    			double latency = Math.log10(dist) > 0? Math.log10(dist) : 0;
		    			
		    			if(j == num_candidates - 1 ) {
		    				writer.write(latency+"");
		    				System.out.print(latency);
		    			}
		    			else {
		    				writer.write(latency + ",");
		    				System.out.print(latency + ",");
		    			}
		    			
		    		}
		    		System.out.println();
		    	}
				writer.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
}
