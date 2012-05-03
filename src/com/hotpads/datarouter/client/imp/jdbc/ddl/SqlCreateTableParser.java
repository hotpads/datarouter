package com.hotpads.datarouter.client.imp.jdbc.ddl;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.CollectionTool;

public class SqlCreateTableParser{

	String input;

	public SqlCreateTableParser(String input){
		this.input = input;
	}

	public String getHeader(){
		int index = input.indexOf('(');
		return input.substring(0, index);
	}
	
	public String getName() {
		String header = getHeader();
		String name = header.substring("create table".length());
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
		SqlTable table = new SqlTable();
		table.setName(getName());
		ta
		//do parsing
		return table;
	}

	
	public static class SqlCreateTableParserTests{
		@Test public void testParse() {
			String sql = "create table SomeTable(.... )";
			SqlCreateTableParser parser = new SqlCreateTableParser(sql);
			SqlTable table = parser.parse();
			Assert.assertEquals(5, CollectionTool.size(table.getColumns()));
		}
	}
}
