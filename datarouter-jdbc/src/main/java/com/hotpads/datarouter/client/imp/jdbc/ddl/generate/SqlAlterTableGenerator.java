package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class SqlAlterTableGenerator implements DdlGenerator{
	private static final Logger logger = LoggerFactory.getLogger(SqlAlterTableGenerator.class);

	private static final int MINIMUM_ALTER_SIZE = 10;
	private static final String NOT_NULL = " not null";
	private final SchemaUpdateOptions options;
	private final SqlTable current, requested;
	private String databaseName="";
	private boolean dropTable = false;
	private boolean willAlterTable = false;
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
			return singleAlters.stream()
					.map(SqlAlterTableClause::getAlterTable)
					.filter(DrStringTool::containsCharactersBesidesWhitespace)
					.collect(Collectors.joining("\n", "", "\n"));
		}
		StringBuilder sb = new StringBuilder();
		sb.append("alter table " +databaseName + "." +current.getName()+"\n");
		boolean appendedAny = false;
		for(SqlAlterTableClause singleAlter : DrIterableTool.nullSafe(singleAlters)){
			if(singleAlter!=null){
				if(appendedAny){
					sb.append(",\n");
				}
				appendedAny = true;
				sb.append(singleAlter.getAlterTable());
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
			boolean appendedAnyCol = false;
			for(SqlColumn col:columnsToInitialize){
				if(appendedAnyCol){
					sb.append(",");
				}
				appendedAnyCol = true;
				sb.append(col.getName());
				sb.append(" = ");
				sb.append(col.getDefaultValue());
			}
			sb.append(";");
		}
		return sb.toString();
	}

	public boolean willAlterTable(){
		generateDdl();
		return willAlterTable;
	}

	private List<SqlAlterTableClause> generate(){
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
		SortedSet<SqlIndex> uniqueIndexesToAdd = diff.getUniqueIndexesToAdd();
		SortedSet<SqlIndex> uniqueIndexesToRemove = diff.getUniqueIndexesToRemove();

		// generate the alter table statements from columns to add and to remove
		if(colsToRemove.size()<current.getNumberOfColumns()+colsToAdd.size()){

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
					sb.append("(");
					sb.append(requestedCol.getMaxLength());
					sb.append(")");
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
			list.add(new SqlAlterTableClause(listOfColumnsInPkey.stream().map(SqlColumn::getName)
					.collect(Collectors.joining(",", "add primary key (", ")")), SqlAlterTypes.ADD_INDEX));
		}
		if(diff.isIndexesModified()){
			list.addAll(getAlterTableForRemovingIndexes(indexesToRemove));
			list.addAll(getAlterTableForAddingIndexes(indexesToAdd));
		}
		if(diff.isUniqueIndexesModified()){
			list.addAll(getAlterTableForRemovingIndexes(uniqueIndexesToRemove));
			list.addAll(getAlterTableForAddingUniqueIndexes(uniqueIndexesToAdd));
		}
		if(options.getModifyEngine() && diff.isEngineModified()){
			list.add(new SqlAlterTableClause("engine="+requested.getEngine().toString().toLowerCase(),
					SqlAlterTypes.MODIFY_ENGINE));
		}
		if(options.getModifyCharacterSetOrCollation()
				&& (diff.isCharacterSetModified()
						|| diff.isCollationModified()
						|| diff.getColumnsWithCharsetOrCollationToConvert().size() > 0)){

			String collation = requested.getCollation().toString().toLowerCase();
			String characterSet = requested.getCharacterSet().toString().toLowerCase();
			list.add(new SqlAlterTableClause("convert to character set " + characterSet + " collate " + collation
					+ ",\n" + "character set " + characterSet + " collate " + collation,
					SqlAlterTypes.MODIFY_CHARACTER_SET));

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
		if(!options.getDropIndexes() || DrCollectionTool.isEmpty(indexesToAdd)){
			return list;
		}

		StringBuilder sb = new StringBuilder();
		boolean appendedAny = false;
		for(SqlIndex index : indexesToAdd){
			if(appendedAny){
				sb.append(",\n");
			}
			appendedAny = true;
			sb.append("drop index ");
			sb.append(index.getName() );
		}
		list.add(new SqlAlterTableClause(sb.toString(), SqlAlterTypes.DROP_INDEX));
		return list;
	}

	private List<SqlAlterTableClause> getAlterTableForAddingIndexes(SortedSet<SqlIndex> indexesToAdd){
		List<SqlAlterTableClause> alterClause = new ArrayList<>();
		if(!options.getAddIndexes() || DrCollectionTool.isEmpty(indexesToAdd)){
			return alterClause;
		}
		StringBuilder sb = new StringBuilder(indexesToAdd.stream().map(index->"add index "+ index.getName()
				+"("+ getColumns(index.getColumns()) +")")
				.collect(Collectors.joining(",\n")));
		alterClause.add(new SqlAlterTableClause(sb.toString(), SqlAlterTypes.ADD_INDEX));
		return alterClause;
	}

	private List<SqlAlterTableClause> getAlterTableForAddingUniqueIndexes(SortedSet<SqlIndex> uniqueIndexesToAdd){
		List<SqlAlterTableClause> alterClauses = new ArrayList<>();
		if(!options.getAddIndexes() ||DrCollectionTool.isEmpty(uniqueIndexesToAdd)){
			return alterClauses;
		}
		StringBuilder sb = new StringBuilder(uniqueIndexesToAdd.stream().map(index->"add unique index "+ index.getName()
			+"("+ getColumns(index.getColumns()) +")")
				.collect(Collectors.joining(",\n")));
		alterClauses.add(new SqlAlterTableClause(sb.toString(), SqlAlterTypes.ADD_INDEX));
		return alterClauses;
	}

	private SqlAlterTableClause getCreateTableSqlFromListOfColumnsToAdd(List<SqlColumn> colsToAdd){
		//new SqlAlterTable("CREATE TABLE " +current.getName() +";", SqlAlterTableTypes.CREATE_TABLE);
		StringBuilder sb = new StringBuilder("create table " + current.getName());
		if(DrCollectionTool.isEmpty(colsToAdd)){
			return new SqlAlterTableClause(sb.toString(), SqlAlterTypes.CREATE_TABLE);
		}
		sb.append(" ( ");
		boolean appendedAny = false;
		for(SqlColumn col:colsToAdd){
			if(appendedAny){
				 sb.append(",\n");
			}
			appendedAny = true;
			sb.append(col.getName());
			sb.append(" ");
			sb.append(col.getType().toString().toLowerCase());
			if(col.getMaxLength()!=null){
				sb.append("(" );
				sb.append(col.getMaxLength());
				sb.append(")");
			}
			sb.append(col.getDefaultValueStatement());
			if(col.getAutoIncrement()) {
				sb.append(" auto_increment");
			}
		}
		sb.append(");\n");
		return new SqlAlterTableClause(sb.toString(), SqlAlterTypes.CREATE_TABLE);
	}

	private SqlAlterTableClause getAlterTableForAddingColumns(List<SqlColumn> colsToAdd){
		if(!options.getAddColumns() || DrCollectionTool.isEmpty(colsToAdd)){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		boolean appendedAny = false;
		sb.append("add (");
		String typeString;

		for(SqlColumn col : colsToAdd){
			MySqlColumnType type = col.getType();
			typeString = col.getType().toString().toLowerCase();
			if(appendedAny){
				sb.append(",\n");
			}
			appendedAny = true;
			sb.append(col.getName());
			sb.append(" ");
			sb.append(typeString);
			if(type.shouldSpecifyLength(col.getMaxLength())){
				sb.append("(");
				sb.append(col.getMaxLength());
				sb.append(")");
			}

			sb.append(col.getDefaultValueStatement());
			if(col.getAutoIncrement()) {
				sb.append(" auto_increment");
			}
		}
		sb.append(")");
		return new SqlAlterTableClause(sb.toString(), SqlAlterTypes.ADD_COLUMN);
	}

	private List<SqlAlterTableClause> getAlterTableForRemovingColumns(List<SqlColumn> colsToRemove){
		List<SqlAlterTableClause> list = new ArrayList<>();
		if(!options.getDeleteColumns() || DrCollectionTool.isEmpty(colsToRemove)){
			return list;
		}
		StringBuilder sb = new StringBuilder(colsToRemove.stream().map(col->"drop column "+ col.getName())
		.collect(Collectors.joining(",\n")));
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

	private String getColumns(List<SqlColumn> columns){
		return columns.stream().map(col -> col.getName()).collect(Collectors.joining(","));
	}

	public static class SqlAlterTableGeneratorTester{
		@Test
		public void testDefaultValue(){

			SqlColumn colA = new SqlColumn("A", MySqlColumnType.BIGINT), colB = new SqlColumn("B",
					MySqlColumnType.VARCHAR, 250, false, false),
					// boolean field with default value true
					colC = new SqlColumn("C", MySqlColumnType.BOOLEAN, 0, true, false, "true", null, null);

			List<SqlColumn> listB = new ArrayList<>(Arrays.asList(colB));
			List<SqlColumn> listC = new ArrayList<>(Arrays.asList(colC));

			SqlIndex indexB = new SqlIndex("index_b", listB);
			SqlIndex indexC = new SqlIndex("unique_c", listC);

			SqlTable table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC).addUniqueIndex(indexC),
					table2 = new SqlTable("TB").addColumn(colA).addColumn(colB).addIndex(indexB);


			SchemaUpdateOptions options = new SchemaUpdateOptions().setAllTrue();

			// case1 : Adding a boolean field to the table with a default value (alter statement + initialize variable)
			SqlAlterTableGenerator alterGenerator21 = new SqlAlterTableGenerator(options, table2, table1, "config");
			logger.warn(alterGenerator21.generateDdl());

			// case2 : Dropping a boolean field from table with a default value specified
			SqlAlterTableGenerator alterGenerator12 = new SqlAlterTableGenerator(options, table1, table2, "config");
			logger.warn(alterGenerator12.generateDdl());
		}
	}

}
