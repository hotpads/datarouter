package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

import java.util.List;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlCreateTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlTableDiffGenerator;
import com.hotpads.datarouter.util.core.ListTool;
import com.hotpads.datarouter.util.core.SetTool;

public class SqlTable{
	
	private static final MySqlCharacterSet DEFAULT_CHARACTER_SET = MySqlCharacterSet.utf8;
	private static final MySqlCollation DEFAULT_COLLATION = MySqlCollation.utf8_bin;
	
	
	/***************** fields *****************************/
	
	private String name;
	private List<SqlColumn> columns;
	private SqlIndex primaryKey;
	private SortedSet<SqlIndex> indexes;
	private MySqlTableEngine engine = MySqlTableEngine.INNODB;
	private MySqlCollation collation = DEFAULT_COLLATION;
	private MySqlCharacterSet charSet = DEFAULT_CHARACTER_SET;
	
	
	/*************** constructors ****************************/
	
	public SqlTable(String name, List<SqlColumn> columns, SqlIndex primaryKey, SortedSet<SqlIndex> indexes){
		this.name = name;
		this.columns = columns;
		this.primaryKey = primaryKey;
		this.indexes = indexes;		
	}

	public SqlTable(String name, List<SqlColumn> columns, SqlIndex primaryKey){
		this.name = name;
		this.columns = columns;
		this.primaryKey = primaryKey;
		this.indexes = SetTool.createTreeSet();
	}

	public SqlTable(String name, List<SqlColumn> columns){
		this.name = name;
		this.columns = columns;
		this.primaryKey = new SqlIndex("PRIMARY");
		this.indexes = SetTool.createTreeSet();
	}

	public SqlTable(String name){
		this.name = name;
		this.columns = ListTool.createArrayList();
		this.primaryKey = new SqlIndex("PRIMARY");
		this.indexes = SetTool.createTreeSet();
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
				}else{
					primaryKey.addColumn(col);
				}
				//System.out.println("The primary key is " + col);
			}
		}
	}
	
	public SqlTable addColumn(SqlColumn column){
		columns.add(column);
		return this;
	}

	public SqlTable addIndex(SqlIndex tableIndex){
		indexes.add(tableIndex);
		return this;
	}
	
	public boolean hasPrimaryKey(){
		return getPrimaryKey()!=null && getPrimaryKey().getColumns().size()>0;
	}

	public boolean containsColumn(String columnName){
		for(SqlColumn col : getColumns()){
			if(col.getName().equals(columnName)) return true;
		}
		return false;
	}
	
	public boolean containsIndex(String string){
		for(SqlIndex index : getIndexes()){
			if(index.getName().equals(string)) return true;
		}
		return false;
	}
	
	public static MySqlCharacterSet getDefaultCharacterSet(){
		return DEFAULT_CHARACTER_SET;
	}

	public static MySqlCollation getDefaultCollation(){
		return DEFAULT_COLLATION;
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
	private static boolean hasAMaxValue(String columnDefinitioin){
		return columnDefinitioin.contains("(");
	}
	
	
	/********************** Object methods ****************************/

	@Override
	public String toString(){
		//TODO use StringBuilder
		StringBuilder sb = new StringBuilder("SqlTable name=" + name + ",\n") ;
//		for(SqlColumn col : getColumns()){
//			sb.append(col + "\n");
//		}
//		sb.append("PK=" + primaryKey + "\nindexes=" + indexes );
//		sb.append("\nEngine : " +getEngine());
		
		//sb.append("The create table statement :\n");
		sb.append(new SqlCreateTableGenerator(this).generateDdl());
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object otherObject){
		if(!(otherObject instanceof SqlTable)){ return false; }
		SqlTable other = (SqlTable)otherObject;
		return ! new SqlTableDiffGenerator(this, other,true).isTableModified();
	}
	
	
	/*************************** get/set ********************************/

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public List<SqlColumn> getColumns(){
		return columns;
	}

	public void setColumns(List<SqlColumn> columns){
		this.columns = columns;
	}

	public SqlIndex getPrimaryKey(){
		return primaryKey;
	}

	public SortedSet<SqlIndex> getIndexes(){
		return indexes;
	}

	public void setIndexes(SortedSet<SqlIndex> indexes){
		this.indexes = indexes;
	}

	public SqlTable setPrimaryKey(SqlIndex primaryKey){
		this.primaryKey = primaryKey;
		return this;
	}

	public int getNumberOfColumns(){
		return getColumns().size();
	}

	public MySqlTableEngine getEngine(){
		return engine;
	}

	public void setEngine(MySqlTableEngine engine){
		this.engine = engine;
	}

	public MySqlCollation getCollation(){
		return collation;
	}

	public void setCollation(MySqlCollation collation){
		this.collation = collation;
	}

	public MySqlCharacterSet getCharacterSet(){
		return charSet;
	}

	public void setCharSet(MySqlCharacterSet charSet){
		this.charSet = charSet;
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
		
//		@Test
//		public void testParseCreateTable() throws IOException{
		//need to get filepath correct somehow
//			FileInputStream fis = new FileInputStream("src/com/hotpads/datarouter/client/imp/jdbc/ddl/test3.txt");
//			DataInputStream in = new DataInputStream(fis);
//			BufferedReader br = new BufferedReader(new InputStreamReader(in));
//			String str, phrase = "";
//			while((str = br.readLine()) != null){
//				phrase += str;
//			}
//			System.out.println(parseCreateTable(phrase));
//		}
	}
}
