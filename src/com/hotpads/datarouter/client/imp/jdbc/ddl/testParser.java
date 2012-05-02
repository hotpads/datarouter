package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import java.sql.Statement;

import org.antlr.grammar.v3.ANTLRv3Parser.throwsSpec_return;
import org.apache.hadoop.hbase.regionserver.ColumnTracker;
import org.mockito.internal.stubbing.answers.ThrowsException;

import sun.dc.pr.PRError;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.util.core.ListTool;
import com.sun.org.apache.xml.internal.serializer.ToUnknownStream;

public class testParser {

	public static void main(String[] args) throws SQLException  {
		Connection conn = JdbcTool.openConnection("localhost", 3306, "drTest0", "root", "");
		Statement stmt = null;
		stmt = conn.createStatement();
		
		List<SqlColumn> columnList = ListTool.createArrayList();
		String phrase = "";
//		 phrase = "CREATE TABLE `Cheese` ("+				// THE STRING TO TOKENIZE
//				"  `id` varchar(30) NOT NULL DEFAULT '',"+
//				"  `country` char(2) DEFAULT NULL,"+
//				"  `rating` int(11) DEFAULT NULL,"+
//				" PRIMARY KEY (`id`)"+
//				") ENGINE=InnoDB DEFAULT CHARSET=latin1 ";
		
		
		
		stmt.execute("use property;");
		ResultSet resultSet3 = stmt.executeQuery("show create table Property");
		while (resultSet3.next()) {
		      // Get the data from the row using the column index
	         phrase += resultSet3.getString(2) ;
		}
		System.out.println(phrase);
		
		
		
		String delims = "[(),]+";					// THE DELIMITERS FOR TOKENIZING
		
		String[] tokens = phrase.split(delims);		// TOKENIZING
		
		System.out.println("1st Parsing :");
//		for (int i = 0; i < tokens.length; i++)			// SHOWING THE TOKENS
//		    System.out.println(tokens[i]);
		
		System.out.println("\n /****** Getting the name of the table ******/");		
		String delimForTableName = "[`]";
		//System.out.println(tokens[0].split(delimForTableName)[1]);
		
		for(int i=1; i<tokens.length; i++){
			if(tokens[i].contains("PRIMARY KEY")){
				
			}
			//if(tokens[i].contains("")){
			//}
			else{
				String[] tempTokens = tokens[i].split("[`]");
				String name=removeSpaces(tempTokens[1]), type;
				if(tempTokens[2].length()<16){
					type = removeSpaces(tempTokens[2]);
					//System.out.println(name + " & " + type);
					
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
				}
				
			}
			
		}
//		System.out.println("\n /****** Getting the  ******/");		
//		String[] columnToken = tokens[1].split("[`]");
//		System.out.println(columnToken[0] + " " + columnToken[1] + columnToken[2]);
		
		//if(tokens[0].contains("CREATE TABLE")) System.out.println("Roger That!");
		// TAKE THE STRING
		// PARSE IT USING '(',')" AND ',' AS DELIMITERS
			// THE FIRST ONE CONTAINS THE NAME OF THE TABLE
			// GETTING THE NAME OF THE TABLE 
			// FOR THE OTHER ONES USE CONTAINS TO KNOW WHAT TYPE OF QUERIE WE ARE DEALING WITH
			
		
		
	}

	private static String removeSpaces(String s) {
		String sResult="";
		for (int i = 0; i < s.length(); i++) {
			if(s.charAt(i)!=' '){
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
