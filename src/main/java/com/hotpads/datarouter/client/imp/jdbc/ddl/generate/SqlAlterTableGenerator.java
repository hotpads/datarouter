package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

import java.util.List;
import java.util.SortedSet;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrStringTool;

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
		if(DrCollectionTool.isEmpty(singleAlters)){ return null; }
		if(dropTable){
			String s="";
			for(SqlAlterTableClause singleAlter : DrIterableTool.nullSafe(singleAlters)){
				String alterSql = singleAlter.getAlterTable();
				if(DrStringTool.containsCharactersBesidesWhitespace(alterSql)){
					s += alterSql+ "\n";
				}
			}
			return s;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("alter table " +databaseName + "." +current.getName()+"\n");
		int numAppended = 0;
		for(SqlAlterTableClause singleAlter : DrIterableTool.nullSafe(singleAlters)){
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

	public boolean willAlterTable(){
		generateDdl();	
		return willAlterTable;
	}
	
	public List<String> getAlterTableStatementsStrings(){
		List<SqlAlterTableClause> list =  generate();
		List<String> l = DrListTool.createArrayList();
		String alterSql="";
		if(dropTable){
			for(SqlAlterTableClause sqlAT : list){
				String s="";
				alterSql = sqlAT.getAlterTable();
				if(DrStringTool.containsCharactersBesidesWhitespace(alterSql)){
					s += alterSql;
					l.add(s);
				}
			}
		}else{
			for(SqlAlterTableClause sqlAT : list){
				String s = "";
				alterSql = sqlAT.getAlterTable();
				if(DrStringTool.containsCharactersBesidesWhitespace(alterSql)){
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
		List<SqlAlterTableClause> l = DrListTool.createArrayList();
		String alterSql="";
		if(dropTable){
			for(SqlAlterTableClause sqlAT : list){
				//sString s="";
				alterSql = sqlAT.getAlterTable();
				if(DrStringTool.containsCharactersBesidesWhitespace(alterSql)){
					l.add(sqlAT);
				}
			}
		}else{
			for(SqlAlterTableClause sqlAT : list){
				StringBuilder sb= new StringBuilder();
				alterSql = sqlAT.getAlterTable();
				if(DrStringTool.containsCharactersBesidesWhitespace(alterSql)){
					sb.append("alter table `" + current.getName() + "` \n");
					sb.append(alterSql);
					sqlAT.setAlterTable(sb.toString());
					l.add(sqlAT);
				}
			}
			//s+="\n";
		}
		
		return l;
	}
	
	public List<SqlAlterTableClause> generate(){
		List<SqlAlterTableClause> list = DrListTool.createArrayList();
		// creating the sqlTableDiffGenerator
		SqlTableDiffGenerator diff = new SqlTableDiffGenerator(current, requested, true);
		if(!diff.isTableModified()){ return list; }
		// get the columns to add and the columns to remove
		List<SqlColumn> colsToAdd = diff.getColumnsToAdd();
		List<SqlColumn> colsToRemove = diff.getColumnsToRemove();
		List<SqlColumn> colsToModify = diff.getColumnsToModify();

		// get the other modifications ( the indexes )
		SortedSet<SqlIndex> indexesToAdd = diff.getIndexesToAdd();
		SortedSet<SqlIndex> indexesToRemove = diff.getIndexesToRemove();
		
		// generate the alter table statements from columns to add and to remove
		if(colsToRemove.size()<current.getNumberOfColumns()){ 
			list.addAll(getAlterTableForRemovingColumns(colsToRemove));
			list.add(getAlterTableForAddingColumns(colsToAdd));
		}else{// cannot drop all columns, should use drop table then create it from list of columns
			dropTable = true;
			list.add(new SqlAlterTableClause("drop table "  +databaseName + "." +current.getName() +";", 
					SqlAlterTypes.DROP_TABLE));
			list.add(getCreateTableSqlFromListOfColumnsToAdd(colsToAdd));
			return list;
		}
		if(options.getModifyColumns() && DrCollectionTool.notEmpty(colsToModify)){
			String type_string;
			MySqlColumnType type ;
			for(SqlColumn col : DrIterableTool.nullSafe(colsToModify)){
				SqlColumn requestedCol = getColumnByNamefromListOfColumn(col.getName(), requested.getColumns());
				type = requestedCol.getType();
				type_string =  type.toString().toLowerCase();
				StringBuilder sb = new StringBuilder("modify " +col.getName() +" " + type_string );
				if(requestedCol.getMaxLength()!=null && type.isSpecifyLength()){
					sb.append("(" +requestedCol.getMaxLength() +")");
				}
				if(requestedCol.getNullable()){
					sb.append(" default null");
				}else{
					sb.append(" not null");
				}
				if(requestedCol.getAutoIncrement()) {
					sb.append(" auto_increment");
				}
				list.add(new SqlAlterTableClause(sb.toString(), SqlAlterTypes.MODIFY));
			}
		}
		if(options.getAddIndexes() && options.getDropIndexes() && diff.isPrimaryKeyModified()){
			if(current.hasPrimaryKey()){
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
		if(diff.isIndexesModified()){
			list.addAll(getAlterTableForRemovingIndexes(indexesToRemove));
			list.addAll(getAlterTableForAddingIndexes(indexesToAdd));
		}	
		if(options.getModifyEngine() && diff.isEngineModified()){
			list.add(new SqlAlterTableClause("engine="+requested.getEngine().toString().toLowerCase(), 
					SqlAlterTypes.MODIFY_ENGINE));
		}
		if(options.getModifyCharacterSet() && diff.isCharacterSetModified()){
			list.add(new SqlAlterTableClause("convert to character set "+requested.getCharacterSet().toString().toLowerCase(), 
					SqlAlterTypes.MODIFY_CHARACTER_SET));			
		}
		if(options.getModifyCollation() && diff.isCollationModified()){			
			list.add(new SqlAlterTableClause("collate "+requested.getCollation().toString().toLowerCase(), 
					SqlAlterTypes.MODIFY_COLLATION));
		}

		//s+=");";
		return list;
	}

	private SqlColumn getColumnByNamefromListOfColumn(String name, List<SqlColumn> columns){
		for(SqlColumn col : DrIterableTool.nullSafe(columns)){
			if(col.getName().equals(name)) return col;
		}
		return null;
	}

	private List<SqlAlterTableClause> getAlterTableForRemovingIndexes(SortedSet<SqlIndex> indexesToAdd){
		List<SqlAlterTableClause> list = DrListTool.createArrayList();
		if(!options.getDropIndexes()){ return list; }
		if(DrCollectionTool.isEmpty(indexesToAdd)){ return list; }
		StringBuilder sb = new StringBuilder();
		for(SqlIndex index : indexesToAdd){
			sb.append("drop index "+ index.getName() + ",\n");
		}
		sb = new StringBuilder(sb.substring(0, sb.length()-2)); 
		list.add(new SqlAlterTableClause(sb.toString(), SqlAlterTypes.DROP_INDEX));
		return list;
	}

	private List<SqlAlterTableClause> getAlterTableForAddingIndexes(SortedSet<SqlIndex> indexesToAdd){
		List<SqlAlterTableClause> list = DrListTool.createArrayList();
		if(!options.getAddIndexes()){ return list; }
		if(DrCollectionTool.isEmpty(indexesToAdd)){ return list; }
		StringBuilder sb = new StringBuilder();
		for(SqlIndex index : indexesToAdd){
			sb.append("add index " + index.getName() + "(");
			for(SqlColumn col : index.getColumns()){
				sb.append( col.getName() + ", ");
			}
			sb = new StringBuilder(sb.substring(0, sb.length()-2)); 
			sb.append("),\n");
		}
		sb = new StringBuilder(sb.substring(0, sb.length()-2)); 
		list.add(new SqlAlterTableClause(sb.toString(), SqlAlterTypes.ADD_INDEX));
		return list;
	}

	private SqlAlterTableClause getCreateTableSqlFromListOfColumnsToAdd(List<SqlColumn> colsToAdd){
		//new SqlAlterTable("CREATE TABLE " +current.getName() +";", SqlAlterTableTypes.CREATE_TABLE);
		StringBuilder sb = new StringBuilder("create table " + current.getName());
		if(DrCollectionTool.isEmpty(colsToAdd)){
			return new SqlAlterTableClause(sb.toString(), SqlAlterTypes.CREATE_TABLE);
		}
		sb.append(" ( ");
		for(SqlColumn col:colsToAdd){
			sb.append(col.getName() + " " + col.getType().toString().toLowerCase());
			if(col.getMaxLength()!=null){
				sb.append("(" + col.getMaxLength() + ")");
			}
			if(col.getNullable()){
				sb.append(" default null");
			}else{
				sb.append(" not null");
			}
			if(col.getAutoIncrement()) {
				sb.append(" auto_increment");
			}
			sb.append(",\n");//
		}
		sb = new StringBuilder(sb.substring(0, sb.length()-2)); // remove the last "," 
		sb.append(");\n");
		return new SqlAlterTableClause(sb.toString(), SqlAlterTypes.CREATE_TABLE);
	}

	private SqlAlterTableClause getAlterTableForAddingColumns(List<SqlColumn> colsToAdd){
		if(!options.getAddColumns()){ return null; }
		if(DrCollectionTool.isEmpty(colsToAdd)){ return null; }
		
		StringBuilder sb = new StringBuilder();
		sb.append("add (");
		String type_string;
		for(SqlColumn col : colsToAdd){
			MySqlColumnType type = col.getType();
			type_string = col.getType().toString().toLowerCase();
			sb.append(col.getName() + " " + type_string);
			if(col.getMaxLength()!=null && type.isSpecifyLength()){
				sb.append("(" + col.getMaxLength() + ")");
			}
			if(col.getNullable()){
				sb.append(" default null");
			}else{
				sb.append(" not null");
			}
			if(col.getAutoIncrement()) {
				sb.append(" auto_increment");
			}
			sb.append(",\n");
		}
		sb = new StringBuilder(sb.substring(0, sb.length()-2)); // remove the last ","   
		sb.append(")");			// sb.deleteCharAt(sb.length()-2)
		return new SqlAlterTableClause(sb.toString(), SqlAlterTypes.ADD_COLUMN);
	}
	
	private List<SqlAlterTableClause> getAlterTableForRemovingColumns(List<SqlColumn> colsToRemove){
		List<SqlAlterTableClause> list = DrListTool.createArrayList();
		if(!options.getDeleteColumns()){ return list; }
		if(DrCollectionTool.isEmpty(colsToRemove)){ return list; }
		StringBuilder sb = new StringBuilder();
		for(SqlColumn col:colsToRemove){
			sb.append("drop column "+ col.getName() + ",\n");
		}
		sb = new StringBuilder(sb.substring(0, sb.length()-2)); // remove the last "," 
		list.add(new SqlAlterTableClause(sb.toString(), SqlAlterTypes.DROP_COLUMN));
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
