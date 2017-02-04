package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlCreateTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlTableDiffGenerator;

public class SqlTable{

	private final String name;
	private final List<SqlColumn> columns;
	private final SqlIndex primaryKey;
	private final Set<SqlIndex> indexes;
	private final Set<SqlIndex> uniqueIndexes;
	private final MySqlCollation collation;
	private final MySqlCharacterSet characterSet;
	private final MySqlRowFormat rowFormat;
	private final MySqlTableEngine engine;

	public SqlTable(String name, SqlIndex primaryKey, List<SqlColumn> columns, Set<SqlIndex> indexes,
			Set<SqlIndex> uniqueIndexes, MySqlCharacterSet characterSet, MySqlCollation collation,
			MySqlRowFormat rowFormat, MySqlTableEngine engine){
		this.name = name;
		this.primaryKey = primaryKey;
		this.columns = columns;
		this.indexes = indexes;
		this.uniqueIndexes = uniqueIndexes;
		this.characterSet = characterSet;
		this.collation = collation;
		this.rowFormat = rowFormat;
		this.engine = engine;
	}

	public SqlTable addColumn(SqlColumn column){
		columns.add(column);
		return this;
	}

	public SqlTable addIndex(SqlIndex tableIndex){
		indexes.add(tableIndex);
		return this;
	}

	public SqlTable addUniqueIndex(SqlIndex tableUniqueIndex){
		uniqueIndexes.add(tableUniqueIndex);
		return this;
	}

	public boolean hasPrimaryKey(){
		return getPrimaryKey() != null && getPrimaryKey().getColumns().size() > 0;
	}

	public boolean containsColumn(String columnName){
		for(SqlColumn col : getColumns()){
			if(col.getName().equals(columnName)){
				return true;
			}
		}
		return false;
	}

	public boolean containsIndex(String string){
		for(SqlIndex index : getIndexes()){
			if(index.getName().equals(string)){
				return true;
			}
		}
		return false;
	}

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

	/********************** Object methods ****************************/

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("SqlTable name=").append(name).append(",\n");
		sb.append(new SqlCreateTableGenerator(this).generateDdl());
		return sb.toString();
	}

	@Override
	public boolean equals(Object otherObject){
		if(!(otherObject instanceof SqlTable)){
			return false;
		}
		SqlTable other = (SqlTable)otherObject;
		return !new SqlTableDiffGenerator(this, other).isTableModified();
	}

	/*************************** get/set ********************************/

	public String getName(){
		return name;
	}


	public List<SqlColumn> getColumns(){
		return columns;
	}

	public SqlIndex getPrimaryKey(){
		return primaryKey;
	}

	public Set<SqlIndex> getIndexes(){
		return indexes;
	}

	public Set<SqlIndex> getUniqueIndexes(){
		return uniqueIndexes;
	}

	public int getNumberOfColumns(){
		return getColumns().size();
	}

	public MySqlTableEngine getEngine(){
		return engine;
	}

	public MySqlCollation getCollation(){
		return collation;
	}

	public MySqlCharacterSet getCharacterSet(){
		return characterSet;
	}

	public MySqlRowFormat getRowFormat(){
		return rowFormat;
	}

	/******************** tests *********************************/
	public static class SqlTableTests{
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
}
