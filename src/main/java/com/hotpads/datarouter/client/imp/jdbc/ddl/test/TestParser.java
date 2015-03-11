package com.hotpads.datarouter.client.imp.jdbc.ddl.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.util.core.DrListTool;
@Deprecated
public class TestParser{

	public static void main(String[] args) throws SQLException, IOException{
		SqlTable table = new SqlTable("Table");
		List<SqlColumn> columns = DrListTool.createArrayList();

		FileInputStream fis = new FileInputStream("src/com/hotpads/datarouter/client/imp/jdbc/ddl/test3.txt");
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String str, phrase = "";
		while((str = br.readLine()) != null){
			phrase += str;
		}
		br.close();

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
		for (int i = 0; i < sTokenPKey.length; i++){
			table.setPrimaryKey(removeNonText(sTokenPKey[i]));
		}
		// FOR THE OTHER KEY DECLARATION 
		List<String> sTokenKey= getKeyDeclarationsFromFullBody(phrase);
		for (String s1: sTokenKey){
				SqlIndex tableIndex = new SqlIndex(getKeyNameFromKeydeclaration(s1));
				System.out.println(getKeyNameFromKeydeclaration(s1));
				for(String s2:getKeyColumnsNamesFromKeyDeclaration(s1)){
					addAppropriateColumnToIndexFromListOfColumn(tableIndex, s2, table.getColumns());
				}
				table.addIndex(tableIndex);
		}
		
		System.out.println(table);
		
	}

	public static void addAppropriateColumnToIndexFromListOfColumn(
			SqlIndex tableIndex, String s1, List<SqlColumn> columns){
		for(SqlColumn col: columns){ 
			if(col.getName().equals(s1)) tableIndex.addColumn(col);
		}
	}

	public static List<String> getKeyColumnsNamesFromKeyDeclaration(String string){
		int index = string.indexOf("(");
		String[] sFinal = string.substring(index).split("[`]+");
		List<String> list = DrListTool.createArrayList();
		for(String s: sFinal){
			s = removeNonText(s);
			if(isNotEmpty(s)) list.add(s); 
		}
		return list; 
	}

	public static String getKeyNameFromKeydeclaration(String string){
		//		String[] sToken = string.split("[`]+");
		//		System.out.println("*** les tokens ***");
		//		for(String s : sToken){
		//			System.out.println(s);
		//		}
		//		//return sToken[1];
		int index1 = string.indexOf("KEY `"); 
		index1 += "KEY `".length();
		String temp = string.substring(index1);
		//System.out.println(temp);
		int index2 = temp.indexOf("`");
		//System.out.println(" les indexes : "+index1 + " " + index2 );
		String s= temp.substring(0, index2);
		//System.out.println(s);
		return s;
	}

	/**
	 * 
	 * @param s
	 * @return true if it contains at least one character different than a space
	 */
	private static boolean isNotEmpty(String s){
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
		List<SqlColumn> columns = DrListTool.createArrayList();
		String delims = "[(),]+";					// THE DELIMITERS FOR TOKENIZING
		String[] tokens = phrase.split(delims);		// TOKENIZING
		
		System.out.println("1st Parsing :");
		System.out.println("\n /****** Getting the name of the table ******/");		
		SqlTable table = new SqlTable("generated Sql Table");
		
		for(int i=1; i<tokens.length; i++){
			if(tokens[i].contains("PRIMARY KEY")){
				System.out.println();
				table.setPrimaryKey(removeNonText(tokens[++i]));
			}else 
				if(tokens[i].contains("KEY")){ // INDEXES 	
			}else{
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
					boolean nullable = true;
					if(tokens[++i].contains("NOT NULL")){
						nullable = false;
					}
					col.setNullable(nullable);
					System.out.println(col);
					columns.add(col);
				}else{  // THIS COLUMN HAS NO MAX VALUE
					String[] tempTokensBis = tempTokens[2].split("[ ]");
					type = removeSpaces(tempTokensBis[1]);
					SqlColumn col = new SqlColumn(name, MySqlColumnType.parse(type));
					// "NULLABLE OR NOT NULLABLE , ..."
					boolean nullable=true;
					for (int j = 0; j < tempTokensBis.length; j++){
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
	 @SuppressWarnings("unused") 
	 private static boolean isKeyDeclaration(String s){
		 return s.toUpperCase().startsWith("KEY") || s.toUpperCase().startsWith("KEY", 1)  || s.toUpperCase().startsWith("KEY", 2) ;
	}

	 /**
	  * 
	  * @param s example:   			   PRIMARY KEY (`id`),
	  * @return true if it's a primary key declaration
	  */
	@SuppressWarnings("unused") 
	private static boolean isPrimaryKeyDeclaration(String s){
		return s.toUpperCase().startsWith("PRIMARY") || s.toUpperCase().startsWith("PRIMARY", 1)  || s.toUpperCase().startsWith("PRIMARY", 2) ;
	}

	/**
	 * 
	 * @param s
	 * @return true if the column type can be null
	 */
	public static boolean getNullable(String s){
		return !s.contains("NOT");
	}

	/**
	 * 
	 * @param s
	 * @return true if the column type has a maximum value
	 */
	private static boolean hasAMaxValue(String s){
		return s.contains("(");
	}

	/**
	 * 
	 * @param s
	 * @return the maximum value for the column type
	 */
	public static String getMaxValueOfColumn(String s){
				 int index = s.lastIndexOf('`');
				String[] tokens = s.substring(index+1).split("[ ()]+");
				return tokens[2];
	}

	/**
	 *  
	 * @param s
	 * @return the type name of the column 
	 */
	public static String getTypeOfColumn(String s){
		 int index = s.lastIndexOf('`');
		String[] tokens = s.substring(index+1).split("[ ()]+");
		return tokens[1];
	}


	 /**
	  * 
	  * @param s a column
	  * @return the name of the column
	  */
	public static String getNameOfColumn(String s){
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
			return "";
	 }
	 
	 public static List<String> getKeyDeclarationsFromFullBody(String phrase){
		 String columnDefinitionSection = SqlTable.getColumnDefinitionSection(phrase);
		 //System.out.println("getColumnDefinitionSection : " + columnDefinitionSection);
		 String [] tokens = getKeyDeclarationFromFullBody(columnDefinitionSection).split("[)]");
		 List<String> keyDeclarationList = DrListTool.createArrayList();
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
	 public static String[] getColumns(String phrase){
		return phrase.split("[,]+"); 
	 }

	
	 /**
	  * 
	  * @param s
	  * @return s without occurrences of space : " "
	  */
	private static String removeSpaces(String s){
		String sResult="";
		for (int i = 0; i < s.length(); i++){
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
	public static String removeNonText(String s){
		String sResult="";
		for (int i = 0; i < s.length(); i++){
			if(s.charAt(i)!=' ' && s.charAt(i)!='`' && s.charAt(i)!=',' && s.charAt(i)!='(' && s.charAt(i)!=')'){
				sResult+=s.charAt(i);
			}
		}
		return sResult;
	}

	public static class ParserTester{
		
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
					") ENGINE=InnoDB AUTO_INCREMENT=6853302 DEFAULT CHARSET=latin1";
					//s2="KEY `index_yyyymmddhhmmss` (`year`,`month`,`date`,`hour`,`minute`,`second`)," +
						//	"KEY `index_awaitingPayment` (`awaitingPayment`),";
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