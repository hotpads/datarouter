package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.hotpads.datarouter.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.util.core.collections.Pair;

public class SqlAlterTableGeneratorFactory{

	@Inject
	private SqlCreateTableGenerator sqlCreateTableGenerator;

	public class SqlAlterTableGenerator{
		private final SchemaUpdateOptions executeOptions;
		private final SchemaUpdateOptions printOptions;
		private final SqlTable current;
		private final SqlTable requested;
		private final String databaseName;

		public SqlAlterTableGenerator(SchemaUpdateOptions executeOptions, SchemaUpdateOptions printOptions,
				SqlTable current, SqlTable requested, String databaseName){
			this.executeOptions = executeOptions;
			this.printOptions = printOptions;
			this.current = current;
			this.requested = requested;
			this.databaseName = databaseName;
		}

		public Pair<Optional<String>,Optional<String>> generateDdl(){
			SqlTableDiffGenerator diff = new SqlTableDiffGenerator(current, requested);
			if(!diff.isTableModified()){
				return new Pair<>(Optional.empty(), Optional.empty());
			}

			if(printOrExecute(SchemaUpdateOptions::getDeleteColumns)
					&& diff.getColumnsToRemove().size() >= current.getNumberOfColumns()){
				return new Pair<>(Optional.of("drop table " + databaseName + "." + current.getName() + ";\n"
						+ sqlCreateTableGenerator.generateDdl(requested, databaseName)), Optional.empty());
			}

			Map<Boolean,List<SqlAlterTableClause>> alters = generate(diff);
			List<SqlAlterTableClause> executeAlters = alters.get(true);
			List<SqlAlterTableClause> printAlters = alters.get(false);
			String alterTablePrefix = "alter table " + databaseName + "." + current.getName() + "\n";
			return new Pair<>(makeStatementFromClauses(alterTablePrefix, executeAlters),
					makeStatementFromClauses(alterTablePrefix, printAlters));
		}

		private Optional<String> makeStatementFromClauses(String alterTablePrefix, List<SqlAlterTableClause> alters){
			if(alters.isEmpty()){
				return Optional.empty();
			}
			return Optional.of(alters.stream()
					.map(SqlAlterTableClause::getAlterTable)
					.collect(Collectors.joining(",\n", alterTablePrefix, ";")));
		}

		private boolean printOrExecute(Function<SchemaUpdateOptions,Boolean> option){
			return option.apply(executeOptions) || option.apply(printOptions);
		}

		private Map<Boolean,List<SqlAlterTableClause>> generate(SqlTableDiffGenerator diff){
			Map<Boolean,List<SqlAlterTableClause>> alters = new HashMap<>();
			alters.put(true, new ArrayList<>());
			alters.put(false, new ArrayList<>());

			if(printOrExecute(SchemaUpdateOptions::getAddColumns)){
				alters.get(executeOptions.getAddColumns()).addAll(getAlterTableForAddingColumns(diff
						.getColumnsToAdd()));
			}
			if(printOrExecute(SchemaUpdateOptions::getDeleteColumns)){
				alters.get(executeOptions.getDeleteColumns()).addAll(getAlterTableForRemovingColumns(diff
						.getColumnsToRemove()));
			}
			if(printOrExecute(SchemaUpdateOptions::getModifyColumns)){
				alters.get(executeOptions.getModifyColumns()).addAll(getAlterTableForModifyingColumns(diff
						.getColumnsToModify()));
			}
			if(printOrExecute(SchemaUpdateOptions::getModifyPrimaryKey) && diff.isPrimaryKeyModified()){
				if(current.hasPrimaryKey()){
					alters.get(executeOptions.getModifyPrimaryKey()).add(new SqlAlterTableClause("drop primary key"));
				}
				alters.get(executeOptions.getModifyPrimaryKey()).add(new SqlAlterTableClause(requested.getPrimaryKey()
						.getColumnNames().stream().collect(Collectors.joining(",", "add primary key (", ")"))));
			}
			if(printOrExecute(SchemaUpdateOptions::getDropIndexes)){
				alters.get(executeOptions.getDropIndexes()).addAll(getAlterTableForRemovingIndexes(diff
						.getIndexesToRemove()));
				alters.get(executeOptions.getDropIndexes()).addAll(getAlterTableForRemovingIndexes(diff
						.getUniqueIndexesToRemove()));
			}
			if(printOrExecute(SchemaUpdateOptions::getAddIndexes)){
				alters.get(executeOptions.getAddIndexes()).addAll(getAlterTableForAddingIndexes(diff.getIndexesToAdd(),
						false));
				alters.get(executeOptions.getAddIndexes()).addAll(getAlterTableForAddingIndexes(diff
						.getUniqueIndexesToAdd(), true));
			}
			if(printOrExecute(SchemaUpdateOptions::getModifyEngine) && diff.isEngineModified()){
				alters.get(executeOptions.getModifyEngine()).add(new SqlAlterTableClause("engine=" + requested
						.getEngine().toString().toLowerCase()));
			}
			if(printOrExecute(SchemaUpdateOptions::getModifyCharacterSetOrCollation)
					&& (diff.isCharacterSetModified() || diff.isCollationModified())){
				String collation = requested.getCollation().toString();
				String characterSet = requested.getCharacterSet().toString();
				String alterClause = "character set " + characterSet + " collate " + collation;
				alters.get(executeOptions.getModifyCharacterSetOrCollation()).add(new SqlAlterTableClause(alterClause));
			}
			if(printOrExecute(SchemaUpdateOptions::getModifyRowFormat) && diff.isRowFormatModified()){
				String rowFormat = requested.getRowFormat().getPersistentString();
				alters.get(executeOptions.getModifyRowFormat()).add(new SqlAlterTableClause("row_format=" + rowFormat));
			}
			return alters;
		}

		private List<SqlAlterTableClause> getAlterTableForAddingColumns(List<SqlColumn> colsToAdd){
			return colsToAdd.stream()
					.map(this::makeAddColumnDefinition)
					.map(SqlAlterTableClause::new)
					.collect(Collectors.toList());
		}

		private List<SqlAlterTableClause> getAlterTableForRemovingColumns(List<SqlColumn> colsToRemove){
			return colsToRemove.stream()
					.map(SqlColumn::getName)
					.map("drop column "::concat)
					.map(SqlAlterTableClause::new)
					.collect(Collectors.toList());
		}

		private List<SqlAlterTableClause> getAlterTableForModifyingColumns(List<SqlColumn> columnsToModify){
			return columnsToModify.stream()
					.map(this::makeModifyColumnDefinition)
					.map(SqlAlterTableClause::new)
					.collect(Collectors.toList());
		}

		private StringBuilder makeModifyColumnDefinition(SqlColumn column){
			return column.makeColumnDefinition("modify ");
		}

		private StringBuilder makeAddColumnDefinition(SqlColumn column){
			return column.makeColumnDefinition("add ");
		}

		private List<SqlAlterTableClause> getAlterTableForRemovingIndexes(Set<SqlIndex> indexesToDrop){
			return indexesToDrop.stream()
					.map(SqlIndex::getName)
					.map("drop index "::concat)
					.map(SqlAlterTableClause::new)
					.collect(Collectors.toList());
		}

		private List<SqlAlterTableClause> getAlterTableForAddingIndexes(Set<SqlIndex> indexesToAdd,
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

}
