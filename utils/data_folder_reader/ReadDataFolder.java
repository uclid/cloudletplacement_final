package data_folder_reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import base.*;

public class ReadDataFolder {
    
    public ArrayList<Cloudlet> getCloudlets(String path) {
    	//cloudlet specifications have 5 fields
    	ArrayList<Cloudlet> cloudlets = new ArrayList<Cloudlet>();
    	
    	String csvFile = path + "cloudlet.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        	
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] item = line.split(cvsSplitBy);
                
                int id = Integer.parseInt(item[0]);
                int processor = Integer.parseInt(item[1]);
                int memory = Integer.parseInt(item[2]);
                int storage = Integer.parseInt(item[3]);
                int radius = Integer.parseInt(item[4]);
                String type = item[5];
                
                cloudlets.add(new Cloudlet(id, processor, memory, storage, radius, type));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    	return cloudlets;
    }
    
    public ArrayList<EndDevice> getDevices(String path) {
    	//device specifications have 5 fields
    	ArrayList<EndDevice> devices = new ArrayList<EndDevice>();
    	
    	String csvFile = path + "device.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        	
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] item = line.split(cvsSplitBy);

                int id = Integer.parseInt(item[0]);
                int processor = Integer.parseInt(item[1]);
                int memory = Integer.parseInt(item[2]);
                int storage = Integer.parseInt(item[3]);
                int x_coord = Integer.parseInt(item[4]);
                int y_coord = Integer.parseInt(item[5]);
               
                devices.add(new EndDevice(id, processor, memory, storage, x_coord, y_coord));
                
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    	return devices;
    }
    
    public ArrayList<CandidatePoint> getPoints(String path) {
    	//points specifications have 3 fields
    	ArrayList<CandidatePoint> points = new ArrayList<CandidatePoint>();
    	
    	String csvFile = path + "points.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        	
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] item = line.split(cvsSplitBy);

                int id = Integer.parseInt(item[0]);
                int x_coord = Integer.parseInt(item[1]);
                int y_coord = Integer.parseInt(item[2]);
                
                points.add(new CandidatePoint(id, x_coord, y_coord));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    	return points;
    }
    
    public int[][] getCosts(String path, int num_cloudlets, int num_candidates) {
    	//costs are 2x2 matrix, cloudlet and points
    	int[][] costs = new int[num_cloudlets][num_candidates];
    	
    	String csvFile = path + "costs.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        	
        	int i = 0;
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] item = line.split(cvsSplitBy);
                for(int j = 0; j < num_candidates; j++) {
                    costs[i][j] = Integer.parseInt(item[j]);
                }
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    	return costs;
    }

    public int[][] getLatencies(String path, int num_devices, int num_candidates) {
    	//latencies are 2x2 matrix, devices and points
    	int[][] latency = new int[num_devices][num_candidates];
    	
    	String csvFile = path + "latencies.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        	
        	int i = 0;
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] item = line.split(cvsSplitBy);
                for(int j = 0; j < num_candidates; j++) {
                    latency[i][j] = (int)Math.round(Double.parseDouble((item[j])));
                }
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    	return latency;
    }
}
