package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class SqlColumn implements Comparable<SqlColumn>{

	/********************** fields *************************/
	
	protected String name;
	protected MySqlColumnType type;
	protected Integer maxLength;
	protected Boolean nullable = true;
	protected Boolean autoIncrement = false;

	
	/********************** construct **********************/
	
	public SqlColumn(String name, MySqlColumnType type, Integer maxLength, Boolean nullable, Boolean autoIncrement){
		this.name = name;
		this.type = type;
		this.maxLength = maxLength;
		this.nullable = nullable;
		this.autoIncrement = autoIncrement;
	}

	public SqlColumn(String name, MySqlColumnType type){
		this.name = name;
		this.type = type;
	}


	/******************* Object methods **********************/
	
	@Override
	public String toString(){
		int i =Integer.MAX_VALUE;
		return "\t[" + name + ", " + type + ", "
				+ maxLength + ", " + nullable +  ", " + autoIncrement + "]";
		//		return "SqlColumn [name=" + name + ", Type=" + type + ", MaxLength="
		//				+ maxLength + ", nullable=" + nullable + "]";
	}

	public SqlColumn clone(){
		return new SqlColumn(getName(), getType(), getMaxLength(), getNullable(), getAutoIncrement());
	}
	
	@Override
	public boolean equals(Object otherObject){
		if(!(otherObject instanceof SqlColumn)){ return false; }
		// //return 0==compareTo((SqlColumn)otherObject);
		return 0 == new SqlColumnNameComparator(true).compare(this,(SqlColumn) otherObject);
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((maxLength == null)?0:maxLength.hashCode());
		result = prime * result + ((name == null)?0:name.hashCode());
		result = prime * result + ((nullable == null)?0:nullable.hashCode());
		result = prime * result + ((type == null)?0:type.hashCode());
		result = prime * result + ((autoIncrement == null)?0:autoIncrement.hashCode());
		return result;
	}

	
	/******************* comparator *************************/
	
	@Override
	public int compareTo(SqlColumn other){
		int c = DrComparableTool.nullFirstCompareTo(name, other.name);
		if(c!=0){ return c; }
		c = DrComparableTool.nullFirstCompareTo(type, other.type);
		if(c!=0){ return c; }
		c = DrComparableTool.nullFirstCompareTo(maxLength, other.maxLength);
		if(c!=0){ return c; }
		c = DrComparableTool.nullFirstCompareTo(nullable, other.nullable);
		if(c!=0){ return c; }
		c = DrComparableTool.nullFirstCompareTo(autoIncrement, other.autoIncrement);
		return c;	
	}
	
	public static class SqlColumnNameComparator implements Comparator<SqlColumn>{
		boolean caseSensitive = true;
		public SqlColumnNameComparator(boolean caseSensitive){
			this.caseSensitive = caseSensitive;
		}
		@Override
		public int compare(SqlColumn a, SqlColumn b){
			if(a==null && b==null) return 0;
			if(a==null)	return -1;
			if(b==null) return 1;
			if(caseSensitive){
				return DrComparableTool.nullFirstCompareTo(a.name, b.name);
			}
			return DrComparableTool.nullFirstCompareTo(DrStringTool.nullSafe(a.name).toLowerCase(), 
					DrStringTool.nullSafe(b.name).toLowerCase());
		}
	}
	
	public static class SqlColumnNameTypeComparator implements Comparator<SqlColumn>{
		boolean caseSensitive = true;
		public SqlColumnNameTypeComparator(boolean caseSensitive){
			this.caseSensitive = caseSensitive;
		}
		@Override
		public int compare(SqlColumn a, SqlColumn b){
			int c ;
			if(a==null && b==null) return 0;
			if(a==null)	return -1;
			if(b==null) return 1;
			if(caseSensitive){
			 c = DrComparableTool.nullFirstCompareTo(a.name, b.name);
			}else{
				c = DrComparableTool.nullFirstCompareTo(DrStringTool.nullSafe(a.name).toLowerCase(), 
						DrStringTool.nullSafe(b.name).toLowerCase());
			}
			if(c!=0){ return c; }
			return DrComparableTool.nullFirstCompareTo(a.type, b.type);
			
		}
	}
	
	public static class SqlColumnNameTypeLengthComparator implements Comparator<SqlColumn>{
		boolean caseSensitive = true;
		public SqlColumnNameTypeLengthComparator(boolean caseSensitive){
			this.caseSensitive = caseSensitive;
		}
		@Override
		public int compare(SqlColumn a, SqlColumn b){
			int c ;
			if(a==null && b==null) return 0;
			if(a==null)	return -1;
			if(b==null) return 1;
			if(caseSensitive){
			 c = DrComparableTool.nullFirstCompareTo(a.name, b.name);
			}else{
				c = DrComparableTool.nullFirstCompareTo(DrStringTool.nullSafe(a.name).toLowerCase(), 
						DrStringTool.nullSafe(b.name).toLowerCase());
			}
			if(c!=0){ return c; }
			c = DrComparableTool.nullFirstCompareTo(a.type, b.type);
			if(c!=0){ return c; }
			return DrComparableTool.nullFirstCompareTo(a.maxLength, b.maxLength);
		}
	}
	
	public static class SqlColumnNameTypeLengthAutoIncrementComparator implements Comparator<SqlColumn>{
		boolean caseSensitive = true;
		public SqlColumnNameTypeLengthAutoIncrementComparator(boolean caseSensitive){
			this.caseSensitive = caseSensitive;
		}
		@Override
		public int compare(SqlColumn a, SqlColumn b){
			int c ;
			if(a==null && b==null) return 0;
			if(a==null)	return -1;
			if(b==null) return 1;
			if(caseSensitive){
			 c = DrComparableTool.nullFirstCompareTo(a.name, b.name);
			}else{
				c = DrComparableTool.nullFirstCompareTo(DrStringTool.nullSafe(a.name).toLowerCase(), 
						DrStringTool.nullSafe(b.name).toLowerCase());
			}
			if(c!=0){ return c; }
			c = DrComparableTool.nullFirstCompareTo(a.type, b.type);
			if(c!=0){ return c; }
			c = DrComparableTool.nullFirstCompareTo(a.maxLength, b.maxLength);
			if(c!=0){return c; }
			return DrComparableTool.nullFirstCompareTo(a.autoIncrement, b.autoIncrement);
		}
	}
	
	/******************* get/set ****************************/
	
	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public MySqlColumnType getType(){
		return type;
	}

	public void setType(MySqlColumnType type){
		this.type = type;
	}

	public Integer getMaxLength(){
		return maxLength;
	}

	public void setMaxLength(Integer maxLength){
		this.maxLength = maxLength;
	}

	public Boolean getNullable(){
		return nullable;
	}

	public void setNullable(Boolean nullable){
		this.nullable = nullable;
	}

	
	public final Boolean getAutoIncrement(){
		return autoIncrement;
	}

	public final void setAutoIncrement(Boolean autoIncrement){
		this.autoIncrement = autoIncrement;
	}

	/******************* tests ***************************/
	
	public static class SqlColumnTests{
		@Test public void testCompareTo(){
			//two different values a, b
			SqlColumn a = new SqlColumn("a", MySqlColumnType.BIGINT, 19, false, false);
			SqlColumn b = new SqlColumn("b", MySqlColumnType.VARCHAR, 120, true, false);
			int c = a.compareTo(b);
			Assert.assertEquals(-1, c);
			Assert.assertFalse(a.equals(b));
			
			//new value a2 which equals a
			SqlColumn a2 = new SqlColumn("a", MySqlColumnType.BIGINT, 19, false, false);
			Assert.assertTrue(a2.equals(a));
			Assert.assertFalse(a2==a);
			
			//test adding to SortedSet to test compareTo method
			SortedSet<SqlColumn> columns = new TreeSet<>();
			columns.add(b);
			columns.add(a);//should keep this version of a/a2 since it was added first
			columns.add(a2);
			//			for(SqlColumn column : columns){
			//				System.out.println(column);
			//			}
			List<SqlColumn> columnList = DrListTool.createArrayList(columns);
			Assert.assertTrue(a==columnList.get(0));//it kept the first version
			Assert.assertTrue(b==columnList.get(1));
			
			//test list sorting
			List<SqlColumn> sortedList = new ArrayList<>();
			sortedList.add(b);
			sortedList.add(a);
			Assert.assertTrue(b==sortedList.get(0));
			Collections.sort(sortedList);
			Assert.assertTrue(b==sortedList.get(1));
		}
		@Test public void testMinus(){
			List<SqlColumn> a = DrListTool.create(
					new SqlColumn("a", MySqlColumnType.VARCHAR, 255, true, false));
					//a.add(new SqlColumn("b", MySqlColumnType.VARCHAR, 255, false));
			List<SqlColumn> b = DrListTool.create(
					new SqlColumn("a", MySqlColumnType.VARCHAR, 255, true, false));
			b.add(new SqlColumn("b", MySqlColumnType.VARCHAR, 250, false, false));			
			Collection<SqlColumn> minus = DrCollectionTool.minus(a, b);
			Assert.assertTrue(DrCollectionTool.isEmpty(minus));
		}
		@Test public void testComparators(){
			SqlColumn a = new SqlColumn("a", MySqlColumnType.BIGINT, 19, false, false);
			SqlColumn b = new SqlColumn("A", MySqlColumnType.VARCHAR, 120, true, false);
			Assert.assertTrue(new SqlColumnNameComparator(true).compare(a, b) != 0);
			Assert.assertTrue(new SqlColumnNameComparator(false).compare(a, b) == 0);
			
			Set<SqlColumn> caseSensitive = new TreeSet<SqlColumn>(new SqlColumnNameComparator(true));
			caseSensitive.add(a);
			caseSensitive.add(b);
			Assert.assertEquals(2, caseSensitive.size());
			
			Set<SqlColumn> caseInsensitive = new TreeSet<SqlColumn>(new SqlColumnNameComparator(false));
			caseInsensitive.add(a);
			caseInsensitive.add(b);
			Assert.assertEquals(1, caseInsensitive.size());
		}
		
		//TODO Test the auto-increment !
	}
	
}
