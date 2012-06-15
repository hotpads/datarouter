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

	private static final int MINIMUM_ALTER_SIZE = 10;
	protected SchemaUpdateOptions options;
	protected SqlTable current, requested;
	protected String databaseName="";
	protected boolean dropTable = false;
	protected boolean willAlterTable = false;

	public SqlAlterTableGenerator(SchemaUpdateOptions options, SqlTable current, SqlTable requested, String databaseName){
		this.options = options;
		this.current = current;
		this.requested = requested;
		this.databaseName = databaseName;
	}
	
	@Override
	public String generateDdl(){
		List<SqlAlterTableClause> singleAlters =  generate();
		if(CollectionTool.isEmpty(singleAlters)){ return null; }
		if(dropTable){
			String s="";
			for(SqlAlterTableClause singleAlter : IterableTool.nullSafe(singleAlters)){
				String alterSql = singleAlter.getAlterTable();
				if(StringTool.containsCharactersBesidesWhitespace(alterSql)){
					s += alterSql+ "\n";
				}
			}
			return s;
		}
		else{
			StringBuilder sb = new StringBuilder();
			sb.append("alter table " +databaseName + "." +current.getName()+"\n");
			int numAppended = 0;
			for(SqlAlterTableClause singleAlter : IterableTool.nullSafe(singleAlters)){
				if(singleAlter!=null /*&& !StringTool.isEmptyOrWhitespace(singleAlter.getAlterTable())*/){
					if(numAppended>0){ sb.append(",\n"); }
					sb.append(singleAlter.getAlterTable());
					++numAppended;
				}
			}
			sb.append(";");
			if(sb.length()>= ("alter table " +databaseName + "." +current.getName()).length()+MINIMUM_ALTER_SIZE){
				willAlterTable=true;
			}
			return sb.toString();
		}
	}
	
	
	public boolean willAlterTable(){
		generateDdl();	
		return willAlterTable;
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
					s += "alter table `" + current.getName() + "` \n";
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
					s += "alter table `" + current.getName() + "` \n";
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
			if(colsToRemove.size()<current.getNumberOfColumns()){ 
				list.addAll(getAlterTableForRemovingColumns(colsToRemove));
				list.add(getAlterTableForAddingColumns(colsToAdd));
			}
			else{// cannot drop all columns, should use drop table then create it from list of columns
				dropTable = true;
				list.add(new SqlAlterTableClause("drop table "  +databaseName + "." +current.getName() +";", SqlAlterTypes.DROP_TABLE));
				list.add(getCreateTableSqlFromListOfColumnsToAdd(colsToAdd));
				return list;
			}
			if(options.getModifyColumns() && !CollectionTool.isEmpty(colsToModify)){
				
				for(SqlColumn col : IterableTool.nullSafe(colsToModify)){
					SqlColumn requestedCol = getColumnByNamefromListOfColumn(col.getName(),requested.getColumns());
					String s="modify " +col.getName() +" " + requestedCol.getType().toString().toLowerCase() ;
					if(requestedCol.getMaxLength()!=null){
						s+=  " (" +requestedCol.getMaxLength() +")";
					}
					if(requestedCol.getNullable()){
						s+=" default null";
					}else{
						s+=" not null";
					}
					list.add(new SqlAlterTableClause(s, SqlAlterTypes.MODIFY));
				}
			}
			//*
			if(options.getAddIndexes() && options.getDropIndexes() && diff.isPrimaryKeyModified()){
				if(current.hasPrimaryKey()){
					// 
					list.add(new SqlAlterTableClause("drop primary key", SqlAlterTypes.DROP_INDEX));
				}
				List<SqlColumn> listOfColumnsInPkey = requested.getPrimaryKey().getColumns(); 
				String s = "add primary key (" ;
				for(SqlColumn col: listOfColumnsInPkey){
					s+= col.getName() + ",";
				}
				s=s.substring(0, s.length()-1)+")";
				list.add(new SqlAlterTableClause(s, SqlAlterTypes.ADD_INDEX));
			}
			//*/
			if(diff.isIndexesModified()){
				list.addAll(getAlterTableForRemovingIndexes(indexesToRemove));
				list.addAll(getAlterTableForAddingIndexes(indexesToAdd));
			}	
			if(options.getModifyEngine() && diff.isEngineModified()){
				list.add(new SqlAlterTableClause("engine="+requested.getEngine().toString().toLowerCase(), SqlAlterTypes.MODIFY_ENGINE));
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
				s+= "drop index "+ index.getName() + ",\n";
			}
			s=s.substring(0,s.length()-2);
		//	s+=";";
			list.add(new SqlAlterTableClause(s, SqlAlterTypes.DROP_INDEX));
		}
		return list;
	}

	private List<SqlAlterTableClause> getAlterTableForAddingIndexes(List<SqlIndex> indexesToAdd){
		List<SqlAlterTableClause> list = ListTool.createArrayList();
		if(!options.getAddIndexes()){ return list; }
		
		if(indexesToAdd.size()>0){
			String s="";
			for(SqlIndex index : indexesToAdd){
				s+="add key " + index.getName() + "(";
				for(SqlColumn col : index.getColumns()){
					s+= col.getName() + ", ";
				}
				s =s.substring(0, s.length()-2);
				s+="),\n";
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
		String s = "create table " +current.getName();
		if(colsToAdd.size()>0){
			s+= " ( ";
			for(SqlColumn col:colsToAdd){
				s+= col.getName() + " " + col.getType().toString().toLowerCase();
				if(col.getMaxLength()!=null){
					s+="(" + col.getMaxLength() + ")";
				}
				if(col.getNullable()){
					s+=" default null";
				}
				else{
					s+=" not null";
				}
				s+=",\n";//
			}
			s = s.substring(0, s.length()-2); // remove the last "," 
			s+=");\n";
		}
		return new SqlAlterTableClause(s, SqlAlterTypes.CREATE_TABLE);
	}

	private SqlAlterTableClause getAlterTableForAddingColumns(List<SqlColumn> colsToAdd) {
		if(!options.getAddColumns()){ return null; }
		
		String s = "";
		if(colsToAdd.size()<=0) return null;
			s+= "add (";
			String type;
			for(SqlColumn col:colsToAdd){
				type = col.getType().toString().toLowerCase();
				s+= col.getName() + " " + type;
				if(col.getMaxLength()!=null && !type.equals("longblob") && !type.equals("double")){
					s+="(" + col.getMaxLength() + ")";
				}
				if(col.getNullable()){
					s+=" default null";
				}
				else{
					s+=" not null";
				}
				s+=",\n";//
			}
			s = s.substring(0, s.length()-2); // remove the last "," 
			s+=")";
		
		return new SqlAlterTableClause(s, SqlAlterTypes.ADD_COLUMN);
	}
	
	private List<SqlAlterTableClause> getAlterTableForRemovingColumns(List<SqlColumn> colsToRemove) {
		List<SqlAlterTableClause> list = ListTool.createArrayList();
		if(!options.getDeleteColumns()){ return list; }
		
		if(colsToRemove.size()>0){
			String s = "";
			for(SqlColumn col:colsToRemove){
				s += "drop column ";
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
