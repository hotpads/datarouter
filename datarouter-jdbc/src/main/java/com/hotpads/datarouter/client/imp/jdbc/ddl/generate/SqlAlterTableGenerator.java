package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.hotpads.datarouter.SchemaUpdateOptions;
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

		List<SqlAlterTableClause> alters = generate(diff);
		if(alters.isEmpty()){
			return Optional.empty();
		}
		String alterTablePrefix = "alter table " + databaseName + "." + current.getName() + "\n";
		return Optional.of(alters.stream()
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
			alters.add(new SqlAlterTableClause(requested.getPrimaryKey().getColumnNames().stream()
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
						|| diff.isCollationModified())){
			String collation = requested.getCollation().toString();
			String characterSet = requested.getCharacterSet().toString();
			String alterClause = "character set " + characterSet + " collate " + collation;
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
				.map(SqlAlterTableGenerator::makeAddColumnDefinition)
				.map(SqlAlterTableClause::new)
				.collect(Collectors.toList());
	}

	private static List<SqlAlterTableClause> getAlterTableForRemovingColumns(List<SqlColumn> colsToRemove){
		return colsToRemove.stream()
				.map(SqlColumn::getName)
				.map("drop column "::concat)
				.map(SqlAlterTableClause::new)
				.collect(Collectors.toList());
	}

	private static List<SqlAlterTableClause> getAlterTableForModifyingColumns(List<SqlColumn> columnsToModify){
		return columnsToModify.stream()
				.map(SqlAlterTableGenerator::makeModifyColumnDefinition)
				.map(SqlAlterTableClause::new)
				.collect(Collectors.toList());
	}

	private static StringBuilder makeModifyColumnDefinition(SqlColumn column){
		return column.makeColumnDefinition("modify ");
	}

	private static StringBuilder makeAddColumnDefinition(SqlColumn column){
		return column.makeColumnDefinition("add ");
	}

	private static List<SqlAlterTableClause> getAlterTableForRemovingIndexes(Set<SqlIndex> indexesToDrop){
		return indexesToDrop.stream()
				.map(SqlIndex::getName)
				.map("drop index "::concat)
				.map(SqlAlterTableClause::new)
				.collect(Collectors.toList());
	}

	private static List<SqlAlterTableClause> getAlterTableForAddingIndexes(Set<SqlIndex> indexesToAdd,
			boolean unique){
		return indexesToAdd.stream()
				.map(index -> {
					String csvColumns = index.getColumnNames().stream()
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
