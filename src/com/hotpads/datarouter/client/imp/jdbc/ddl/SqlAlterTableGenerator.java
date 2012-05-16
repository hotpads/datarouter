package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;

import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;

public class SqlAlterTableGenerator{

	protected SchemaUpdateOptions options;
	protected SqlTable current, requested;
	protected boolean dropTable = false;

	public SqlAlterTableGenerator(SchemaUpdateOptions options, SqlTable current, SqlTable requested){
		this.options = options;
		this.current = current;
		this.requested = requested;
	}
	
	public List<String> getAlterTableStatements(){
		List<SqlAlterTable> list =  generate();
		List<String> l = ListTool.createArrayList();
		String alterSql="";
		if(dropTable){
			for(SqlAlterTable sqlAT : list){
				String s="";
				alterSql = sqlAT.getAlterTable();
				if(StringTool.containsCharactersBesidesWhitespace(alterSql)){
						s+=alterSql;
						l.add(s);
				}
			}
		}
		else{
				for(SqlAlterTable sqlAT : list){
				String s="";
				alterSql = sqlAT.getAlterTable();
				if(StringTool.containsCharactersBesidesWhitespace(alterSql)){
						s+="ALTER TABLE `" + current.getName()+"` \n"; 
						s+=alterSql;
						l.add(s);
				}
			}
			//s+="\n";
		}
		
		return l;
	}
	
	public List<SqlAlterTable> generate() {
		//TODO everything
		

		List<SqlAlterTable> list = ListTool.createArrayList();
		
		// creating the sqlTableDiffGenerator
		SqlTableDiffGenerator diff = new SqlTableDiffGenerator(current,requested,true);

		// get the columns to add and the columns to remove
		List<SqlColumn> colsToAdd = diff.getColumnsToAdd(),
						colsToRemove = diff.getColumnsToRemove();

		// get the other modifications ( the indexes )
		List<SqlIndex> indexesToAdd = diff.getIndexesToAdd(),
						 indexesToRemove = diff.getIndexesToRemove();
		
		// generate the alter table statements from columns to add and to remove
		if(colsToRemove.size()<current.getNumberOfColumns()){ 
			list.addAll(getAlterTableForRemovingColumns(colsToRemove));
			list.add(getAlterTableForAddingColumns(colsToAdd));
		}
		else{// cannot drop all columns, should use drop table then create it from list of columns
			dropTable = true;
			list.add(new SqlAlterTable("DROP TABLE " +current.getName() +";", SqlAlterTypes.DROP_TABLE));
			list.add(getCreateTableSqlFromListOfColumnsToAdd(colsToAdd));
		}
		
		/*
		if(diff.isPrimaryKeyModified()){
			list.add(new SqlAlterTable("DROP PRIMARY KEY ;", SqlAlterTypes.DROP_INDEX));
			
			List<SqlColumn> listOfColumnsInPkey =requested.getPrimaryKey().getColumns(); 
			String s = "ADD CONSTRAINT "+ requested.getPrimaryKey().getName() + "PRIMARY KEY (" ;
			for(SqlColumn col: listOfColumnsInPkey){
				s+= col.getName() + ",";
			}
			s=s.substring(0, s.length()-1)+")";
			list.add(new SqlAlterTable(s, SqlAlterTypes.ADD_CONTRAINT));
		}
		//*/
		// append them all into s
		
		//s+=");";
		return list;
	}

	private SqlAlterTable getCreateTableSqlFromListOfColumnsToAdd(
			List<SqlColumn> colsToAdd) {
		// TODO Auto-generated method stub
		//new SqlAlterTable("CREATE TABLE " +current.getName() +";", SqlAlterTableTypes.CREATE_TABLE);
		String s = "CREATE TABLE " +current.getName();
		if(colsToAdd.size()>0){
			s+= " ( ";
			for(SqlColumn col:colsToAdd){
				s+= col.getName() + " " + col.getType().toString().toLowerCase();
				if(col.getMaxLength()!=null){
					s+="(" + col.getMaxLength() + ") ";
				}
				if(col.getNullable()){
					s+=" DEFAULT NULL";
				}
				else{
					s+=" NOT NULL";
				}
				s+=",\n";//
			}
			s = s.substring(0, s.length()-2); // remove the last "," 
			s+=");\n";
		}
		return new SqlAlterTable(s, SqlAlterTypes.CREATE_TABLE);
	}

	private SqlAlterTable getAlterTableForAddingColumns(List<SqlColumn> colsToAdd) {
		// TODO Auto-generated method stub
		String s = "";
		if(colsToAdd.size()>0){
			s+= "ADD ( ";
			for(SqlColumn col:colsToAdd){
				s+= col.getName() + " " + col.getType().toString().toLowerCase();
				if(col.getMaxLength()!=null){
					s+="(" + col.getMaxLength() + ") ";
				}
				if(col.getNullable()){
					s+=" DEFAULT NULL";
				}
				else{
					s+=" NOT NULL";
				}
				s+=",\n";//
			}
			s = s.substring(0, s.length()-2); // remove the last "," 
			s+=");\n";
		}
		return new SqlAlterTable(s, SqlAlterTypes.ADD_COLUMN);
	}
	
	private List<SqlAlterTable> getAlterTableForRemovingColumns(List<SqlColumn> colsToRemove) {
		// TODO Auto-generated method stub
		List<SqlAlterTable> list = ListTool.createArrayList();
		
		if(colsToRemove.size()>0){
			String s = "";
			for(SqlColumn col:colsToRemove){
				s += "DROP COLUMN ";
					s+= col.getName() + ", ";
			}
			s = s.substring(0, s.length()-2); // remove the last "," 
			s+=";";
			list.add(new SqlAlterTable(s, SqlAlterTypes.DROP_COLUMN));
		}
		return list;
	}


	public static class TestSqlAlterTableGenerator{
		@Test public void generateTest() throws IOException{
			SqlColumn 
			colA = new SqlColumn("A", MySqlColumnType.BIGINT),
			colB = new SqlColumn("B", MySqlColumnType.VARCHAR,250,false),
			colC = new SqlColumn("C", MySqlColumnType.BOOLEAN),
			colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
			List<SqlColumn> 
					listBC = ListTool.createArrayList(),
					listM = ListTool.createArrayList();
	
			listBC.add(colB);
			listBC.add(colC);
			listM.add(colM);
			SqlTable 
					table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC),
					table2 = new SqlTable("TB").addColumn(colA).addColumn(colM);
			
			SchemaUpdateOptions options = new SchemaUpdateOptions().setAllTrue();
			SqlAlterTableGenerator alterGenerator21 = new SqlAlterTableGenerator(options, table2, table1);
			SqlAlterTableGenerator alterGenerator12 = new SqlAlterTableGenerator(options, table1, table2);
			System.out.println(alterGenerator21.generate());
			System.out.println(alterGenerator12.generate());
			
			FileInputStream fis = new FileInputStream("src/com/hotpads/datarouter/client/imp/jdbc/ddl/test2.txt");
			DataInputStream in = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String str, phrase = "";
			while((str = br.readLine()) != null){
				phrase += str;
			}
			SqlCreateTableParser parser = new SqlCreateTableParser(phrase);
			SqlTable tab1 = parser.parse();
			
			 fis = new FileInputStream("src/com/hotpads/datarouter/client/imp/jdbc/ddl/test22.txt");
			 in = new DataInputStream(fis);
			 br = new BufferedReader(new InputStreamReader(in));
			 phrase = "";
			while((str = br.readLine()) != null){
				phrase += str;
			}
			 parser = new SqlCreateTableParser(phrase);
			SqlTable tab2 = parser.parse();
			System.out.println(tab2);
			
			SqlAlterTableGenerator alterGeneratorBis21 = new SqlAlterTableGenerator(options, tab2, tab1);
			SqlAlterTableGenerator alterGeneratorBis12 = new SqlAlterTableGenerator(options, tab1, tab2);
			System.out.println(alterGeneratorBis21.generate());
			System.out.println(alterGeneratorBis12.generate());
		}
	}
	
}
