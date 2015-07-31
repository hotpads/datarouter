package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class SqlAlterTableGenerator implements DdlGenerator{

	private static final int MINIMUM_ALTER_SIZE = 10;
	private static final String NOT_NULL = " not null";
	protected SchemaUpdateOptions options;
	protected SqlTable current, requested;
	protected String databaseName="";
	protected boolean dropTable = false;
	protected boolean willAlterTable = false;
	private final Set<SqlColumn> columnsToInitialize = new HashSet<>();

	public SqlAlterTableGenerator(SchemaUpdateOptions options, SqlTable current, SqlTable requested,
			String databaseName){
		this.options = options;
		this.current = current;
		this.requested = requested;
		this.databaseName = databaseName;
	}

	@Override
	public String generateDdl(){
		List<SqlAlterTableClause> singleAlters =  generate();
		if(DrCollectionTool.isEmpty(singleAlters)){
			return null;
		}
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
				if(numAppended>0){
					sb.append(",\n");
				}
				sb.append(singleAlter.getAlterTable());
				++numAppended;
			}
		}
		sb.append(";");
		if(sb.length()>= ("alter table " +databaseName + "." +current.getName()).length()+MINIMUM_ALTER_SIZE){
			willAlterTable=true;
		}

		if(columnsToInitialize.size()>0){
			sb.append("\n\n");
			sb.append("Update "+databaseName + "." +current.getName()+ " set ");
			sb.append("\n");
			for(SqlColumn col:columnsToInitialize){
				sb.append(col.getName()+" = "+col.getDefaultValue());
				sb.append(",\n");
			}
			sb = new StringBuilder(sb.substring(0, sb.length()-2)); // remove the last ","
			sb.append(";");
		}
		return sb.toString();
	}

	public boolean willAlterTable(){
		generateDdl();
		return willAlterTable;
	}

	public List<String> getAlterTableStatementsStrings(){
		List<SqlAlterTableClause> list =  generate();
		List<String> alterStmt = new ArrayList<>();
		String alterSql="";
		if(dropTable){
			for(SqlAlterTableClause sqlAT : list){
				String s="";
				alterSql = sqlAT.getAlterTable();
				if(DrStringTool.containsCharactersBesidesWhitespace(alterSql)){
					s += alterSql;
					alterStmt.add(s);
				}
			}
		}else{
			for(SqlAlterTableClause sqlAT : list){
				String s = "";
				alterSql = sqlAT.getAlterTable();
				if(DrStringTool.containsCharactersBesidesWhitespace(alterSql)){
					s += "alter table `" + current.getName() + "` \n";
					s += alterSql;
					alterStmt.add(s);
				}
			}
			//s+="\n";
		}

		return alterStmt;
	}

	public List<SqlAlterTableClause> getAlterTableStatements(){
		List<SqlAlterTableClause> list =  generate();
		List<SqlAlterTableClause> l = new ArrayList<>();
		String alterSql="";
		if(dropTable){
			for(SqlAlterTableClause sqlAT : list){
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
		List<SqlAlterTableClause> list = new ArrayList<>();
		// creating the sqlTableDiffGenerator
		SqlTableDiffGenerator diff = new SqlTableDiffGenerator(current, requested, true);
		if(!diff.isTableModified()){
			return list;
		}
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
			getColumnsToInitialize(colsToAdd);

		}else{// cannot drop all columns, should use drop table then create it from list of columns
			dropTable = true;
			list.add(new SqlAlterTableClause("drop table "  +databaseName + "." +current.getName() +";",
					SqlAlterTypes.DROP_TABLE));
			list.add(getCreateTableSqlFromListOfColumnsToAdd(colsToAdd));
			return list;
		}
		if(options.getModifyColumns() && DrCollectionTool.notEmpty(colsToModify)){
			String typeString;
			MySqlColumnType type ;
			for(SqlColumn col : DrIterableTool.nullSafe(colsToModify)){
				SqlColumn requestedCol = getColumnByNamefromListOfColumn(col.getName(), requested.getColumns());
				type = requestedCol.getType();
				typeString =  type.toString().toLowerCase();
				StringBuilder sb = new StringBuilder("modify " +col.getName() +" " + typeString );
				if(type.shouldSpecifyLength(requestedCol.getMaxLength())){
					sb.append("(" +requestedCol.getMaxLength() +")");
				}
				//	getDefaultValueStatement(col)
				sb.append(requestedCol.getDefaultValueStatement());
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
			list.add(new SqlAlterTableClause("convert to character set "+
					requested.getCharacterSet().toString().toLowerCase(),
					SqlAlterTypes.MODIFY_CHARACTER_SET));
		}
		if(options.getModifyCollation() && diff.isCollationModified()){
			list.add(new SqlAlterTableClause("collate "+requested.getCollation().toString().toLowerCase(),
					SqlAlterTypes.MODIFY_COLLATION));
		}

		return list;
	}

	private SqlColumn getColumnByNamefromListOfColumn(String name, List<SqlColumn> columns){
		for(SqlColumn col : DrIterableTool.nullSafe(columns)){
			if(col.getName().equals(name)){
				return col;
			}
		}
		return null;
	}

	private List<SqlAlterTableClause> getAlterTableForRemovingIndexes(SortedSet<SqlIndex> indexesToAdd){
		List<SqlAlterTableClause> list = new ArrayList<>();
		if(!options.getDropIndexes()){
			return list;
		}
		if(DrCollectionTool.isEmpty(indexesToAdd)){
			return list;
		}
		StringBuilder sb = new StringBuilder();
		for(SqlIndex index : indexesToAdd){
			sb.append("drop index "+ index.getName() + ",\n");
		}
		sb = new StringBuilder(sb.substring(0, sb.length()-2));
		list.add(new SqlAlterTableClause(sb.toString(), SqlAlterTypes.DROP_INDEX));
		return list;
	}

	private List<SqlAlterTableClause> getAlterTableForAddingIndexes(SortedSet<SqlIndex> indexesToAdd){
		List<SqlAlterTableClause> list = new ArrayList<>();
		if(!options.getAddIndexes()){
			return list;
		}
		if(DrCollectionTool.isEmpty(indexesToAdd)){
			return list;
		}
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
			sb.append(col.getDefaultValueStatement());
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

		if(!options.getAddColumns()){
			return null;
		}
		if(DrCollectionTool.isEmpty(colsToAdd)){
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("add (");
		String typeString;
		for(SqlColumn col : colsToAdd){
			MySqlColumnType type = col.getType();
			typeString = col.getType().toString().toLowerCase();
			sb.append(col.getName() + " " + typeString);
			if(type.shouldSpecifyLength(col.getMaxLength())){
				sb.append("(" + col.getMaxLength() + ")");
			}

			sb.append(col.getDefaultValueStatement());

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
		List<SqlAlterTableClause> list = new ArrayList<>();
		if(!options.getDeleteColumns()){
			return list;
		}
		if(DrCollectionTool.isEmpty(colsToRemove)){
			return list;
		}
		StringBuilder sb = new StringBuilder();
		for(SqlColumn col:colsToRemove){
			sb.append("drop column "+ col.getName() + ",\n");
		}
		sb = new StringBuilder(sb.substring(0, sb.length()-2)); // remove the last ","
		list.add(new SqlAlterTableClause(sb.toString(), SqlAlterTypes.DROP_COLUMN));
		return list;
	}

	private void getColumnsToInitialize(List<SqlColumn> columnsToAdd){
		for(SqlColumn col : columnsToAdd){
			String defaultValueStatement = col.getDefaultValueStatement();
			if(! (defaultValueStatement.equals(NOT_NULL) || DrStringTool.isNull(col.getDefaultValue()))){
				columnsToInitialize.add(col);
			}
		}
	}

	public static class SqlAlterTableGeneratorTester{
		@Test
		public void testDefaultValue() throws IOException{

			SqlColumn colA = new SqlColumn("A", MySqlColumnType.BIGINT), colB = new SqlColumn("B",
					MySqlColumnType.VARCHAR, 250, false, false),
					// boolean field with default value true
					colC = new SqlColumn("C", MySqlColumnType.BOOLEAN, 0, true, false, "true");

			SqlTable table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC), table2 = new SqlTable(
					"TB").addColumn(colA).addColumn(colB);

			SchemaUpdateOptions options = new SchemaUpdateOptions().setAllTrue();

			// case1 : Adding a boolean field to the table with a default value (alter statement + initialize variable)
			SqlAlterTableGenerator alterGenerator21 = new SqlAlterTableGenerator(options, table2, table1, "config");
			System.out.println(alterGenerator21.generateDdl());

			// case2 : Dropping a boolean field from table with a default value specified
			SqlAlterTableGenerator alterGenerator12 = new SqlAlterTableGenerator(options, table1, table2, "config");
			System.out.println(alterGenerator12.generateDdl());

		}
	}

}
