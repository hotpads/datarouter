package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MathTool;

@SuppressWarnings("rawtypes") 
public class SqlIndex implements Comparable{
	
	/********************** fields *************************/
	
	protected String name;
	protected List<SqlColumn> columns;
	
	
	/********************** constructors **********************/
	
	public SqlIndex(String name, List<SqlColumn> columns) {
		super();
		this.name = name;
		this.columns = columns;
	}

	public SqlIndex(String name) {
		super();
		this.name = name;
		this.columns=ListTool.createArrayList();
	}

	
	/******************* methods ****************************/
	
	public SqlIndex addColumn(SqlColumn col){
		columns.add(col);
		return this;
	}

	
	/******************* Object methods **********************/
	
	@Override
	public String toString() {
		return "`" + name + "` , (" + columns + ")";
	}
	
	
	/****************** get/set ****************************/
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SqlColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<SqlColumn> columns) {
		this.columns = columns;
	}
	
	public int getNumberOfColumns(){
		return CollectionTool.size(columns);
	}

	/******************* comparator *************************/
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columns == null) ? 0 : columns.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SqlIndex)) {
			return false;
		}
		SqlIndex other = (SqlIndex) obj;
		if (columns == null) {
			if (other.columns != null) {
				return false;
			}
		} else if (!columns.equals(other.columns)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(Object o) {
			int c = ComparableTool.nullFirstCompareTo(name, ((SqlIndex) o).name);
			if(c!=0){return c;}
			for(int i=0; i<MathTool.min(columns.size(), ((SqlIndex) o).columns.size()); i++){
				c=ComparableTool.nullFirstCompareTo(columns.get(i),((SqlIndex) o).columns.get(i));
				if(c!=0){return c;}
			}
			return 0;
	}
	
	public static class TestSqlIndex{
		@Test public void equalsTester(){
			SqlColumn a = new SqlColumn("a", MySqlColumnType.BIGINT),
					b = new SqlColumn("b", MySqlColumnType.BIGINT),
					aa=new SqlColumn("a", MySqlColumnType.VARBINARY), 
					bb=new SqlColumn("b", MySqlColumnType.VARCHAR);
			SqlIndex index1 = new SqlIndex("index"), 
					index2= new SqlIndex("index");
			
			index1.addColumn(a).addColumn(b);
			index2.addColumn(aa).addColumn(bb);
			
			Assert.assertTrue(index1.equals(index2));
		}
	}

}
