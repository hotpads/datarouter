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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.ddl.domain.SqlIndex;
import io.datarouter.client.mysql.ddl.domain.SqlTable;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.util.tuple.Pair;

public class SqlAlterTableGeneratorFactory{

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

		public Pair<Optional<String>,Optional<String>> generateDdl(){
			SqlTableDiffGenerator diff = new SqlTableDiffGenerator(current, requested);
			if(!diff.isTableModified()){
				return new Pair<>(Optional.empty(), Optional.empty());
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

		private boolean printOrExecute(BiFunction<SchemaUpdateOptions,Boolean,Boolean> option){
			return option.apply(schemaUpdateOptions, false) || option.apply(schemaUpdateOptions, true);
		}

		private Map<Boolean,List<SqlAlterTableClause>> generate(SqlTableDiffGenerator diff){
			Map<Boolean,List<SqlAlterTableClause>> alters = new HashMap<>();
			alters.put(true, new ArrayList<>());
			alters.put(false, new ArrayList<>());

			if(printOrExecute(SchemaUpdateOptions::getAddColumns)){
				alters.get(schemaUpdateOptions.getAddColumns(false)).addAll(getAlterTableForAddingColumns(diff
						.getColumnsToAdd()));
			}
			if(printOrExecute(SchemaUpdateOptions::getDeleteColumns)){
				alters.get(schemaUpdateOptions.getDeleteColumns(false)).addAll(getAlterTableForRemovingColumns(diff
						.getColumnsToRemove()));
			}
			if(printOrExecute(SchemaUpdateOptions::getModifyColumns)){
				alters.get(schemaUpdateOptions.getModifyColumns(false)).addAll(getAlterTableForModifyingColumns(diff
						.getColumnsToModify()));
			}
			if(printOrExecute(SchemaUpdateOptions::getModifyPrimaryKey) && diff.isPrimaryKeyModified()){
				if(current.hasPrimaryKey()){
					alters.get(schemaUpdateOptions.getModifyPrimaryKey(false)).add(new SqlAlterTableClause(
							"drop primary key"));
				}
				alters.get(schemaUpdateOptions.getModifyPrimaryKey(false)).add(new SqlAlterTableClause(requested
						.getPrimaryKey().getColumnNames().stream()
								.collect(Collectors.joining(",", "add primary key (", ")"))));
			}
			if(printOrExecute(SchemaUpdateOptions::getDropIndexes)){
				alters.get(schemaUpdateOptions.getDropIndexes(false)).addAll(getAlterTableForRemovingIndexes(diff
						.getIndexesToRemove()));
				alters.get(schemaUpdateOptions.getDropIndexes(false)).addAll(getAlterTableForRemovingIndexes(diff
						.getUniqueIndexesToRemove()));
			}
			if(printOrExecute(SchemaUpdateOptions::getAddIndexes)){
				alters.get(schemaUpdateOptions.getAddIndexes(false)).addAll(getAlterTableForAddingIndexes(diff
						.getIndexesToAdd(), false));
				alters.get(schemaUpdateOptions.getAddIndexes(false)).addAll(getAlterTableForAddingIndexes(diff
						.getUniqueIndexesToAdd(), true));
			}
			if(printOrExecute(SchemaUpdateOptions::getModifyEngine) && diff.isEngineModified()){
				alters.get(schemaUpdateOptions.getModifyEngine(false)).add(new SqlAlterTableClause("engine=" + requested
						.getEngine().toString().toLowerCase()));
			}
			if(printOrExecute(SchemaUpdateOptions::getModifyCharacterSetOrCollation)
					&& (diff.isCharacterSetModified() || diff.isCollationModified())){
				String collation = requested.getCollation().toString();
				String characterSet = requested.getCharacterSet().toString();
				String alterClause = "character set " + characterSet + " collate " + collation;
				alters.get(schemaUpdateOptions.getModifyCharacterSetOrCollation(false)).add(new SqlAlterTableClause(
						alterClause));
			}
			if(printOrExecute(SchemaUpdateOptions::getModifyRowFormat) && diff.isRowFormatModified()){
				String rowFormat = requested.getRowFormat().getPersistentString();
				alters.get(schemaUpdateOptions.getModifyRowFormat(false)).add(new SqlAlterTableClause("row_format="
						+ rowFormat));
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
