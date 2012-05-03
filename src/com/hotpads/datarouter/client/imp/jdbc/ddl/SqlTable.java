package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;

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

	public static SqlTable parseCreateTable(String phrase){
		
		SqlTable table = new SqlTable("Table");
		List<SqlColumn> columns = ListTool.createArrayList();
		
		
		for(String s:TestParser.getColumns(TestParser.getBody(phrase))){
			if(StringTool.containsAnyNonSpaceCharacters(s)){
				SqlColumn col = new SqlColumn(TestParser.getNameOfColumn(s), MySqlColumnType.parse(TestParser.getTypeOfColumn(s)));
				if(hasAMaxValue(s)){
					col.setMaxLength(Integer.parseInt(TestParser.getMaxValueOfColumn(s)));
				}
				col.setNullable(TestParser.getNullable(s));
				System.out.println(col);
				columns.add(col);
			}
		}
		table.setColumns(columns);
		
		// FOR THE PRIMARY KEY DECLARATION
		String[] sTokenPKey= TestParser.getPrimaryKeyDeclarationFromFullBody(phrase).split("[,()]");
		for (int i = 0; i < sTokenPKey.length; i++) {
			table.setPrimaryKey(TestParser.removeNonText(sTokenPKey[i]));
		}
		// FOR THE OTHER KEY DECLARATION 
		List<String> sTokenKey= TestParser.getKeyDeclarationsFromFullBody(phrase);
		for (String s1: sTokenKey) {
				SqlIndex tableIndex = new SqlIndex(TestParser.getKeyNameFromKeydeclaration(s1));
				System.out.println(TestParser.getKeyNameFromKeydeclaration(s1));
				for(String s2:TestParser.getKeyColumnsNamesFromKeyDeclaration(s1)){
					TestParser.addAppropriateColumnToIndexFromListOfColumn(tableIndex,s2,table.getColumns());
				}
				table.addIndex(tableIndex);
		}
		return table;
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
				List<SqlColumn> list = ListTool.createArrayList();
				list.add(col);
				this.primaryKey =  new SqlIndex(name+" Primary Key", list);
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
	
	
	public void setPrimaryKey(SqlIndex primaryKey) {
		this.primaryKey = primaryKey;
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

		/**
		 * 
		 * @param s
		 * @return true if the column type has a maximum value
		 */
		private static boolean hasAMaxValue(String s) {
			// TODO Auto-generated method stub
			return s.contains("(");
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
		@Test public void testParseCreateTable() throws IOException{
			FileInputStream fis = new FileInputStream("src/com/hotpads/datarouter/client/imp/jdbc/ddl/test3.txt");
			  DataInputStream in = new DataInputStream(fis);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String str, phrase="";
			while((str=br.readLine()) != null) {
				phrase+=str;
			}
			System.out.println(parseCreateTable(phrase));
		}
	}

	public void addIndex(SqlIndex tableIndex) {
		indexes.add(tableIndex);
		
	}

	@Override
	public String toString() {
		return "SqlTable [name=" + name + ", \n columns=" + columns
				+ ",\n primaryKey=" + primaryKey + ", \nindexes=" + indexes + "]";
	}
	
	
}
