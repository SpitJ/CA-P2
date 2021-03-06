import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import com.google.common.collect.TreeBasedTable;
import com.google.common.collect.Table;


// Class to read from DB into Guava Table
public class ReadFromDB 
{
	public Table<String, String, String> ReadDBinTable(String TableName, Table<String, String, String> input)
	{
		System.out.println("Try open DB and read table " + TableName);
		try 
		{
			Class.forName( "com.mysql.cj.jdbc.Driver" );
			Connection conn = DriverManager.getConnection(
					 "jdbc:mysql://localhost/subtables?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
					 "root",
					 "root" );
			Statement stmt = conn.createStatement();
			// Test Set
//		    ResultSet rs = stmt.executeQuery( "SELECT * FROM subtables.analog_values" );
		    // Training Set
		    ResultSet rs = stmt.executeQuery( "SELECT * FROM subtables." + TableName);
		    ResultSetMetaData rsmd = rs.getMetaData();
		    
		    
		    
		    // Fill Database Result in Guava Table
		    Integer row = 0; //Time
			String column = ""; //name or rdfid
			String value = ""; // value of measurement
			while ( rs.next() ) 
			{
				row = rs.getInt("time");
				column = rs.getString("name");
				value = rs.getString("value");
				
//				System.out.println(" r: " + row + " col: " + column + " val: " + value);
				
				input.put(row.toString(), column, value);
			}
		}
		catch (Throwable ignore) 
		{
			System.out.println("Error reading from DB: " + ignore.toString());
		}
		return input;
	}

}
