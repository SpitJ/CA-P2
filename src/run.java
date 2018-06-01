

import com.google.common.collect.TreeBasedTable;
import com.google.common.collect.Table;


public class run {

	public static void main(String[] args) 
	{
		Table<String, String, String> meas = TreeBasedTable.create();

		// read  grid info from DB
		ReadFromDB readDB = new ReadFromDB();
		meas = readDB.ReadDBinTable("measurements",meas);
		
		TableToCSV tabletocsv = new TableToCSV();
//		System.out.println("Read DB: " + meas);
		tabletocsv.write(meas, "./csv/measurements.csv");
		
		// Normalize the data: Calc Avg of one measurement & scale appropriate
		Table<String, String, String> norm_meas = TreeBasedTable.create();
		KMeans kmeans = new KMeans();
		norm_meas = kmeans.NormalizeMeasurements(meas);
		
		// Calculate the clusters with the normalized data
		Table<String, String, String> centroids = TreeBasedTable.create();
		centroids = kmeans.InitCentroids(norm_meas, 6);
		
		// Perform actual k means until treshold is reached
		norm_meas = kmeans.PerformKMeans(norm_meas, centroids, 0.001);
		
//		System.out.println("Normalized measurements: " + norm_meas);
		tabletocsv.write(norm_meas, "./csv/normalized_measurements.csv");
	}
	

}
