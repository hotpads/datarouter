package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.ListTool;

public class TestParser {
	
	public static void main(String[] args) throws SQLException, IOException  {
		SqlTable table = new SqlTable("Table");
		List<SqlColumn> columns = ListTool.createArrayList();
	
		FileInputStream fis = new FileInputStream("src/com/hotpads/datarouter/client/imp/jdbc/ddl/test3.txt");
		// Get the object of DataInputStream
		  DataInputStream in = new DataInputStream(fis);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  String str, phrase="";
		while((str=br.readLine()) != null) {
			phrase+=str;
		}

		for(String s:getColumns(getBody(phrase))){
			if(isNotEmpty(s)){
				SqlColumn col = new SqlColumn(getNameOfColumn(s), MySqlColumnType.parse(getTypeOfColumn(s)));
				if(hasAMaxValue(s)){
					col.setMaxLength(Integer.parseInt(getMaxValueOfColumn(s)));
				}
				col.setNullable(getNullable(s));
				System.out.println(col);
				columns.add(col);
			}
		}
		table.setColumns(columns);
		
		// FOR THE PRIMARY KEY DECLARATION
		String[] sTokenPKey= getPrimaryKeyDeclarationFromFullBody(phrase).split("[,()]");
		for (int i = 0; i < sTokenPKey.length; i++) {
			table.setPrimaryKey(removeNonText(sTokenPKey[i]));
		}
		// FOR THE OTHER KEY DECLARATION 
		List<String> sTokenKey= getKeyDeclarationsFromFullBody(phrase);
		for (String s1: sTokenKey) {
				SqlIndex tableIndex = new SqlIndex(getKeyNameFromKeydeclaration(s1));
				System.out.println(getKeyNameFromKeydeclaration(s1));
				for(String s2:getKeyColumnsNamesFromKeyDeclaration(s1)){
					addAppropriateColumnToIndexFromListOfColumn(tableIndex,s2,table.getColumns());
				}
				table.addIndex(tableIndex);
		}
		
		System.out.println(table);
		
	}

	static void addAppropriateColumnToIndexFromListOfColumn(
			SqlIndex tableIndex, String s1, List<SqlColumn> columns) {
		for(SqlColumn col: columns){ //TODO can to distinct columns have the same name ?
			if(col.getName().equals(s1)) tableIndex.addColumn(col);
		}
	}

	static List<String> getKeyColumnsNamesFromKeyDeclaration(String string) {
		int index = string.indexOf("(");
		String[] sFinal = string.substring(index).split("[`]+");
		List<String> list = ListTool.createArrayList();
		for(String s:sFinal){
			s=removeNonText(s);
			if(isNotEmpty(s)) list.add(s); 
		}
		return list; 
	}

	static String getKeyNameFromKeydeclaration(String string) {
		String[] sToken = string.split("[`]+");
		return sToken[1];
	}

	/**
	 * 
	 * @param s
	 * @return true if it contains at least one character different than a space
	 */
	private static boolean isNotEmpty(String s) {
		for(char c:s.toCharArray()){
			if(c!=' ') return true;
		}
		return false;
	}


	@Deprecated
	/**
	 * OLD VERSION OF THE PARSING 
	 * @param phrase
	 * @return
	 */
	public static SqlTable parseAndCreateTable(String phrase){
		List<SqlColumn> columns = ListTool.createArrayList();
		String delims = "[(),]+";					// THE DELIMITERS FOR TOKENIZING
		String[] tokens = phrase.split(delims);		// TOKENIZING
		
		System.out.println("1st Parsing :");
		System.out.println("\n /****** Getting the name of the table ******/");		
		SqlTable table = new SqlTable("generated Sql Table");
		
		for(int i=1; i<tokens.length; i++){
			if(tokens[i].contains("PRIMARY KEY")){
				System.out.println();
				table.setPrimaryKey(removeNonText(tokens[++i]));
			}
			else if(tokens[i].contains("KEY")){ // INDEXES 
				
			}
			else{
				String[] tempTokens = tokens[i].split("[`]");
				String name=removeSpaces(tempTokens[1]), type;
				if(tempTokens[2].contains("datetime")){ // WHEN THERE'S NO MAX LENGTH THISE TOKEN CONTAINS CERTAIN TYPES , should add more types 
					type = removeSpaces(tempTokens[2]);
					
					SqlColumn col = new SqlColumn(name, MySqlColumnType.parse(type));
					// MAX LENGTH
					int maxLength = Integer.parseInt(tokens[++i]);
					//System.out.println(maxLength);
					col.setMaxLength(maxLength);
					// "NULLABLE OR NOT NULLABLE , ..."
					boolean nullable=true;
					if(tokens[++i].contains("NOT NULL")){
						nullable=false;
					}
					col.setNullable(nullable);
					System.out.println(col);
					columns.add(col);
				}
				else{  // THIS COLUMN HAS NO MAX VALUE
					String[] tempTokensBis = tempTokens[2].split("[ ]");
					type = removeSpaces(tempTokensBis[1]);
					SqlColumn col = new SqlColumn(name, MySqlColumnType.parse(type));
					// "NULLABLE OR NOT NULLABLE , ..."
					boolean nullable=true;
					for (int j = 0; j < tempTokensBis.length; j++) {
						if(tempTokensBis[j].contains("NOT"));
						nullable=false;
					}
					col.setNullable(nullable);
					System.out.println(col);
					columns.add(col);
				}
				table.setColumns(columns);
			}
		}
		return table;
	}
	
	
	/**
	 * 
	 * @param s  example:			KEY `index_yyyymmddhhmmss` (`year`,`month`,`date`,`hour`,`minute`,`second`),
	 * @return true if it's a declaration of an index
	 */
	 private static boolean isKeyDeclaration(String s) {
		// TODO Auto-generated method stub
		 return s.toUpperCase().startsWith("KEY") || s.toUpperCase().startsWith("KEY", 1)  || s.toUpperCase().startsWith("KEY", 2) ;
	}

	 /**
	  * 
	  * @param s example:   			   PRIMARY KEY (`id`),
	  * @return true if it's a primary key declaration
	  */
	private static boolean isPrimaryKeyDeclaration(String s) {
		// TODO Auto-generated method stub
		return s.toUpperCase().startsWith("PRIMARY") || s.toUpperCase().startsWith("PRIMARY", 1)  || s.toUpperCase().startsWith("PRIMARY", 2) ;
	}

	/**
	 * 
	 * @param s
	 * @return true if the column type can be null
	 */
	static boolean getNullable(String s) {
		// TODO Auto-generated method stub
		return !s.contains("NOT");
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

	/**
	 * 
	 * @param s
	 * @return the maximum value for the column type
	 */
	static String getMaxValueOfColumn(String s) {
		// TODO Auto-generated method stub
				 int index = s.lastIndexOf('`');
				String[] tokens = s.substring(index+1).split("[ ()]+");
				return tokens[2];
	}

	/**
	 *  
	 * @param s
	 * @return the type name of the column 
	 */
	static String getTypeOfColumn(String s) {
		// TODO Auto-generated method stub
		 int index = s.lastIndexOf('`');
		String[] tokens = s.substring(index+1).split("[ ()]+");
		return tokens[1];
	}


	 /**
	  * 
	  * @param s a column
	  * @return the name of the column
	  */
	static String getNameOfColumn(String s) {
		// TODO Auto-generated method stub
		 int index = s.indexOf('`');
		 String[] tokens = s.substring(index+1).split("[`]+");
		return tokens[0];
	}

	 
	 /**
	  * 
	  * @param  phrase is what we got for queries of the type " show create table nameOfTheTable " 
	  * @return the body of the SQL phrase, i.e. things between the first "(" and   "Primary Key"
	  */
	 public static String getBody(String phrase){
		    int index1 = phrase.indexOf('('), index2=phrase.toUpperCase().indexOf("PRIMARY");
			return phrase.substring(index1+1,index2);
	 }
	
	 /**
	  * 
	  * @param phrase is the full body
	  * @return
	  */
	 public static String getPrimaryKeyDeclarationFromFullBody(String phrase){
		    int index = phrase.toUpperCase().indexOf("PRIMARY");
		    String[] tokens = phrase.substring(index).split("[)]+");
			return tokens[0]+")";
			
	 }
	 
	 public static String getKeyDeclarationFromFullBody(String phrase){
		    int firstIndex = phrase.toUpperCase().indexOf("KEY"),
		    index = phrase.substring(firstIndex+1).toUpperCase().indexOf("KEY");
		    if(index>0){
		    	return phrase.substring(firstIndex).substring(index+1);
		    }
		    else{
		    	return "";
		    }
	 }
	 
	 public static List<String> getKeyDeclarationsFromFullBody(String phrase){
		 String [] tokens = getKeyDeclarationFromFullBody(SqlTable.getColumnDefinitionSection(phrase)).split("[)]");
		 List<String> keyDeclarationList = ListTool.createArrayList();
		 for(String s:tokens){
			 if(isNotEmpty(removeNonText(s))){
				 keyDeclarationList.add(s);
			 }
		 }
		 return keyDeclarationList ;
	 }
	 
	 /**
	  * 
	  * @param phrase is the body of an Sql  querie of the type " show create table nameOfTheTable " 
	  * @return list of Strings containing the column declarations, primary key declaration and key/indexes declaration 
	  */
	 static String[] getColumns(String phrase){
		return phrase.split("[,]+"); 
	 }
	
	 private static void testGetColumns(){
		 System.out.println(SqlTable.getColumnDefinitionSection("Header(blabla(blob()))trail"));
	 }
	
	 /**
	  * 
	  * @param s
	  * @return s without occurrences of space : " "
	  */
	private static String removeSpaces(String s) {
		String sResult="";
		for (int i = 0; i < s.length(); i++) {
			if(s.charAt(i)!=' '){
				sResult+=s.charAt(i);
			}
		}
		return sResult;
	}
	/**
	 * 
	 * @param s
	 * @return s without occurrences of " " and "" 
	 */
	static String removeNonText(String s) {
		String sResult="";
		for (int i = 0; i < s.length(); i++) {
			if(s.charAt(i)!=' ' && s.charAt(i)!='`' && s.charAt(i)!=',' && s.charAt(i)!='(' && s.charAt(i)!=')'){
				sResult+=s.charAt(i);
			}
		}
		return sResult;
	}

	public static class ParserTest{
		
		@Test public void testGetKeyDeclarationFromFullBody(){
			String s="CREATE TABLE `Inquiry` (" +
					"`ccEmailOpened` datetime DEFAULT NULL,"+
					"`userToken` varchar(255) DEFAULT NULL,"+
					"PRIMARXtY KEY (`id`),"+
					"KEY `index_yyyymmddhhmmss` (`year`,`month`,`date`,`hour`,`minute`,`second`)," +
					"KEY `index_awaitingPayment` (`awaitingPayment`)," +
					") ENGINE=InnoDB AUTO_INCREMENT=6853302 DEFAULT CHARSET=latin1",
					s2="KEY `index_yyyymmddhhmmss` (`year`,`month`,`date`,`hour`,`minute`,`second`)," +
							"KEY `index_awaitingPayment` (`awaitingPayment`),";
			Assert.assertEquals(s2, getKeyDeclarationFromFullBody(SqlTable.getColumnDefinitionSection(s)));
			
			// if there's no key declaration 
			 s="CREATE TABLE `Inquiry` (" +
						"`ccEmailOpened` datetime DEFAULT NULL,"+
						"`userToken` varchar(255) DEFAULT NULL,"+
						"PRIMARXtY KEY (`id`),"+
						") ENGINE=InnoDB AUTO_INCREMENT=6853302 DEFAULT CHARSET=latin1";
						s2="";
				Assert.assertEquals(s2, getKeyDeclarationFromFullBody(SqlTable.getColumnDefinitionSection(s)));
		}
		
		@Test public void testGetKeyColumnsNamesFromKeyDeclaration(){
			/*
			 * NOT YET DONE
			 */
			String s="CREATE TABLE `Inquiry` (" +
					"`ccEmailOpened` datetime DEFAULT NULL,"+
					"`userToken` varchar(255) DEFAULT NULL,"+
					"PRIMARXtY KEY (`id`),"+
					"KEY `index_yyyymmddhhmmss` (`year`,`month`,`date`,`hour`,`minute`,`second`)," +
					"KEY `index_awaitingPayment` (`awaitingPayment`)," +
					") ENGINE=InnoDB AUTO_INCREMENT=6853302 DEFAULT CHARSET=latin1",
					s2="KEY `index_yyyymmddhhmmss` (`year`,`month`,`date`,`hour`,`minute`,`second`)," +
							"KEY `index_awaitingPayment` (`awaitingPayment`),";
			List<String> sList=getKeyDeclarationsFromFullBody(s);
			
			for(String str1:sList){
				System.out.println("]["+str1);
				for(String str2:getKeyColumnsNamesFromKeyDeclaration(str1)){
					System.out.println("/*" +str2);
				}
			}
			// Assert.assertEquals(s2, getKeyDeclarationFromFullBody(SqlTable.getFullBody(s)));
		}
	}
}
