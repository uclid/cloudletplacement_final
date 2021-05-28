package dataset_creation;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class OpenDataToDataset {
	
	public static void getDevices(int borough) {
    	//scan for the borough in the CSV file and get 
		//normalized coordinates of the hotspot locations
    	
    	String csvFile = "raw_data/nycopendata/NYC_Wi-Fi_Hotspot_Locations.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        	
        	int i = 0;
        	int j = 0;
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] item = line.split(cvsSplitBy);
                int borough_in_file = 0;
                
                try {
                	borough_in_file = Integer.parseInt(item[1]);
                }catch(Exception e){
                	//System.out.println(item[1]);
                }
                
                Random r = new Random();
                int demand = r.nextInt(21-5) + 5; //get int from 5 to 20 
                if(borough_in_file == borough){
                	try {
                		System.out.println((j+1) + "," + demand + "," + demand + "," + demand + "," + (int)Double.parseDouble(item[5]) + "," + (int)Double.parseDouble(item[6]));
                	}catch(Exception e) {
                		
                	}
                	j++;
                }
                i++;
            }
            
            System.out.println("Total Points in Borough " + borough + ": " + j);

        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    }
	
	public static void getCandidates(int borough) {
    	//scan for the borough in the CSV file 
		//Suitable candidates are subway station or library
    	
    	String csvFile = "raw_data/nycopendata/NYC_Wi-Fi_Hotspot_Locations.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        	
        	int i = 0;
        	int j = 0;
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] item = line.split(cvsSplitBy);
                int borough_in_file = 0;
                
                try {
                	borough_in_file = Integer.parseInt(item[1]);
                }catch(Exception e){
                	//System.out.println(item[1]);
                }
                
                if(borough_in_file == borough && (item[7].contains("Sub") || item[7].contains("Lib"))){
                	try {
                		System.out.println((j+1) + "," + (int)Double.parseDouble(item[5]) + "," + (int)Double.parseDouble(item[6]));
                	}catch(Exception e) {
                		
                	}
                	j++;
                }
                i++;
            }
            
            System.out.println("Total Candidate Points in Borough " + borough + ": " + j);

        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    }


	public static void main(String[] args) {
		
		getDevices(1);
		//getCandidates(5);
	}
	
}
