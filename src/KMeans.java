import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

// class to create K means clusters
public class KMeans 
{	
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

	public Table<String, String, String> CalcCluster(Table<String, String, String> norm_meas, int amountCentroids)
	{
		Table<String, String, String> cluster = TreeBasedTable.create();
		
		// Convert rowKeys of normalized measurements to list (to index them)
		List rowKeyList = new ArrayList(norm_meas.rowKeySet());
		//create start centroids table
		for(int i=0;i<amountCentroids;i++)
		{
			Map<String, String> row = new HashMap<String, String>();
			row = norm_meas.row(rowKeyList.get(i).toString());
			cluster.row(String.valueOf(i)).putAll(row);
		}
		
		TableToCSV tabletocsv = new TableToCSV();
		tabletocsv.write(cluster, "./csv/cluster.csv");
		
		// calculate euclydic distance of each point to each cluster
		
		
		
		return norm_meas;
	}
}
