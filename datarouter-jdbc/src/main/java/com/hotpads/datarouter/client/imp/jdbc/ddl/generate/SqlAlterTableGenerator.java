package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;

import com.hotpads.datarouter.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;

public class SqlAlterTableGenerator{

	private final SchemaUpdateOptions options;
	private final SqlTable current, requested;
	private final String databaseName;

	public SqlAlterTableGenerator(SchemaUpdateOptions options, SqlTable current, SqlTable requested,
			String databaseName){
		this.options = options;
		this.current = current;
		this.requested = requested;
		this.databaseName = databaseName;
	}

	public Optional<String> generateDdl(){
		SqlTableDiffGenerator diff = new SqlTableDiffGenerator(current, requested);
		if(!diff.isTableModified()){
			return Optional.empty();
		}

		if(diff.getColumnsToRemove().size() >= current.getNumberOfColumns()){
			return Optional.of("drop table " + databaseName + "." + current.getName() + ";\n"
					+ new SqlCreateTableGenerator(requested, databaseName).generateDdl());
		}

		String alterTablePrefix = "alter table " + databaseName + "." + current.getName() + "\n";
		return Optional.of(generate(diff).stream()
				.map(SqlAlterTableClause::getAlterTable)
				.collect(Collectors.joining(",\n", alterTablePrefix, ";")));
	}

	private List<SqlAlterTableClause> generate(SqlTableDiffGenerator diff){
		List<SqlAlterTableClause> alters = new ArrayList<>();
		if(options.getAddColumns()){
			alters.addAll(getAlterTableForAddingColumns(diff.getColumnsToAdd()));
		}
		if(options.getDeleteColumns()){
			alters.addAll(getAlterTableForRemovingColumns(diff.getColumnsToRemove()));
		}
		if(options.getModifyColumns()){
			alters.addAll(getAlterTableForModifyingColumns(diff.getColumnsToModify()));
		}
		if(options.getAddIndexes() && options.getDropIndexes() && diff.isPrimaryKeyModified()){
			if(current.hasPrimaryKey()){
				alters.add(new SqlAlterTableClause("drop primary key"));
			}
			List<SqlColumn> listOfColumnsInPkey = requested.getPrimaryKey().getColumns();
			alters.add(new SqlAlterTableClause(listOfColumnsInPkey.stream()
					.map(SqlColumn::getName)
					.collect(Collectors.joining(",", "add primary key (", ")"))));
		}
		if(options.getDropIndexes()){
			alters.addAll(getAlterTableForRemovingIndexes(diff.getIndexesToRemove()));
			alters.addAll(getAlterTableForRemovingIndexes(diff.getUniqueIndexesToRemove()));
		}
		if(options.getAddIndexes()){
			alters.addAll(getAlterTableForAddingIndexes(diff.getIndexesToAdd(), false));
			alters.addAll(getAlterTableForAddingIndexes(diff.getUniqueIndexesToAdd(), true));
		}
		if(options.getModifyEngine() && diff.isEngineModified()){
			alters.add(new SqlAlterTableClause("engine=" + requested.getEngine().toString().toLowerCase()));
		}
		if(options.getModifyCharacterSetOrCollation()
				&& (diff.isCharacterSetModified()
						|| diff.isCollationModified()
						|| diff.getColumnsWithCharsetOrCollationToConvert().size() > 0)){
			String collation = requested.getCollation().toString();
			String characterSet = requested.getCharacterSet().toString();
			String alterClause = "character set " + characterSet + " collate " + collation;
			alters.add(new SqlAlterTableClause("convert to " + alterClause));
			alters.add(new SqlAlterTableClause(alterClause));
		}
		if(options.getModifyRowFormat() && diff.isRowFormatModified()){
			String rowFormat = requested.getRowFormat().getPersistentString();
			alters.add(new SqlAlterTableClause("row_format=" + rowFormat));
		}
		return alters;
	}

	private static List<SqlAlterTableClause> getAlterTableForAddingColumns(List<SqlColumn> colsToAdd){
		return colsToAdd.stream()
				.map(SqlAlterTableGenerator::makeColumnDefinition)
				.map(new StringBuilder("add ")::append)
				.map(SqlAlterTableClause::new)
				.collect(Collectors.toList());
	}

	private static List<SqlAlterTableClause> getAlterTableForRemovingColumns(List<SqlColumn> colsToRemove){
		return colsToRemove.stream()
				.map(SqlColumn::getName)
				.map(new StringBuilder("drop column ")::append)
				.map(SqlAlterTableClause::new)
				.collect(Collectors.toList());
	}

	private static List<SqlAlterTableClause> getAlterTableForModifyingColumns(List<SqlColumn> columnsToModify){
		return columnsToModify.stream()
				.map(SqlAlterTableGenerator::makeColumnDefinition)
				.map(new StringBuilder("modify ")::append)
				.map(sql -> sql.append(","))
				.map(SqlAlterTableClause::new)
				.collect(Collectors.toList());
	}

	private static StringBuilder makeColumnDefinition(SqlColumn column){
		MySqlColumnType type = column.getType();
		StringBuilder sb = new StringBuilder("modify ").append(column.getName()).append(" ")
				.append(type.toString().toLowerCase());
		if(type.shouldSpecifyLength(column.getMaxLength())){
			sb.append("(").append(column.getMaxLength()).append(")");
		}
		sb.append(column.getDefaultValueStatement());
		if(column.getAutoIncrement()){
			sb.append(" auto_increment");
		}
		return sb;
	}

	private static List<SqlAlterTableClause> getAlterTableForRemovingIndexes(SortedSet<SqlIndex> indexesToDrop){
		return indexesToDrop.stream()
				.map(SqlIndex::getName)
				.map(new StringBuilder("drop index ")::append)
				.map(SqlAlterTableClause::new)
				.collect(Collectors.toList());
	}

	private static List<SqlAlterTableClause> getAlterTableForAddingIndexes(SortedSet<SqlIndex> indexesToAdd,
			boolean unique){
		return indexesToAdd.stream()
				.map(index -> {
					String csvColumns = index.getColumns().stream()
							.map(SqlColumn::getName)
							.collect(Collectors.joining(",", "(", ")"));
					StringBuilder sb = new StringBuilder("add ");
					if(unique){
						sb.append("unique ");
					}
					return sb.append("index ").append(index.getName()).append(csvColumns);
				})
				.map(SqlAlterTableClause::new)
				.collect(Collectors.toList());
	}

}
