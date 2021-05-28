package dataset_creation;

/**
 * To create randomly sampled datasets based on each
 * scenario. Sampling is done for candidate points
 * and devices. Costs, Latencies depend on the candidate
 * points and/or devices. Cloudlets remain the same for
 * each sub-scenario. 
 * @author Dixit
 *
 */
public class DatasetSampling {
	
	public static void main(String[] args) {
		
		SelectionSampling s = new SelectionSampling();
		String root = "datasets/queens/";
		
		String devicePath = root + "base_device.csv";
		String pointPath = root + "base_points.csv";
		
		//change this when sampling device or points
		//check the base file in datasets to see the exact number
		int total_num = 531;
		
		//int sub_folder = 1;
		
		for(int i = 1; i <= 30; i++) {
			//change the end filename when sampling device or points
			String outpath = root + "samples/" + i + "/" + "device.csv";
			
			//uncomment the function you want
			s.createSample(devicePath, total_num, outpath);
			//s.createSample(pointPath, total_num, outpath);
		}
		
	}

}
