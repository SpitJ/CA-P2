

import com.google.common.collect.TreeBasedTable;
import com.google.common.collect.Table;


public class run {

	public static void main(String[] args) 
	{
		Table<String, String, String> grid = TreeBasedTable.create();

		// read  grid info from DB
		ReadFromDB readDB = new ReadFromDB();
		grid = readDB.ReadDBinTable("measurements",grid);
		
		
		TableToCSV tabletocsv = new TableToCSV();
		System.out.println("Read DB: " + grid);
		tabletocsv.write(grid, "./csv/grid.csv");
		
	}
	

}
