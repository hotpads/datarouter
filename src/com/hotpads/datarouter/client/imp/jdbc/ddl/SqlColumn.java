package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public class SqlColumn implements Comparable<SqlColumn>{

	/********************** fields *************************/
	
	protected String name;
	protected MySqlColumnType type;
	protected Integer maxLength;
	protected Boolean nullable;
	

	/********************** constructors **********************/
	
	public SqlColumn(String name, MySqlColumnType type, Integer maxLength,
			Boolean nullable) {
		this.name = name;
		this.type = type;
		this.maxLength = maxLength;
		this.nullable = nullable;
	}

	public SqlColumn(String name, MySqlColumnType type) {
		this.name = name;
		this.type = type;
	}


	/******************* Object methods **********************/
	
	@Override
	public String toString() {
		return "SqlColumn [name=" + name + ", Type=" + type + ", MaxLength="
				+ maxLength + ", nullable=" + nullable + "]";
	}

	
	/******************* comparator *************************/
	
	@Override
	public boolean equals(Object otherObject) {
		if(!(otherObject instanceof SqlColumn)) { return false; }
		return 0==compareTo((SqlColumn)otherObject);
	}


	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((maxLength == null)?0:maxLength.hashCode());
		result = prime * result + ((name == null)?0:name.hashCode());
		result = prime * result + ((nullable == null)?0:nullable.hashCode());
		result = prime * result + ((type == null)?0:type.hashCode());
		return result;
	}
	
	
	@Override
	public int compareTo(SqlColumn other){
		int c = ComparableTool.nullFirstCompareTo(name, other.name);
		if(c!=0) { return c; }
		c = ComparableTool.nullFirstCompareTo(type, other.type);
		if(c!=0) { return c; }
		c = ComparableTool.nullFirstCompareTo(maxLength, other.maxLength);
		if(c!=0) { return c; }
		c = ComparableTool.nullFirstCompareTo(nullable, other.nullable);
		return c;
	}
	
	
	/******************* get/set ****************************/
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MySqlColumnType getType() {
		return type;
	}

	public void setType(MySqlColumnType type) {
		this.type = type;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	public Boolean getNullable() {
		return nullable;
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

	
	/******************* tests ***************************/
	
	public static class SqlColumnTests{
		@Test public void testCompareTo() {
			//two different values a, b
			SqlColumn a = new SqlColumn("a", MySqlColumnType.BIGINT, 19, false);
			SqlColumn b = new SqlColumn("b", MySqlColumnType.VARCHAR, 120, true);
			int c = a.compareTo(b);
			Assert.assertEquals(-1, c);
			Assert.assertFalse(a.equals(b));
			
			//new value a2 which equals a
			SqlColumn a2 = new SqlColumn("a", MySqlColumnType.BIGINT, 19, false);
			Assert.assertTrue(a2.equals(a));
			Assert.assertFalse(a2==a);
			
			//test adding to SortedSet to test compareTo method
			SortedSet<SqlColumn> columns = SetTool.createTreeSet();
			columns.add(b);
			columns.add(a);//should keep this version of a/a2 since it was added first
			columns.add(a2);
//			for(SqlColumn column : columns) {
//				System.out.println(column);
//			}
			List<SqlColumn> columnList = ListTool.createArrayList(columns);
			Assert.assertTrue(a==columnList.get(0));//it kept the first version
			Assert.assertTrue(b==columnList.get(1));
			
			//test list sorting
			List<SqlColumn> sortedList = ListTool.createArrayList();
			sortedList.add(b);
			sortedList.add(a);
			Assert.assertTrue(b==sortedList.get(0));
			Collections.sort(sortedList);
			Assert.assertTrue(b==sortedList.get(1));
		}
	}
	
	
	
}
