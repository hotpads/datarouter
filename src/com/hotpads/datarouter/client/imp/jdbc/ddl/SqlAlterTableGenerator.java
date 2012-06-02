package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;

public class SqlAlterTableGenerator implements DdlGenerator{

	protected SchemaUpdateOptions options;
	protected SqlTable current, requested;
	protected boolean dropTable = false;

	public SqlAlterTableGenerator(SchemaUpdateOptions options, SqlTable current, SqlTable requested){
		this.options = options;
		this.current = current;
		this.requested = requested;
	}
	
	@Override
	public String generateDdl(){
		List<SqlAlterTableClause> singleAlters =  generate();
		if(CollectionTool.isEmpty(singleAlters)){ return null; }
		StringBuilder sb = new StringBuilder(), sb2 = new StringBuilder();
		
		sb.append("alter table "+current.getName()+"\n");
		int numAppended = 0;
		for(SqlAlterTableClause singleAlter : IterableTool.nullSafe(singleAlters)){
			if(singleAlter!=null){
				if(singleAlter.getType().equals(SqlAlterTypes.DROP_TABLE) || singleAlter.getType().equals(SqlAlterTypes.CREATE_TABLE) ){
					sb2.append(singleAlter.getAlterTable());
					sb2.append("\n");
				}
				else{
					if(numAppended>0){ sb.append(",\n"); }
					sb.append(singleAlter.getAlterTable());
					++numAppended;
				}
			}
		}
		sb.append(";");
		sb2.append(sb);
		return sb2.toString();
	}
	
	public List<String> getAlterTableStatementsStrings(){
		List<SqlAlterTableClause> list =  generate();
		List<String> l = ListTool.createArrayList();
		String alterSql="";
		if(dropTable){
			for(SqlAlterTableClause sqlAT : list){
				String s="";
				alterSql = sqlAT.getAlterTable();
				if(StringTool.containsCharactersBesidesWhitespace(alterSql)){
					s += alterSql;
					l.add(s);
				}
			}
		}
		else{
			for(SqlAlterTableClause sqlAT : list){
				String s = "";
				alterSql = sqlAT.getAlterTable();
				if(StringTool.containsCharactersBesidesWhitespace(alterSql)){
					s += "ALTER TABLE `" + current.getName() + "` \n";
					s += alterSql;
					l.add(s);
				}
			}
			//s+="\n";
		}
		
		return l;
	}
	
	public List<SqlAlterTableClause> getAlterTableStatements(){
		List<SqlAlterTableClause> list =  generate();
		List<SqlAlterTableClause> l = ListTool.createArrayList();
		String alterSql="";
		if(dropTable){
			for(SqlAlterTableClause sqlAT : list){
				//sString s="";
				alterSql = sqlAT.getAlterTable();
				if(StringTool.containsCharactersBesidesWhitespace(alterSql)){
					l.add(sqlAT);
				}
			}
		}
		else{
			for(SqlAlterTableClause sqlAT : list){
				String s="";
				alterSql = sqlAT.getAlterTable();
				if(StringTool.containsCharactersBesidesWhitespace(alterSql)){
					s += "ALTER TABLE `" + current.getName() + "` \n";
					s += alterSql;
					sqlAT.setAlterTable(s);
					l.add(sqlAT);
				}
			}
			//s+="\n";
		}
		
		return l;
	}
	
	public List<SqlAlterTableClause> generate() {
		List<SqlAlterTableClause> list = ListTool.createArrayList();
		// creating the sqlTableDiffGenerator
		SqlTableDiffGenerator diff = new SqlTableDiffGenerator(current,requested,true);
		if(diff.isTableModified()){
			// get the columns to add and the columns to remove
			List<SqlColumn> colsToAdd = diff.getColumnsToAdd(),
							colsToRemove = diff.getColumnsToRemove(),
							colsToModify = diff.getColumnsToModify();
	
			// get the other modifications ( the indexes )
			List<SqlIndex> indexesToAdd = diff.getIndexesToAdd(),
							 indexesToRemove = diff.getIndexesToRemove();
			
			// generate the alter table statements from columns to add and to remove
			if(colsToRemove.size()<current.getNumberOfColumns() && (colsToAdd.size()>0 || colsToRemove.size()>0) ){ 
				list.addAll(getAlterTableForRemovingColumns(colsToRemove));
				list.add(getAlterTableForAddingColumns(colsToAdd));
			}
			else if(colsToRemove.size()>0){// cannot drop all columns, should use drop table then create it from list of columns
				dropTable = true;
				list.add(new SqlAlterTableClause("DROP TABLE " +current.getName() +";", SqlAlterTypes.DROP_TABLE));
				list.add(getCreateTableSqlFromListOfColumnsToAdd(colsToAdd));
			}
			if(!CollectionTool.isEmpty(colsToModify)){
				
				for(SqlColumn col : IterableTool.nullSafe(colsToModify)){
					SqlColumn requestedCol = getColumnByNamefromListOfColumn(col.getName(),requested.getColumns());
					String s="MODIFY " +col.getName() +" " + requestedCol.getType() ;
					if(requestedCol.getMaxLength()!=null){
						s+=  " (" +requestedCol.getMaxLength() +")";
					}
					list.add(new SqlAlterTableClause(s, SqlAlterTypes.MODIFY));
				}
			}
			//*
			if(diff.isPrimaryKeyModified()){
				if(current.hasPrimaryKey()){
					list.add(new SqlAlterTableClause("DROP PRIMARY KEY ", SqlAlterTypes.DROP_INDEX));
				}
				
				List<SqlColumn> listOfColumnsInPkey =requested.getPrimaryKey().getColumns(); 
				String s = "ADD " /*CONSTRAINT "+ requested.getPrimaryKey().getName() + */ +" PRIMARY KEY (" ;
				for(SqlColumn col: listOfColumnsInPkey){
					s+= col.getName() + ",";
				}
				s=s.substring(0, s.length()-1)+")";
				list.add(new SqlAlterTableClause(s, SqlAlterTypes.ADD_INDEX));
			}
			//*/
			if(diff.isIndexesModified() && (!CollectionTool.isEmpty(indexesToAdd) || !CollectionTool.isEmpty(indexesToRemove))){
				list.addAll(getAlterTableForRemovingIndexes(indexesToRemove));
				list.addAll(getAlterTableForAddingIndexes(indexesToAdd));
			}	
		}
		//s+=");";
		return list;
	}

	private SqlColumn getColumnByNamefromListOfColumn(String name, List<SqlColumn> columns){
		for(SqlColumn col : IterableTool.nullSafe(columns)){
			if(col.getName().equals(name)) return col;
		}
		return null;
	}

	private List<SqlAlterTableClause> getAlterTableForRemovingIndexes(List<SqlIndex> indexesToAdd) {
		List<SqlAlterTableClause> list = ListTool.createArrayList();
		if(!options.getDropIndexes()){ return list; }
		
		if(indexesToAdd.size()>0){
			String s="";
			for(SqlIndex index : indexesToAdd){
				s+= "DROP INDEX "+ index.getName() + ", ";
			}
			s=s.substring(0,s.length()-2);
		//	s+=";";
			list.add(new SqlAlterTableClause(s, SqlAlterTypes.DROP_INDEX));
		}
		return list;
	}

	private List<SqlAlterTableClause> getAlterTableForAddingIndexes(List<SqlIndex> indexesToRemove){
		List<SqlAlterTableClause> list = ListTool.createArrayList();
		if(!options.getAddIndexes()){ return list; }
		
		if(indexesToRemove.size()>0){
			String s="";
			for(SqlIndex index : indexesToRemove){
				s+="ADD KEY " + index.getName() + "( ";
				for(SqlColumn col : index.getColumns()){
					s+= col.getName() + ", ";
				}
				s =s.substring(0, s.length()-2);
				s+="), ";
			}
			s = s.substring(0, s.length()-2);
		//	s+=";";
			list.add(new SqlAlterTableClause(s, SqlAlterTypes.ADD_INDEX));
		}
		return list;
	}

	private SqlAlterTableClause getCreateTableSqlFromListOfColumnsToAdd(
			List<SqlColumn> colsToAdd) {
		//new SqlAlterTable("CREATE TABLE " +current.getName() +";", SqlAlterTableTypes.CREATE_TABLE);
		if(colsToAdd.size()>0){
			String s = "CREATE TABLE " +current.getName();
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
			return new SqlAlterTableClause(s, SqlAlterTypes.CREATE_TABLE);
		}
		return null;
	}

	private SqlAlterTableClause getAlterTableForAddingColumns(List<SqlColumn> colsToAdd) {
		if(!options.getAddColumns()){ return null; }
		
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
			s+=")";
		}
		return new SqlAlterTableClause(s, SqlAlterTypes.ADD_COLUMN);
	}
	
	private List<SqlAlterTableClause> getAlterTableForRemovingColumns(List<SqlColumn> colsToRemove) {
		List<SqlAlterTableClause> list = ListTool.createArrayList();
		if(!options.getDeleteColumns()){ return list; }
		
		if(colsToRemove.size()>0){
			String s = "";
			for(SqlColumn col:colsToRemove){
				s += "DROP COLUMN ";
					s+= col.getName() + ", ";
			}
			s = s.substring(0, s.length()-2); // remove the last "," 
			//s+=";";
			list.add(new SqlAlterTableClause(s, SqlAlterTypes.DROP_COLUMN));
		}
		return list;
	}
	
	public static class SqlAlterTableGeneratorTester{
		 
		/*public void generateTest() throws IOException{
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
			//SqlColumnNameComparator c = new SqlColumnNameComparator(true);
			
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
			//System.out.println(tab2);
			
			SqlAlterTableGenerator alterGeneratorBis21 = new SqlAlterTableGenerator(options, tab2, tab1);
			SqlAlterTableGenerator alterGeneratorBis12 = new SqlAlterTableGenerator(options, tab1, tab2);
			System.out.println(alterGeneratorBis21.generate());
			System.out.println(alterGeneratorBis12.generate());
		}
	
		@Test public void getAlterTableStatementsTester() throws IOException{
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
			//SqlColumnNameComparator nameComparator = new SqlColumnNameComparator(true);
			//SqlColumnNameTypeComparator nameTypeComparator= new SqlColumnNameTypeComparator(true);
			
			
			
			SchemaUpdateOptions options = new SchemaUpdateOptions().setAllTrue();
			SqlAlterTableGenerator alterGenerator21 = new SqlAlterTableGenerator(options, table2, table1);
			SqlAlterTableGenerator alterGenerator12 = new SqlAlterTableGenerator(options, table1, table2);
			System.out.println(alterGenerator21.getAlterTableStatements());
			System.out.println(alterGenerator12.getAlterTableStatements());
			
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
			
			//System.out.println(tab1);
			//System.out.println(tab2);
			SqlAlterTableGenerator alterGeneratorBis21 = new SqlAlterTableGenerator(options, tab2, tab1);
			SqlAlterTableGenerator alterGeneratorBis12 = new SqlAlterTableGenerator(options, tab1, tab2);
			
			System.out.println(alterGeneratorBis21.getAlterTableStatements());
			System.out.println(alterGeneratorBis12.getAlterTableStatements());
			
			
			tab1 = new SqlTable("a"); tab2 =  new SqlTable("a");
			SqlColumn col0 =  new SqlColumn("ab", MySqlColumnType.VARCHAR,200,true), col0clone = col0.clone();
			tab2.addColumn(col0);
			col0clone.setType(MySqlColumnType.BINARY);
			tab1.addColumn(col0clone);
			
			// System.out.println(tab1);
			// System.out.println(tab2);
			alterGeneratorBis21 = new SqlAlterTableGenerator(options, tab2, tab1);
			alterGeneratorBis12 = new SqlAlterTableGenerator(options, tab1, tab2);
			Assert.assertTrue(CollectionTool.isEmpty(alterGeneratorBis21.getAlterTableStatements()));
			Assert.assertTrue(CollectionTool.isEmpty(alterGeneratorBis12.getAlterTableStatements()));
			Assert.assertFalse(CollectionTool.isEmpty(alterGeneratorBis21.getAlterTableStatements()));
			Assert.assertFalse(CollectionTool.isEmpty(alterGeneratorBis12.getAlterTableStatements()));
			
	}
	
		@Test public void getAlterTableForIndexesTester(){
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
			SqlColumnNameComparator nameComparator = new SqlColumnNameComparator(true);
			SqlColumnNameTypeComparator nameTypeComparator= new SqlColumnNameTypeComparator(true);
			
			SqlIndex index = new SqlIndex("1", listBC),
						index2 = new SqlIndex("2", listM);
			table1.addIndex(index);
			table1.addIndex(index2);
			
			SchemaUpdateOptions options = new SchemaUpdateOptions().setAllTrue();
			SqlAlterTableGenerator alterGenerator21 = new SqlAlterTableGenerator(options, table2, table1);
			SqlAlterTableGenerator alterGenerator12 = new SqlAlterTableGenerator(options, table1, table2);
			
			System.out.println(alterGenerator12.getMasterAlterStatement());
			System.out.println(alterGenerator21.getMasterAlterStatement());
		}
		*/
	}
	
}
