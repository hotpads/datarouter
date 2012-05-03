package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.ListTool;

public class SqlTable {
	
	// ATTRIBUTES
	String name;
	List<SqlColumn> columns;
	SqlIndex primaryKey;
	List<SqlIndex> indexes;
	
	// CONSTRUCTORS
	public SqlTable(String name, List<SqlColumn> columns, SqlIndex primaryKey,
			List<SqlIndex> indexes) {
		super();
		this.name = name;
		this.columns = columns;
		this.primaryKey = primaryKey;
		this.indexes = indexes;
	}

	public SqlTable(String name, List<SqlColumn> columns) {
		super();
		this.name = name;
		this.columns = columns;
		this.indexes = ListTool.createArrayList();
	}
	
	public SqlTable(String name) {
		super();
		this.name = name;
		this.columns = ListTool.createArrayList();
		this.indexes = ListTool.createArrayList();
	}

	public static SqlTable parseCreateTable(String createTableStatement){
		return null;
	}
	public String getCreateTable(){
		return null;
	}

	// GETTERS & SETTERS
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

	public SqlIndex getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String s) {
		for(SqlColumn col : columns){
			if(col.getName().equals(s)){
				//this.primaryKey =  col;
				System.out.println("The primary key is " + col);
			}
		}
			}

	public List<SqlIndex> getIndexes() {
		return indexes;
	}

	public void setIndexes(List<SqlIndex> indexes) {
		this.indexes = indexes;
	}
	
	/**
	 * 
	 * @param phrase is what we got for queries of the type " show create table nameOfTheTable " 
	 * @return the  header of the SQL phrase, i.e. things before the first "(" 
	 */
	 public static String  getHeader(String phrase){
		    int index = phrase.indexOf('(');
			return phrase.substring(0,index);
	 }
	 /**
	  * 
	  * @param phrase is what we got for queries of the type " show create table nameOfTheTable " 
	  * @return the tail of the SQL phrase, i.e. things after the last ")" 
	  */
	 public static String getTail(String phrase){
		 int index = phrase.lastIndexOf(')');
			return phrase.substring(index+1);
	 }
	 
	 
	 /**
	  * 
	  * @param phrase is what we got for queries of the type " show create table nameOfTheTable " 
	  * @return the full body of the SQL phrase, i.e. things between the first "(" and the last ")" 
	  */
	 public static String getFullBody(String phrase){
		    int index1 = phrase.indexOf('('), index2=phrase.lastIndexOf(')');
			return phrase.substring(index1+1,index2);
	 }

	@Override
	public String toString() {
		return "SqlTable [name=" + name + ", columns=" + columns + "]";
	}
	
	public static class SqlTableTests{
		@Test public void testGetHeader() {
			Assert.assertEquals("Header", getHeader("Header(blabla(blob()))trail"));
		}
		@Test public void testGetTail() {
			Assert.assertEquals("trail", getTail("Header(blabla(blob()))trail"));
		}
		@Test public void testGetFullBody() {
			Assert.assertEquals("blabla(blob())", getFullBody("Header(blabla(blob()))trail"));
		}
	}
	
}
