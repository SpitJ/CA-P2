import com.google.common.collect.TreeBasedTable;
import com.google.common.collect.Table;

public class run 
{
	public static void main(String[] args) 
	{
		// initialize csv writer
		TableToCSV tabletocsv = new TableToCSV();
		
		// read  training set from DB
		Table<String, String, String> training_set = TreeBasedTable.create();
		ReadFromDB readDB = new ReadFromDB();
		training_set = readDB.ReadDBinTable("measurements",training_set);
		tabletocsv.write(training_set, "./csv/training_set.csv");
		
		// Normalize the data: Calc Avg of one measurement & scale appropriate
		Table<String, String, String> norm_training_set = TreeBasedTable.create();
		norm_training_set.putAll(training_set);
		Table<String, String, String> norm_factor = TreeBasedTable.create();
		MachineLearning machinelearning = new MachineLearning();
		norm_factor = machinelearning.CalculateNormalizationFactors(norm_training_set);
		norm_training_set = machinelearning.Normalize(norm_training_set, norm_factor);
		tabletocsv.write(norm_training_set, "./csv/norm_training_set.csv");
		
		// Calculate the initial centroids 
		Table<String, String, String> norm_centroids = TreeBasedTable.create();
		norm_centroids = machinelearning.InitCentroids(norm_training_set, 4);
		tabletocsv.write(norm_centroids, "./csv/norm_init_centroids.csv");
		
		// Perform actual k means until treshold is reached
		norm_centroids = machinelearning.PerformKMeans(norm_training_set, norm_centroids, 0.001);
		
		// Denormalize centroids and store in csv
		Table<String, String, String> centroids = TreeBasedTable.create();
		centroids = machinelearning.DeNormalize(norm_centroids, norm_factor);
		tabletocsv.write(centroids, "./csv/centroids.csv");
		
		// link centroids to training set, calculate distances and store in csv
		training_set = machinelearning.LinkTrainingSetCluster(training_set);
		tabletocsv.write(training_set, "./csv/training_set.csv");
				
		// Read in Test Set for KNN
		Table<String, String, String> test_set = TreeBasedTable.create();
		test_set = readDB.ReadDBinTable("analog_values",test_set);
		tabletocsv.write(test_set, "./csv/test_set.csv");
		
		// Normalize Test Set
		Table<String, String, String> norm_test_set = TreeBasedTable.create();
		norm_test_set = machinelearning.Normalize(test_set, norm_factor);
		tabletocsv.write(norm_test_set, "./csv/norm_test_set.csv");
		
		// perform KNN
		test_set = machinelearning.PerformKNN(test_set,norm_test_set, norm_training_set, 4);
		tabletocsv.write(test_set, "./csv/test_set.csv");
	}
}
