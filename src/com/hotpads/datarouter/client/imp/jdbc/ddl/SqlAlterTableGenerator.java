package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;

import com.hotpads.util.core.ListTool;

public class SqlAlterTableGenerator{

	protected SqlTable current, requested;

	public SqlAlterTableGenerator(SqlTable current, SqlTable requested){
		this.current = current;
		this.requested = requested;
	}
	
	public List<SqlAlterTable> generate() {
		//TODO everything
		String s="ALTER TABLE `" + current.getName()+"` (\n"; 

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
		list.add(getAlterTableForAddingColumns(colsToAdd));
		list.add(getAlterTableForRemovingColumns(colsToRemove));
		// append them all into s
		
		//s+=");";
		return list;
	}

	private SqlAlterTable getAlterTableForAddingColumns(List<SqlColumn> colsToAdd) {
		// TODO Auto-generated method stub
		String s= "ADD ( ";
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
		s+=")\n";
		return new SqlAlterTable(s, SqlAlterTableTypes.ADD_COLUMN);
	}
	
	private SqlAlterTable getAlterTableForRemovingColumns(List<SqlColumn> colsToRemove) {
		// TODO Auto-generated method stub
		String s= "DROP COLUMN ( ";
		for(SqlColumn col:colsToRemove){
			s+= col.getName() + ", ";
		}
		s = s.substring(0, s.length()-2); // remove the last "," 
		s+=")\n";
		return new SqlAlterTable(s, SqlAlterTableTypes.DROP_COLUMN);
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
			
			SqlAlterTableGenerator alterGenerator21 = new SqlAlterTableGenerator(table2, table1);
			SqlAlterTableGenerator alterGenerator12 = new SqlAlterTableGenerator(table1, table2);
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
			
			SqlAlterTableGenerator alterGeneratorBis21 = new SqlAlterTableGenerator(tab2, tab1);
			SqlAlterTableGenerator alterGeneratorBis12 = new SqlAlterTableGenerator(tab1, tab2);
			System.out.println(alterGeneratorBis21.generate());
			System.out.println(alterGeneratorBis12.generate());
		}
	}
	
}
