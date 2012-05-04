package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;

public class SqlCreateTableParser{

	protected String input;

	public SqlCreateTableParser(String input){
		this.input = input;
	}

	public String getHeader(){
		int index = input.indexOf('(');
		return input.substring(0, index);
	}
	
	public String getTableName() {
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
	public String getTail(){
		int index = input.lastIndexOf(')');
		return input.substring(index + 1);
	}

	/**
	 * 
	 * @param phrase
	 *            is what we got for queries of the type " show create table nameOfTheTable "
	 * @return the full body of the SQL phrase, i.e. things between the first "(" and the last ")"
	 */
	public String getFullBody(){
		int index1 = input.indexOf('('), index2 = input.lastIndexOf(')');
		return input.substring(index1 + 1, index2);
	}
	
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

	public String getBody(){
		int index1 = input.indexOf('('), index2 = input.toUpperCase().indexOf("PRIMARY");
		return input.substring(index1 + 1, index2);
	}

	public String[] getColumns(){
		return getBody().split("[,]+");
	}

	public static String getNameOfColumn(String s) {
		 int index = s.indexOf('`');
		 String[] tokens = s.substring(index+1).split("[`]+");
		return tokens[0];
	}
	
	static String getTypeOfColumn(String s) {
		 int index = s.lastIndexOf('`');
		String[] tokens = s.substring(index+1).split("[ ()]+");
		return tokens[1];
	}
	
	private static boolean hasAMaxValue(String s) {
		return s.contains("(");
	}
	
	static String getMaxValueOfColumn(String s) {
				 int index = s.lastIndexOf('`');
				String[] tokens = s.substring(index+1).split("[ ()]+");
				return tokens[2];
	}
		
	public static class SqlCreateTableParserTests{
		@Test public void testParse() {
			String sql = "CREATE TABLE `Model` (" +
					"`includeInSummary` tinyint(1) DEFAULT NULL," +
					"`feedModelId` varchar(100) NOT NULL DEFAULT ''," +
					" PRIMARY KEY (`feedId`,`feedListingId`,`feedModelId`)" +
					") ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 397312 kB'";
			SqlCreateTableParser parser = new SqlCreateTableParser(sql);
			SqlTable table = parser.parse();
			
			
			// TESTS THAT COULD BE DONE ON THIS METHOD
			
			// TESTING THE NAME OF THE TABLE
																	//System.out.println(parser.getTableName());
			Assert.assertEquals("Model",parser.getTableName());
			// TESTING THE NUMBER OF THE COLUMS
																	//System.out.println(CollectionTool.size(table.getColumns()));
			Assert.assertEquals(2, CollectionTool.size(table.getColumns()));
			// TESTING THE NAMES OF THE COLUMNS
			
			// TESTING THE TYPES OF THE COLUMS
			
			// TESTING THE MAXIMUM LENGHTS OF THE COLUMNS
			
			// TESTING THE NULLQBILITY OF THE COLUMNS
			
			// TESTING THE PRIMARY KEYS (THE NAMES, THE NUMBER )
			
			// TESTING THE KEYS (THE NAMES, THE NUMBER_)
			
		}
	}
}
