package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;

public class SqlCreateTableParser{

	/************************** fields *************************/
	
	protected String input;

	
	/******************* constructors ****************************/
	
	public SqlCreateTableParser(String input){
		this.input = input;
	}
	
	
	/****************** primary method ****************************/

	public SqlTable parse() {
		SqlTable table = new SqlTable(getTableName());
		List<SqlColumn> columns = ListTool.createArrayList();

		
		for(String s:getColumns()){
			if(StringTool.containsAnyNonSpaceCharacters(s)){
				SqlColumn col = new SqlColumn(getNameOfColumn(s), MySqlColumnType.parse(getTypeOfColumn(s)));
				if(hasAMaxValue(s)){
					col.setMaxLength(Integer.parseInt(getMaxValueOfColumn(s)));
				}
				col.setNullable(TestParser.getNullable(s));
				System.out.println(col);
				columns.add(col);
			}
		}
		table.setColumns(columns);
		
		// FOR THE PRIMARY KEY DECLARATION
		String[] sTokenPKey= TestParser.getPrimaryKeyDeclarationFromFullBody(input).split("[,()]");
		for (int i = 0; i < sTokenPKey.length; i++) {
			table.setPrimaryKey(TestParser.removeNonText(sTokenPKey[i]));
		}
		// FOR THE OTHER KEY DECLARATION 
		List<String> sTokenKey= TestParser.getKeyDeclarationsFromFullBody(input);
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
	
	/********************* helper methods *******************************/
	
	protected String getHeader(){
		int index = input.indexOf('(');
		return input.substring(0, index);
	}
	
	protected String getTableName() {
		String header = getHeader();
		String name = header.split("[`]+")[1];
		return name;
	}

	/**
	 * 
	 * @param phrase
	 *            is what we got for queries of the type " show create table nameOfTheTable "
	 * @return the tail of the SQL phrase, i.e. things after the last ")"
	 */
	protected String getTail(){
		int index = input.lastIndexOf(')');
		return input.substring(index + 1);
	}

	/**
	 * 
	 * @param phrase
	 *            is what we got for queries of the type " show create table nameOfTheTable "
	 * @return the full body of the SQL phrase, i.e. things between the first "(" and the last ")"
	 */
	protected String getFullBody(){
		int index1 = input.indexOf('('), index2 = input.lastIndexOf(')');
		return input.substring(index1 + 1, index2);
	}

	protected String getBody(){
		int index1 = input.indexOf('('), index2 = input.toUpperCase().indexOf("PRIMARY");
		return input.substring(index1 + 1, index2);
	}

	protected String[] getColumns(){
		return getBody().split("[,]+");
	}
	
	
	/******************** static methods **************************/

	protected static String getNameOfColumn(String s) {
		 int index = s.indexOf('`');
		 String[] tokens = s.substring(index+1).split("[`]+");
		return tokens[0];
	}
	
	protected static String getTypeOfColumn(String s) {
		 int index = s.lastIndexOf('`');
		String[] tokens = s.substring(index+1).split("[ ()]+");
		return tokens[1];
	}
	
	protected static boolean hasAMaxValue(String s) {
		return s.contains("(");
	}
	
	protected static String getMaxValueOfColumn(String s) {
				 int index = s.lastIndexOf('`');
				String[] tokens = s.substring(index+1).split("[ ()]+");
				return tokens[2];
	}
		
	
	/******************** tests *****************************/
	
	public static class SqlCreateTableParserTests{
		@Test public void testParse() {
			String sql = "CREATE TABLE `Model` (" +
					"`includeInSummary` tinyint(1) DEFAULT NULL," +
					"`feedModelId` varchar(100) NOT NULL DEFAULT ''," +
					" PRIMARY KEY (`feedId`,`feedListingId`,`feedModelId`)" +
					") ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 397312 kB'";
			SqlCreateTableParser parser = new SqlCreateTableParser(sql);
			SqlTable table = parser.parse();
						
			//tableName
			Assert.assertEquals("Model",parser.getTableName());
			
			//number of columns
			Assert.assertEquals(2, CollectionTool.size(table.getColumns()));
			
			//column names
			Assert.assertEquals("includeInSummary", table.getColumns().get(0).getName());
			Assert.assertEquals("feedModelId", table.getColumns().get(1).getName());
			
			//column types
			Assert.assertEquals("TINYINT", table.getColumns().get(0).getType().toString());
			Assert.assertEquals("VARCHAR", table.getColumns().get(1).getType().toString());
			
			//column lengths
			Assert.assertEquals(1, table.getColumns().get(0).getMaxLength());
			Assert.assertEquals(100, table.getColumns().get(1).getMaxLength());
			
			//column nullability
			Assert.assertEquals(true, table.getColumns().get(0).getNullable());
			Assert.assertEquals(false, table.getColumns().get(1).getNullable());
						
			sql = "CREATE TABLE `Property` (" +
					"`id` bigint(20) NOT NULL AUTO_INCREMENT," +
					"`acres` double DEFAULT NULL," +
					"  PRIMARY KEY (`id`)" +
					" KEY `blabla` (`id`,`acres`)," +
					") ENGINE=InnoDB DEFAULT CHARSET=latin1 |";
			parser = new SqlCreateTableParser(sql);
			table = parser.parse();
			
			//primary keys
			Assert.assertEquals("id", table.getPrimaryKey().getColumns().get(0).getName());
			
			//secondary indexes
			Assert.assertEquals(1, table.getIndexes().size());
			Assert.assertEquals("id", table.getIndexes().get(0).getColumns().get(0).getName());
			Assert.assertEquals("acres", table.getIndexes().get(0).getColumns().get(1).getName());
		}
	}
}
