import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Table;

public class TableToCSV {
	
	// stores a guava table to a csv
	public void write(Table<String, String, String> table, String outputFilePath)
	{   
		try 
		{
			Writer out = new BufferedWriter(new FileWriter(outputFilePath));
			List<String> values = new ArrayList();
			
			//Header
			values.add("");
			for (String columnKey : table.columnKeySet())
			{
				values.add(columnKey);
			}
			writeLine(out, values);
			values.clear();
			
			//rows and values
			for (String rowKey : table.rowKeySet())
			{
				values.add(rowKey);
				for (String columnKey : table.columnKeySet())
				{
					values.add(table.get(rowKey, columnKey));
				}
				writeLine(out, values);
				values.clear();
			}
			
			out.close();
		}
		catch (final Exception e) 
		{
		    e.printStackTrace();
		}
	}
	
	// writes line in csv file with given values
	public void writeLine(Writer w, List<String> values) 
	        throws Exception
    {
        boolean firstVal = true;
        for (String val : values)  {
            if (!firstVal) {
                w.write(",");
            }
            w.write("\"");
            if(val != null && !val.isEmpty())
            {
            	for (int i=0; i<val.length(); i++) {
        		char ch = val.charAt(i);
    			if (ch=='\"') 
    			{
    				w.write("\"");  //extra quote
    			}
    			w.write(ch);
          }
            }     
            w.write("\"");
            firstVal = false;
        }
        w.write("\n");
    }
}
