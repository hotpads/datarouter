package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn.SqlColumnNameTypeComparator;
import com.hotpads.datarouter.util.core.DrComparableTool;

public class SqlIndex implements Comparable<SqlIndex>{

	private String name;
	private List<SqlColumn> columns;

	public SqlIndex(String name, List<SqlColumn> columns){
		this.name = name;
		this.columns = columns;
	}

	public SqlIndex(String name){
		this.name = name;
		this.columns = new ArrayList<>();
	}

	public SqlIndex addColumn(SqlColumn col){
		columns.add(col);
		return this;
	}

	/******************* Object methods **********************/

	@Override
	public String toString(){
		return "`" + name + "` , (" + columns + ")";
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + (columns == null ? 0 : columns.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		return result;
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
		if(columns == null){
			if(other.columns != null){
				return false;
			}
		}else if(!columns.equals(other.columns)){
			return false;
		}
		if(name == null){
			if(other.name != null){
				return false;
			}
		}else if(!name.equals(other.name)){
			return false;
		}
		return true;
	}


	/****************** get/set ****************************/

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
		public void equalsTester(){
			SqlColumn columnA = new SqlColumn("a", MySqlColumnType.BIGINT);
			SqlColumn columnB = new SqlColumn("b", MySqlColumnType.BIGINT);
			SqlColumn aa = new SqlColumn("a", MySqlColumnType.VARBINARY);
			SqlColumn bb = new SqlColumn("b", MySqlColumnType.VARCHAR);
			SqlIndex index1 = new SqlIndex("index");
			SqlIndex index2 = new SqlIndex("index");

			index1.addColumn(columnA).addColumn(columnB);
			index2.addColumn(aa).addColumn(bb);

			Assert.assertTrue(index1.equals(index2));
		}
	}

}
