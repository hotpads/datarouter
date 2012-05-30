package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

import com.hotpads.util.core.ListTool;

public class LevenshteinDistance {
	 
	  public static int computeDistance(String s1, String s2) {
	    s1 = s1.toLowerCase();
	    s2 = s2.toLowerCase();
	 
	    int[] costs = new int[s2.length() + 1];
	    for (int i = 0; i <= s1.length(); i++) {
	      int lastValue = i;
	      for (int j = 0; j <= s2.length(); j++) {
	        if (i == 0)
	          costs[j] = j;
	        else {
	          if (j > 0) {
	            int newValue = costs[j - 1];
	            if (s1.charAt(i - 1) != s2.charAt(j - 1))
	              newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
	            costs[j - 1] = lastValue;
	            lastValue = newValue;
	          }
	        }
	      }
	      if (i > 0)
	        costs[s2.length()] = lastValue;
	    }
	    return costs[s2.length()];
	  }
	 
	  public static int computeDistance(SqlTable table1, SqlTable table2) {

		    int[] costs = new int[table2.getNumberOfColumns() + 1];
		    for (int i = 0; i <= table1.getNumberOfColumns(); i++) {
		      int lastValue = i;
		      for (int j = 0; j <= table2.getNumberOfColumns(); j++) {
		        if (i == 0)
		          costs[j] = j;
		        else {
		          if (j > 0) {
		            int newValue = costs[j - 1];
		            if (table1.getColumns().get(i - 1) != table2.getColumns().get(j - 1))
		              newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
		            costs[j - 1] = lastValue;
		            lastValue = newValue;
		          }
		        }
		      }
		      if (i > 0)
		        costs[table2.getNumberOfColumns()] = lastValue;
		    }
		    return costs[table2.getNumberOfColumns()];
		  }
	  
	  public static int computeDistance(SqlColumn table1, SqlColumn table2) {
		  return computeDistance( table1.getName(),  table2.getName()) ;
	  }
	  
	  public static <T> int computeDistance(T o1 , T o2) {
		return 0;
		 
		//		    int[] costs = new int[o2.length() + 1];
		//		    for (int i = 0; i <= o1.length(); i++) {
		//		      int lastValue = i;
		//		      for (int j = 0; j <= o2.length(); j++) {
		//		        if (i == 0)
		//		          costs[j] = j;
		//		        else {
		//		          if (j > 0) {
		//		            int newValue = costs[j - 1];
		//		            if (o1.charAt(i - 1) != o2.charAt(j - 1))
		//		              newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
		//		            costs[j - 1] = lastValue;
		//		            lastValue = newValue;
		//		          }
		//		        }
		//		      }
		//		      if (i > 0)
		//		        costs[o2.length()] = lastValue;
		//		    }
		//		    return costs[o2.length()];
		  }
	  
	  public static void printDistance(String s1, String s2) {
	    System.out.println(s1 + "-->" + s2 + ": " + computeDistance(s1, s2));
	  }
	 
	  public static void printDistance(SqlTable s1, SqlTable s2) {
		    System.out.println(s1 + "-->" + s2 + ": " + computeDistance(s1, s2));
		  }
	  
	  public static void main(String[] args) {
	    printDistance("kitten", "sitting");
	    printDistance("rosettacode", "raisethysword");
	    printDistance(new StringBuilder("rosettacode").reverse().toString(), new StringBuilder("raisethysword").reverse().toString());
	    printDistance("mySuperCoolId","myMegaSuperCoolId");
	    
	    SqlColumn 
		colA = new SqlColumn("A", MySqlColumnType.BIGINT),
		colB = new SqlColumn("B", MySqlColumnType.VARCHAR,250,false),
		colC = new SqlColumn("C", MySqlColumnType.BOOLEAN),
		colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
		List<SqlColumn> 
				listBC = ListTool.createArrayList(),
				listM = ListTool.createArrayList();

		listBC.add(colB);
		listBC.add(colC);
		listM.add(colM);
		SqlTable 
				table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC),
				table2 = new SqlTable("TB").addColumn(colA).addColumn(colM);
		
	    printDistance(table1,table2); 
	    for (int i = 1; i < args.length; i += 2)
	      printDistance(args[i - 1], args[i]);
	  }
	}