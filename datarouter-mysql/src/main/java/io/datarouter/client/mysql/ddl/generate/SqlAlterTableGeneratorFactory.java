/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.mysql.ddl.generate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.ddl.domain.SqlIndex;
import io.datarouter.client.mysql.ddl.domain.SqlTable;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;

public class SqlAlterTableGeneratorFactory{

	private static class DdlBuilder{

		private static final boolean PREVENT_START_UP = true;

		final List<SqlAlterTableClause> executeAlters;
		final List<SqlAlterTableClause> printAlters;
		boolean preventStartUp;

		DdlBuilder(){
			executeAlters = new ArrayList<>();
			printAlters = new ArrayList<>();
		}

		void add(Boolean execute, SqlAlterTableClause alter, boolean required){
			add(execute, Arrays.asList(alter), required);
		}

		void add(Boolean execute, List<SqlAlterTableClause> alters, boolean required){
			List<SqlAlterTableClause> list = execute ? executeAlters : printAlters;
			list.addAll(alters);
			if(!alters.isEmpty() && !execute && required && PREVENT_START_UP){
				preventStartUp = true;
			}
		}

		Ddl build(String alterTablePrefix){
			return new Ddl(makeStatementFromClauses(alterTablePrefix, executeAlters), makeStatementFromClauses(
					alterTablePrefix, printAlters), preventStartUp);
		}

		Optional<String> makeStatementFromClauses(String alterTablePrefix, List<SqlAlterTableClause> alters){
			if(alters.isEmpty()){
				return Optional.empty();
			}
			return Optional.of(alters.stream()
					.map(SqlAlterTableClause::getAlterTable)
					.collect(Collectors.joining(",\n", alterTablePrefix, ";")));
		}

	}

	@Inject
	private SchemaUpdateOptions schemaUpdateOptions;

	public class SqlAlterTableGenerator{

		private final SqlTable current;
		private final SqlTable requested;
		private final String databaseName;

		public SqlAlterTableGenerator(SqlTable current, SqlTable requested, String databaseName){
			this.current = current;
			this.requested = requested;
			this.databaseName = databaseName;
		}

		public Ddl generateDdl(){
			SqlTableDiffGenerator diff = new SqlTableDiffGenerator(current, requested);
			if(!diff.isTableModified()){
				return new Ddl(Optional.empty(), Optional.empty(), false);
			}
			DdlBuilder ddlBuilder = generate(diff);
			String alterTablePrefix = "alter table " + databaseName + "." + current.getName() + "\n";
			return ddlBuilder.build(alterTablePrefix);
		}

		private boolean printOrExecute(BiFunction<SchemaUpdateOptions,Boolean,Boolean> option){
			return option.apply(schemaUpdateOptions, false) || option.apply(schemaUpdateOptions, true);
		}

		private DdlBuilder generate(SqlTableDiffGenerator diff){
			DdlBuilder ddlBuilder = new DdlBuilder();

			if(printOrExecute(SchemaUpdateOptions::getAddColumns)){
				ddlBuilder.add(schemaUpdateOptions.getAddColumns(false), getAlterTableForAddingColumns(diff
						.getColumnsToAdd()), true);
			}
			if(printOrExecute(SchemaUpdateOptions::getDeleteColumns)){
				ddlBuilder.add(schemaUpdateOptions.getDeleteColumns(false), getAlterTableForRemovingColumns(diff
						.getColumnsToRemove()), false);
			}
			if(printOrExecute(SchemaUpdateOptions::getModifyColumns)){
				ddlBuilder.add(schemaUpdateOptions.getModifyColumns(false), getAlterTableForModifyingColumns(diff
						.getColumnsToModify()), false);
			}
			if(printOrExecute(SchemaUpdateOptions::getModifyPrimaryKey) && diff.isPrimaryKeyModified()){
				if(current.hasPrimaryKey()){
					ddlBuilder.add(schemaUpdateOptions.getModifyPrimaryKey(false), new SqlAlterTableClause(
							"drop primary key"), false);
				}
				ddlBuilder.add(schemaUpdateOptions.getModifyPrimaryKey(false), new SqlAlterTableClause(requested
						.getPrimaryKey().getColumnNames().stream().collect(Collectors.joining(",", "add primary key (",
								")"))), false);
			}
			if(printOrExecute(SchemaUpdateOptions::getDropIndexes)){
				ddlBuilder.add(schemaUpdateOptions.getDropIndexes(false), getAlterTableForRemovingIndexes(diff
						.getIndexesToRemove()), false);
				ddlBuilder.add(schemaUpdateOptions.getDropIndexes(false), getAlterTableForRemovingIndexes(diff
						.getUniqueIndexesToRemove()), false);
			}
			if(printOrExecute(SchemaUpdateOptions::getAddIndexes)){
				ddlBuilder.add(schemaUpdateOptions.getAddIndexes(false), getAlterTableForAddingIndexes(diff
						.getIndexesToAdd(), false), true);
				ddlBuilder.add(schemaUpdateOptions.getAddIndexes(false), getAlterTableForAddingIndexes(diff
						.getUniqueIndexesToAdd(), true), true);
			}
			if(printOrExecute(SchemaUpdateOptions::getModifyEngine) && diff.isEngineModified()){
				ddlBuilder.add(schemaUpdateOptions.getModifyEngine(false), new SqlAlterTableClause("engine=" + requested
						.getEngine().toString().toLowerCase()), false);
			}
			if(printOrExecute(SchemaUpdateOptions::getModifyCharacterSetOrCollation) && (diff.isCharacterSetModified()
					|| diff.isCollationModified())){
				String collation = requested.getCollation().toString();
				String characterSet = requested.getCharacterSet().toString();
				String alterClause = "character set " + characterSet + " collate " + collation;
				ddlBuilder.add(schemaUpdateOptions.getModifyCharacterSetOrCollation(false), new SqlAlterTableClause(
						alterClause), false);
			}
			if(printOrExecute(SchemaUpdateOptions::getModifyRowFormat) && diff.isRowFormatModified()){
				String rowFormat = requested.getRowFormat().getPersistentString();
				ddlBuilder.add(schemaUpdateOptions.getModifyRowFormat(false), new SqlAlterTableClause("row_format="
						+ rowFormat), false);
			}
			return ddlBuilder;
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
