package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;

import com.hotpads.util.core.ListTool;

public class testParser {

	public static void main(String[] args) throws SQLException, IOException  {
		SqlTable table = new SqlTable("Table");
		List<SqlColumn> columns = ListTool.createArrayList();
		
		
		testGetHeader();
		 testGetTail();
		 testGetFullBody();
		FileInputStream fis = new FileInputStream("/home/moubenal/workspace/datarouter/src/com/hotpads/datarouter/client/imp/jdbc/ddl/test2.txt");
		// Get the object of DataInputStream
		  DataInputStream in = new DataInputStream(fis);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  String str, phrase="";
		while((str=br.readLine()) != null) {
			phrase+=str;
		}
		
		
//		System.out.println(" get body : " +getBody(phrase));
//		System.out.println("primary key " +getPrimaryKeyDeclarationFromFullBody(getFullBody(phrase)));
//		for(String s:getColumns(getBody(phrase))){
//			if(isNotEmpty(s)){
//						System.out.println("name : " +getNameOfColumn(s));
//					
//					System.out.println("type : " +getTypeOfColumn(s));
//					if(hasAMaxValue(s)){
//						System.out.println("max value : " +getMaxValueOfColumn(s));
//					}
//					System.out.println("nullable : " +getNullable(s));
//			}
//		}
		
		for(String s:getColumns(getBody(phrase))){
			if(isNotEmpty(s)){
				SqlColumn col = createAppropriateColumnType(getNameOfColumn(s), getTypeOfColumn(s));
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
		
		
		
		 //SqlTable table = parseAndCreateTable(phrase);
		
		//if(tokens[0].contains("CREATE TABLE")) System.out.println("Roger That!");
		// TAKE THE STRING
		// PARSE IT USING '(',')" AND ',' AS DELIMITERS
			// THE FIRST ONE CONTAINS THE NAME OF THE TABLE
			// GETTING THE NAME OF THE TABLE 
			// FOR THE OTHER ONES USE CONTAINS TO KNOW WHAT TYPE OF QUERIE WE ARE DEALING WITH
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
					
					SqlColumn col = createAppropriateColumnType(name, type);
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
					SqlColumn col = createAppropriateColumnType(name, type);
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
	private static boolean getNullable(String s) {
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
	private static String getMaxValueOfColumn(String s) {
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
	private static String getTypeOfColumn(String s) {
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
	private static String getNameOfColumn(String s) {
		// TODO Auto-generated method stub
		 int index = s.indexOf('`');
		 String[] tokens = s.substring(index+1).split("[`]+");
		return tokens[0];
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
	 
	 public static void testGetHeader(){ 
		 System.out.println(getHeader("Header(blabla(blob()))trail"));
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
	 public static void testGetTail(){ 
		 System.out.println(getTail("Header(blabla(blob()))trail"));
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
	 public static void testGetFullBody(){
		 System.out.println(getFullBody("Header(blabla(blob()))trail"));
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
	 /**
	  * 
	  * @param phrase is the body of an Sql  querie of the type " show create table nameOfTheTable " 
	  * @return list of Strings containing the column declarations, primary key declaration and key/indexes declaration 
	  */
	 private static String[] getColumns(String phrase){
		return phrase.split("[,]+"); 
	 }
	 private static void testGetColumns(){
		 System.out.println(getFullBody("Header(blabla(blob()))trail"));
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
	private static String removeNonText(String s) {
		String sResult="";
		for (int i = 0; i < s.length(); i++) {
			if(s.charAt(i)!=' ' && s.charAt(i)!='`'){
				sResult+=s.charAt(i);
			}
		}
		return sResult;
	}

	private static SqlColumn createAppropriateColumnType(String name,
			String type) {
		switch (MysqlColumnType.valueOf(type.toUpperCase())) {
		case BIT:
			return new SqlColumn(name, MysqlColumnType.BIT);
		case TINYINT:
			return new SqlColumn(name, MysqlColumnType.TINYINT);
		case BOOL:
			return new SqlColumn(name, MysqlColumnType.BOOL);
		case BOOLEAN:
			return new SqlColumn(name, MysqlColumnType.BOOLEAN);
		case SMALLINT:
			return new SqlColumn(name, MysqlColumnType.SMALLINT);
		case MEDIUMINT:
			return new SqlColumn(name, MysqlColumnType.MEDIUMINT);
		case INT:
			return new SqlColumn(name, MysqlColumnType.INT);
		case INTEGER:
			return new SqlColumn(name, MysqlColumnType.INTEGER);
		case BIGINT:
			return new SqlColumn(name, MysqlColumnType.BIGINT);
		case DECIMAL:
			return new SqlColumn(name, MysqlColumnType.DECIMAL);
		case DEC:
			return new SqlColumn(name, MysqlColumnType.DEC);
		case FLOAT:
			return new SqlColumn(name, MysqlColumnType.FLOAT);
		case DOUBLE:
			return new SqlColumn(name, MysqlColumnType.DOUBLE);
		case DOUBLE_PRECISION:
			return new SqlColumn(name, MysqlColumnType.DOUBLE_PRECISION);
		case DATE:
			return new SqlColumn(name, MysqlColumnType.DATE);
		case DATETIME:
			return new SqlColumn(name, MysqlColumnType.DATETIME);
		case TIMESTAMP:
			return new SqlColumn(name, MysqlColumnType.TIMESTAMP);
		case TIME:
			return new SqlColumn(name, MysqlColumnType.TIME);
		case YEAR:
			return new SqlColumn(name, MysqlColumnType.YEAR);
		case CHAR:
			return new SqlColumn(name, MysqlColumnType.CHAR);
		case VARCHAR:
			return new SqlColumn(name, MysqlColumnType.VARCHAR);
		case BINARY:
			return new SqlColumn(name, MysqlColumnType.BINARY);
		case VARBINARY:
			return new SqlColumn(name, MysqlColumnType.VARBINARY);
		case TINYBLOB:
			return new SqlColumn(name, MysqlColumnType.TINYBLOB);
		case TINYTEXT:
			return new SqlColumn(name, MysqlColumnType.TINYTEXT);
		case BLOB:
			return new SqlColumn(name, MysqlColumnType.BLOB);
		case MEDIUMBLOB:
			return new SqlColumn(name, MysqlColumnType.MEDIUMBLOB);
		case MEDIUMTEXT:
			return new SqlColumn(name, MysqlColumnType.MEDIUMTEXT);
		case LONGBLOB:
			return new SqlColumn(name, MysqlColumnType.LONGBLOB);
		case LONGTEXT:
			return new SqlColumn(name, MysqlColumnType.LONGTEXT);
		case ENUM:
			return new SqlColumn(name, MysqlColumnType.ENUM);
		case SET:
			return new SqlColumn(name, MysqlColumnType.SET);
		case TEXT:
			return new SqlColumn(name, MysqlColumnType.TEXT);
		default:
			return  new SqlColumn(name, MysqlColumnType.VARCHAR); //// <============== Is varchar the default type ?
		}
	}
}
