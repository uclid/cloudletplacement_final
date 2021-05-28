package dataset_creation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class SelectionSampling {
	
	//Input: File from where the sample is generated
	
	/*
	 * Basic - Selection Sampling Algorithm
	 * Iterate through the list and for each element, make the probability 
	 * of selection = (number needed) / (number left)

    	So if you had 40 items, the first would have a 5/40 chance of being selected.

    	If it is, the next has a 4/39 chance, otherwise it has a 5/39 chance. 
    	By the time you get to the end you will have your 5 items, 
    	and often you'll have all of them before that.
    	
    	Given a good random number generator, this method is unbaised.
    	- It represents the actual distribution of points.
    	- Each point has an equal chance of getting selected.
	 */
	public void createSample(String path, int total_num, String outpath) {
		
		double probability_of_selection = 1;
		//we set this equal since the sample is always smaller
		int num_needed = total_num;
		int sample_size = 0;
		
		Random r = new Random();
		
		String csvFile = path;
        String line = "";
        String cvsSplitBy = ",";
        
        //reordered numbering based on new selection
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        	
        	ArrayList<String> arr = new ArrayList<String>();
        	
            while ((line = br.readLine()) != null) {
            	
            	//split the items
            	String[] item = line.split(cvsSplitBy);
            	
            	probability_of_selection = num_needed/(total_num*1.0);
            	double d = r.nextDouble();
            	
            	if(d <= probability_of_selection) {
            		
            		String new_line = (sample_size+1) + "";
            		for(int i=1; i < item.length; i++) {
            			new_line += "," + item[i];
            		}
            		
            		//add to the arraylist for later writing
            		arr.add(new_line);
            		System.out.println(new_line);
            		
            		//reduce num_needed
            		num_needed--;
            		sample_size++;
            	}
                
            }
            
          //automate the sampling to be written to a file
            FileWriter writer = new FileWriter(outpath);
            int final_index = arr.size();
            
            for(int i = 0 ; i < final_index; i++) {
            	//this is to avoid extra-line at the end
            	if(i < final_index-1) {
            		writer.write(arr.get(i) + System.lineSeparator());
            	}
            	else {
            		writer.write(arr.get(i));
            	}
            }
            
            System.out.println("Sample Size: " + sample_size);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	

}
