package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn.SqlColumnNameTypeComparator;
import com.hotpads.datarouter.util.core.DrComparableTool;

public class SqlIndex implements Comparable<SqlIndex>{

	private final String name;
	private final List<SqlColumn> columns;

	public SqlIndex(String name, List<SqlColumn> columns){
		this.name = name;
		this.columns = columns;
	}

	@Override
	public String toString(){
		return "`" + name + "` , (" + columns + ")";
	}

	@Override
	public int hashCode(){
		return Objects.hash(name, columns);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(!(obj instanceof SqlIndex)){
			return false;
		}
		SqlIndex other = (SqlIndex)obj;
		return Objects.equals(name, other.name)
				&& Objects.equals(columns, other.columns);
	}


	public String getName(){
		return name;
	}

	public List<SqlColumn> getColumns(){
		return columns;
	}

	/********************* comparators **********************************/

	public static class SqlIndexNameComparator implements Comparator<SqlIndex>{

		@Override
		public int compare(SqlIndex index1, SqlIndex index2){
			int diff = DrComparableTool.nullFirstCompareTo(index1.getName(), index2.getName());
			if(diff != 0){
				return diff;
			}
			SqlColumnNameTypeComparator nameTypeColumnComparator = new SqlColumnNameTypeComparator(true);
			for(int i = 0; i < Math.min(index1.getColumns().size(), index2.getColumns().size()); i++){
				diff = DrComparableTool.nullFirstCompareTo(index1.getColumns().get(i), index2.getColumns().get(i));
				if(index1.getColumns().get(i) == null && index2.getColumns().get(i) == null){
					diff = 0;
				}else if(index1.getColumns().get(i) == null){
					diff = -1;
				}else if(index2.getColumns().get(i) == null){
					diff = 1;
				}else{
					diff = nameTypeColumnComparator.compare(index1.getColumns().get(i), index2.getColumns().get(i));
				}
				if(diff != 0){
					return diff;
				}
			}
			return 0;
		}

	}

	@Override
	public int compareTo(SqlIndex obj){
		int diff = DrComparableTool.nullFirstCompareTo(name, obj.name);
		if(diff != 0){
			return diff;
		}
		SqlColumnNameTypeComparator nameTypeColumnComparator = new SqlColumnNameTypeComparator(true);
		for(int i = 0; i < Math.min(columns.size(), obj.columns.size()); i++){
			diff = DrComparableTool.nullFirstCompareTo(columns.get(i), obj.columns.get(i));
			if(columns.get(i) == null && obj.columns.get(i) == null){
				diff = 0;
			}else if(columns.get(i) == null){
				diff = -1;
			}else if(obj.columns.get(i) == null){
				diff = 1;
			}else{
				diff = nameTypeColumnComparator.compare(columns.get(i), obj.columns.get(i));
			}
			if(diff != 0){
				return diff;
			}
		}
		return 0;
	}


	/**************************** tests *******************************************/

	public static class SqlIndexTests{
		@Test
		public void testEquals(){
			SqlColumn columnA = new SqlColumn("a", MySqlColumnType.BIGINT);
			SqlColumn columnB = new SqlColumn("b", MySqlColumnType.BIGINT);
			SqlColumn aa = new SqlColumn("a", MySqlColumnType.VARBINARY);
			SqlColumn bb = new SqlColumn("b", MySqlColumnType.VARCHAR);
			SqlIndex index1 = new SqlIndex("index", Arrays.asList(columnA, columnB));
			SqlIndex index2 = new SqlIndex("index", Arrays.asList(aa, bb));
			Assert.assertEquals(index1, index2);
		}
	}

}
