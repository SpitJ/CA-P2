import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;


// class to create K means clusters
public class KMeans 
{	
	// for debug purposes
	TableToCSV tabletocsv = new TableToCSV();
	
	Table<String, String, String> norm_training_set;
	Table<String, String, String> norm_centroids;
	
	// Normalizes input based on normalization table
	public Table<String, String, String> Normalize(Table<String, String, String> input, Table<String, String, String> norm_factor)
	{
		for (String columnKey : norm_factor.columnKeySet())
		{
			for (String rowKey : input.rowKeySet())
			{
				Double value = Double.parseDouble(input.get(rowKey, columnKey));
				// substract average from each value and put in normalized measurement table
				Double avg = Double.parseDouble(norm_factor.get("avg", columnKey));
				value = value - avg;
				
				// scale data to equal 1 if magnitude is absolute maximum 
				Double scalefactor = Double.parseDouble(norm_factor.get("scalefactor", columnKey));	
				value = value*scalefactor;
				input.put(rowKey, columnKey, value.toString());
			}
		}
		// return normalized input
		return input;
	}
	
	// DeNormalizes input based on normalization table
	public Table<String, String, String> DeNormalize(Table<String, String, String> input, Table<String, String, String> norm_factor)
	{
		Table<String, String, String> output = TreeBasedTable.create();
		for (String columnKey : norm_factor.columnKeySet())
		{
			for (String rowKey : input.rowKeySet())
			{
				Double value = Double.parseDouble(input.get(rowKey, columnKey));
				// scale data to equal 1 if magnitude is absolute maximum 
				Double scalefactor = Double.parseDouble(norm_factor.get("scalefactor", columnKey));	
				value = value/scalefactor;
				// substract average from each value and put in normalized measurement table
				Double avg = Double.parseDouble(norm_factor.get("avg", columnKey));
				value = value + avg;
				output.put(rowKey, columnKey, value.toString());
			}
		}
		// return normalized input
		return output;
	}
	
	// returns table with normalization factors
	public Table<String, String, String> CalculateNormalizationFactors(Table<String, String, String> training_set)
	{
		Table<String, String, String> norm_factor = TreeBasedTable.create();
		// Calc AVG:
		for (String columnKey : training_set.columnKeySet())
		{
			// find average
			Double avg = 0.0;
			Double amount = 0.0;
			for (String rowKey : training_set.rowKeySet())
			{
				Double value = Double.parseDouble(training_set.get(rowKey, columnKey));
				avg = avg + value;
				amount = amount + 1;
			}
			avg = avg / amount;
			norm_factor.put("avg", columnKey, String.valueOf(avg));
			
			// find abs max in each column in normalized data
			Double absmax = 0.0;
			for (String rowKey : training_set.rowKeySet())
			{
				Double value = Double.parseDouble(training_set.get(rowKey, columnKey));
				value = value - avg;
				if (Math.abs(value) > absmax)
				{
					absmax = Math.abs(value);
				}
			}
			// if absolute maximum = 0, then don't scale data
			if(absmax==0.0)
			{
				absmax = 1.0;
			}
			Double scalefactor = 1.0/absmax;
			norm_factor.put("scalefactor", columnKey, scalefactor.toString());
		}
		
		tabletocsv.write(norm_factor, "./csv/norm_factor.csv");

		return norm_factor;
	}
	
	// Create initial centroids based on amount
	public Table<String, String, String> InitCentroids(Table<String, String, String> init_norm_training_set, int amountCentroids)
	{
		Table<String, String, String> init_centroids = TreeBasedTable.create();
		norm_training_set = init_norm_training_set;
		// Convert rowKeys of normalized measurements to list (to index them)
		List rowKeyList = new ArrayList(init_norm_training_set.rowKeySet());
		//create start centroids table
		for(int i=0;i<amountCentroids;i++)
		{
			Map<String, String> row = new HashMap<String, String>();
			row = init_norm_training_set.row(rowKeyList.get(i).toString());
			init_centroids.row(String.valueOf(i)).putAll(row);
		}
		return init_centroids;
	}
	
	// function to perform actual K means
	public Table<String, String, String> PerformKMeans(Table<String, String, String> init_norm_training_set, Table<String, String, String> init_centroids, Double treshold)
	{ 
		norm_training_set = init_norm_training_set;
		norm_centroids = init_centroids;
		
		// calculate new centroids until treshold reached
		for(int i=0; i<100; i++)
		{
			Double distance = CalcNewCentroid();
			System.out.println("New Calculation performed, Distance: " + distance);
			if(distance < treshold )
			{
				i = 100;
			}
		}
		
		
		return norm_centroids;
	}
	
	// function to perform actual K means
	public Table<String, String, String> PerformKNN(Table<String, String, String> norm_test_set, Table<String, String, String> norm_training_set, int amount_nearest_neighbors)
	{ 
		// calculate distance from each point in training set to test set
		Table<String, String, String> training_to_test_set = TreeBasedTable.create();
		training_to_test_set = CalcEuclyDist(norm_training_set, norm_test_set);
		
		tabletocsv.write(training_to_test_set, "./csv/training_to_test_set.csv");
		
		return norm_test_set;
	}
	
	// Calculates new centroid position and returns treshold
	private Double CalcNewCentroid()
	{
		// calculate euclydic distance of each measurement object to each centroid and store it
		Table<String, String, String> training_set_cluster = TreeBasedTable.create();
		training_set_cluster = CalcEuclyDist(norm_training_set, norm_centroids);
		
		// assign cluster to each measurement object
		for (String rowKey : norm_training_set.rowKeySet())
		{
			Double mindist = Double.parseDouble(training_set_cluster.get(rowKey, "dist_0"));
			Integer cluster = 0;
			// find minimium euclydian distance
			for (String centroidRowKey : norm_centroids.rowKeySet())
			{
				Double dist = Double.parseDouble(training_set_cluster.get(rowKey, "dist_" + centroidRowKey));
				if (dist < mindist)
				{
					mindist = dist;
					cluster = Integer.valueOf(centroidRowKey);
				}
			}
			training_set_cluster.put(rowKey, "mindist", mindist.toString());
			training_set_cluster.put(rowKey, "cluster", cluster.toString());
		}
				
		// calculate new cluster position
		// first calculate average position of all included measurements
		for (String centroidRowKey : norm_centroids.rowKeySet())
		{
			Set<String> RowKeys = new HashSet<String>();
			// find all measurements linked to a specific cluster
			RowKeys = getKeysByValue(training_set_cluster.column("cluster"),centroidRowKey);
			// iterate through measurements linked to a specific cluster and calculate average
			
			for (String columnKey : norm_training_set.columnKeySet())
			{
				Double avg = 0.0;
				Double amount = 0.0;
				for (String rowKey : RowKeys)
				{
					Double value = Double.parseDouble(norm_training_set.get(rowKey, columnKey));
					avg = avg + value;
					amount = amount + 1;
				}
//				System.out.println("Cluster: " + centroidRowKey + " amount " + amount);
				avg = avg / amount;
				norm_centroids.put(centroidRowKey, columnKey + "_avg", avg.toString());
				norm_centroids.put(centroidRowKey, "amount", amount.toString());
			}
		}
		
		// calculate error between current and new centroid position
		for (String centroidRowKey : norm_centroids.rowKeySet())
		{
			Double old_val = 0.0;
			Double new_val = 0.0;
			Double dist = 0.0;
			for (String columnKey : norm_training_set.columnKeySet())
			{
				old_val = Double.parseDouble(norm_centroids.get(centroidRowKey, columnKey));
				new_val = Double.parseDouble(norm_centroids.get(centroidRowKey, columnKey + "_avg"));
				dist += (old_val-new_val)*(old_val-new_val);
			}
			dist = Math.sqrt(dist);
			norm_centroids.put(centroidRowKey, "dist_to_new", dist.toString());
		}
		
		// calculate distance to new centroid position for later return
		Double treshold = 0.0;
		for (String centroidRowKey : norm_centroids.rowKeySet())
		{
			Double dist = Double.parseDouble(norm_centroids.get(centroidRowKey, "dist_to_new"));
			if (dist > treshold)
			{
				treshold = dist;
			}
		}
		
		// set new centroid 
		for (String centroidRowKey : norm_centroids.rowKeySet())
		{
			String new_val = "";
			for (String columnKey : norm_training_set.columnKeySet())
			{
				new_val = norm_centroids.get(centroidRowKey, columnKey + "_avg");
				norm_centroids.put(centroidRowKey, columnKey, new_val);
			}
		}
		
		tabletocsv.write(norm_centroids, "./csv/norm_centroids.csv");
		tabletocsv.write(training_set_cluster, "./csv/training_set_cluster.csv");

		return treshold;
	}
	
	// calculates euclydian distance and returns it in a table
	// rows: the objects which should be taken over as rows in the returned table
	// columns: the objects which should be taken over as columns (dist_'objectname')
	private Table<String, String, String> CalcEuclyDist(Table<String, String, String> rows, Table<String, String, String> columns)
	{
		Table<String, String, String> from_to = TreeBasedTable.create();
		for (String toRowKey : columns.rowKeySet())
		{
			for (String rowKey : rows.rowKeySet())
			{
				Double meas_val = 0.0;
				Double centroid_val = 0.0;
				Double dist = 0.0;
				for (String columnKey : norm_training_set.columnKeySet())
				{
//							System.out.println("RowKey: " + rowKey + "ColumKey: " + columnKey);
					meas_val = Double.parseDouble(rows.get(rowKey, columnKey));
					centroid_val = Double.parseDouble(columns.get(toRowKey, columnKey));
					dist += (centroid_val-meas_val)*(centroid_val-meas_val);	
				}
				dist = Math.sqrt(dist);
				from_to.put(rowKey, "dist_" + toRowKey, dist.toString());
			}
		}
		return from_to;
	}
		
	// helper function to obtain keys from a value
	private static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) 
	{
	    Set<T> keys = new HashSet<T>();
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (Objects.equals(value, entry.getValue())) {
	            keys.add(entry.getKey());
	        }
	    }
	    return keys;
	}
}
