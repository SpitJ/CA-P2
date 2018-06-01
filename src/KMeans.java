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
	
	Table<String, String, String> norm_meas;
	Table<String, String, String> centroids;
	
	// Normalize the data: Calc Avg of one measurement & scale appropriate
	public Table<String, String, String> NormalizeMeasurements(Table<String, String, String> meas)
	{
		// Calc AVG:
		for (String columnKey : meas.columnKeySet())
		{
			// find average
			Double avg = 0.0;
			Double amount = 0.0;
			for (String rowKey : meas.rowKeySet())
			{
				Double value = Double.parseDouble(meas.get(rowKey, columnKey));
				avg = avg + value;
				amount = amount + 1;
			}
			avg = avg / amount;
			// substract average from each value and put in normalized measurement table
			for (String rowKey : meas.rowKeySet())
			{
				Double value = Double.parseDouble(meas.get(rowKey, columnKey));
				value = value - avg;
				meas.put(rowKey, columnKey, value.toString());
			}
			
			// find abs max in each column in normalized data
			Double absmax = 0.0;
			for (String rowKey : meas.rowKeySet())
			{
				Double value = Double.parseDouble(meas.get(rowKey, columnKey));
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
			// scale data to equal 1 if magnitude is absolute maximum 
			for (String rowKey : meas.rowKeySet())
			{
				Double value = Double.parseDouble(meas.get(rowKey, columnKey));
				value = value/absmax;
				meas.put(rowKey, columnKey, value.toString());
			}
		}
		return meas;
	}
	
	// Create initial centroids based on amount
	public Table<String, String, String> InitCentroids(Table<String, String, String> norm_meas, int amountCentroids)
	{
		Table<String, String, String> init_centroids = TreeBasedTable.create();
		
		// Convert rowKeys of normalized measurements to list (to index them)
		List rowKeyList = new ArrayList(norm_meas.rowKeySet());
		//create start centroids table
		for(int i=0;i<amountCentroids;i++)
		{
			Map<String, String> row = new HashMap<String, String>();
			row = norm_meas.row(rowKeyList.get(i*4).toString());
			init_centroids.row(String.valueOf(i)).putAll(row);
		}
		tabletocsv.write(init_centroids, "./csv/init_centroids.csv");
		return init_centroids;
	}
	
	public Table<String, String, String> PerformKMeans(Table<String, String, String> init_norm_meas, Table<String, String, String> init_centroids, Double treshold)
	{ 
		norm_meas = init_norm_meas;
		centroids = init_centroids;
		
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
		
		
		return norm_meas;
	}
	
	private Double CalcNewCentroid()
	{
		// calculate euclydic distance of each measurement object to each centroid and store it
		Table<String, String, String> meas_cluster = TreeBasedTable.create();
		for (String centroidRowKey : centroids.rowKeySet())
		{
			for (String rowKey : norm_meas.rowKeySet())
			{
				Double meas_val = 0.0;
				Double centroid_val = 0.0;
				Double dist = 0.0;
				for (String columnKey : norm_meas.columnKeySet())
				{
//					System.out.println("RowKey: " + rowKey + "ColumKey: " + columnKey);
					meas_val = Double.parseDouble(norm_meas.get(rowKey, columnKey));
					centroid_val = Double.parseDouble(centroids.get(centroidRowKey, columnKey));
					dist += (centroid_val-meas_val)*(centroid_val-meas_val);	
				}
				dist = Math.sqrt(dist);
				meas_cluster.put(rowKey, "dist_" + centroidRowKey, dist.toString());
			}
		}
		
		tabletocsv.write(meas_cluster, "./csv/measurement_cluster.csv");

		// assign cluster to each measurement object
		for (String rowKey : norm_meas.rowKeySet())
		{
			Double mindist = Double.parseDouble(meas_cluster.get(rowKey, "dist_0"));
			Integer cluster = 0;
			// find minimium euclydian distance
			for (String centroidRowKey : centroids.rowKeySet())
			{
				Double dist = Double.parseDouble(meas_cluster.get(rowKey, "dist_" + centroidRowKey));
				if (dist < mindist)
				{
					mindist = dist;
					cluster = Integer.valueOf(centroidRowKey);
				}
			}
			meas_cluster.put(rowKey, "mindist", mindist.toString());
			meas_cluster.put(rowKey, "cluster", cluster.toString());
		}
		
		tabletocsv.write(meas_cluster, "./csv/measurement_cluster.csv");
		
		// calculate new cluster position
		// first calculate average position of all included measurements
		for (String centroidRowKey : centroids.rowKeySet())
		{
			Set<String> RowKeys = new HashSet<String>();
			// find all measurements linked to a specific cluster
			RowKeys = getKeysByValue(meas_cluster.column("cluster"),centroidRowKey);
			// iterate through measurements linked to a specific cluster and calculate average
			
			for (String columnKey : norm_meas.columnKeySet())
			{
				Double avg = 0.0;
				Double amount = 0.0;
				for (String rowKey : RowKeys)
				{
					Double value = Double.parseDouble(norm_meas.get(rowKey, columnKey));
					avg = avg + value;
					amount = amount + 1;
				}
//				System.out.println("Cluster: " + centroidRowKey + " amount " + amount);
				avg = avg / amount;
				centroids.put(centroidRowKey, columnKey + "_avg", avg.toString());
				centroids.put(centroidRowKey, "amount", amount.toString());
			}
		}
		
		// calculate error between current and new centroid position
		for (String centroidRowKey : centroids.rowKeySet())
		{
			Double old_val = 0.0;
			Double new_val = 0.0;
			Double dist = 0.0;
			for (String columnKey : norm_meas.columnKeySet())
			{
				old_val = Double.parseDouble(centroids.get(centroidRowKey, columnKey));
				new_val = Double.parseDouble(centroids.get(centroidRowKey, columnKey + "_avg"));
				dist += (old_val-new_val)*(old_val-new_val);
			}
			dist = Math.sqrt(dist);
			centroids.put(centroidRowKey, "dist_to_new", dist.toString());
		}
		
		// calculate distance to new centroid position for later return
		Double treshold = 0.0;
		for (String centroidRowKey : centroids.rowKeySet())
		{
			Double dist = Double.parseDouble(centroids.get(centroidRowKey, "dist_to_new"));
			if (dist > treshold)
			{
				treshold = dist;
			}
		}
		
		// set new centroid 
		for (String centroidRowKey : centroids.rowKeySet())
		{
			String new_val = "";
			for (String columnKey : norm_meas.columnKeySet())
			{
				new_val = centroids.get(centroidRowKey, columnKey + "_avg");
				centroids.put(centroidRowKey, columnKey, new_val);
			}
		}
		
		tabletocsv.write(centroids, "./csv/centroids.csv");
		
		return treshold;
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
