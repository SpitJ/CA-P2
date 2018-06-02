

import com.google.common.collect.TreeBasedTable;
import com.google.common.collect.Table;


public class run {

	public static void main(String[] args) 
	{
		// read  training set from DB
		Table<String, String, String> training_set = TreeBasedTable.create();
		ReadFromDB readDB = new ReadFromDB();
		training_set = readDB.ReadDBinTable("measurements",training_set);
		
		TableToCSV tabletocsv = new TableToCSV();
//		System.out.println("Read DB: " + training_set);
		tabletocsv.write(training_set, "./csv/training_set.csv");
		
		// Normalize the data: Calc Avg of one measurement & scale appropriate
		Table<String, String, String> norm_training_set = TreeBasedTable.create();
		Table<String, String, String> norm_factor = TreeBasedTable.create();
		KMeans kmeans = new KMeans();
		norm_factor = kmeans.CalculateNormalizationFactors(training_set);
		norm_training_set = kmeans.Normalize(training_set, norm_factor);
		
		// Calculate the clusters with the normalized data
		Table<String, String, String> norm_centroids = TreeBasedTable.create();
		norm_centroids = kmeans.InitCentroids(norm_training_set, 4);
		tabletocsv.write(norm_centroids, "./csv/norm_init_centroids.csv");
		
		// Perform actual k means until treshold is reached
		norm_centroids = kmeans.PerformKMeans(norm_training_set, norm_centroids, 0.001);
		
		// Denormalize Centroids and store in csv
		Table<String, String, String> centroids = TreeBasedTable.create();
		centroids = kmeans.DeNormalize(norm_centroids, norm_factor);
		tabletocsv.write(centroids, "./csv/centroids.csv");
		
//		System.out.println("Normalized measurements: " + norm_training_set);
		tabletocsv.write(norm_training_set, "./csv/norm_training_set.csv");
		
		//Read in Test Set for KNN
		Table<String, String, String> test_set = TreeBasedTable.create();
		test_set = readDB.ReadDBinTable("analog_values",test_set);
		tabletocsv.write(test_set, "./csv/test_set.csv");
		
		// Normalize Test Set
		Table<String, String, String> norm_test_set = TreeBasedTable.create();
		norm_test_set = kmeans.Normalize(test_set, norm_factor);
		tabletocsv.write(norm_test_set, "./csv/norm_test_set.csv");
		
		test_set = kmeans.PerformKNN(test_set,norm_test_set, norm_training_set, 4);
		tabletocsv.write(test_set, "./csv/test_set.csv");
		// 
	}
	

}
