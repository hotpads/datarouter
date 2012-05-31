package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.ListTool;

public class SqlTable {
	
	/***************** fields *****************************/
	
	private String name;
	private List<SqlColumn> columns;
	private SqlIndex primaryKey;
	private List<SqlIndex> indexes;
//	private MySqlTableEngine engine;
//	private MySqlCollation collation;
	
	
	/*************** constructors ****************************/
	
	public SqlTable(String name, List<SqlColumn> columns, SqlIndex primaryKey, List<SqlIndex> indexes) {
		this.name = name;
		this.columns = columns;
		this.primaryKey = primaryKey;
		this.indexes = indexes;
	}

	
	public SqlTable(String name, List<SqlColumn> columns, SqlIndex primaryKey) {
		this.name = name;
		this.columns = columns;
		this.primaryKey = primaryKey;
		this.indexes = ListTool.createArrayList();
	}


	public SqlTable(String name, List<SqlColumn> columns) {
		this.name = name;
		this.columns = columns;
		this.primaryKey = new SqlIndex("PRIMARY");
		this.indexes = ListTool.createArrayList();
	}
	
	public SqlTable(String name) {
		this.name = name;
		this.columns = ListTool.createArrayList();
		this.primaryKey = new SqlIndex("PRIMARY");
		this.indexes = ListTool.createArrayList();
	}
	
	
	/*************** methods *********************************/
	
	public String getCreateTable(){
		return null;
	}
	
	public void setPrimaryKey(String primaryKeyColumnName){
		for(SqlColumn col : columns){
			//System.out.println(" col " + col +" pkeyColumnName " + primaryKeyColumnName);
			if(col.getName().equals(primaryKeyColumnName)){
				List<SqlColumn> list = ListTool.createArrayList();
				list.add(col);
				if(primaryKey==null){
					primaryKey = new SqlIndex(name + "_Primary_Key", list);
				}
				else{
					primaryKey.addColumn(col);
				}
				//System.out.println("The primary key is " + col);
			}
		}
	}
	
	public SqlTable addColumn(SqlColumn column) {
		columns.add(column);
		return this;
	}

	public SqlTable addIndex(SqlIndex tableIndex) {
		indexes.add(tableIndex);
		return this;
	}
	
	
	/******************* static methods ***************************/

	//text before the first parenthesis, example "show create table Zebra"
	public static String getHeader(String phrase){
		int index = phrase.indexOf('(');
		return phrase.substring(0, index);
	}
	 
	//text inside the parentheses which is a csv separated list of column definitions
	public static String getColumnDefinitionSection(String phrase){
		int index1 = phrase.indexOf('('), index2 = phrase.lastIndexOf(')');
		return phrase.substring(index1 + 1, index2);
	}

	//text after the closing parenthesis. specifies table engine, charset, collation
	public static String getTail(String phrase){
		int index = phrase.lastIndexOf(')');
		return phrase.substring(index + 1);
	}

	//for example, "varchar(100)" has a maxValue of 100 while "datetime" does not have a maxValue
	@SuppressWarnings("unused") 
	private static boolean hasAMaxValue(String columnDefinitioin){
		return columnDefinitioin.contains("(");
	}
	
	
	/********************** Object methods ****************************/

	@Override
	public String toString() {
		String s =  "SqlTable name=" + name + ",\n" ;
				for(SqlColumn col : getColumns()){
					s+=col + "\n";
				}
				s+= "PK=" + primaryKey + "\nindexes=" + indexes ;
				return s;
	}
	
	@Override
	public boolean equals(Object otherObject){
		if(!(otherObject instanceof SqlTable)) { return false; }
		SqlTable other = (SqlTable)otherObject;
		return ! new SqlTableDiffGenerator(this, other,true).isTableModified();
	}
	
	
	/*************************** get/set ********************************/
	
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

	public List<SqlIndex> getIndexes(){
		return indexes;
	}

	public void setIndexes(List<SqlIndex> indexes){
		this.indexes = indexes;
	}

	public SqlTable setPrimaryKey(SqlIndex primaryKey){
		this.primaryKey = primaryKey;
		return this;
	}

	public int getNumberOfColumns() {
		return getColumns().size();
	}
	/******************** tests *********************************/

	public static class SqlTableTester{
		@Test
		public void testGetHeader(){
			Assert.assertEquals("Header", getHeader("Header(blabla(blob()))trail"));
		}

		@Test
		public void testGetTail(){
			Assert.assertEquals("trail", getTail("Header(blabla(blob()))trail"));
		}

		@Test
		public void testGetFullBody(){
			Assert.assertEquals("blabla(blob())", getColumnDefinitionSection("Header(blabla(blob()))trail"));
		}
	}
	
	public boolean hasPrimaryKey() {
		// TODO Auto-generated method stub
		return getPrimaryKey()!=null && getPrimaryKey().getColumns().size()>0;
	}

}
