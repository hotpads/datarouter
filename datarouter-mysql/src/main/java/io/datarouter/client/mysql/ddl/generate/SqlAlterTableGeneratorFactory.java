/*
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.ddl.domain.SqlIndex;
import io.datarouter.client.mysql.ddl.domain.SqlTable;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SqlAlterTableGeneratorFactory{

	private enum PrintVersion{
		BOTH,
		QUICK,
		THOROUGH,
		;
	}

	private static class DdlBuilder{

		private static final boolean PREVENT_START_UP = true;

		final List<CharSequence> executeAlters;
		final List<CharSequence> quickPrintAlters;
		final List<CharSequence> thoroughPrintAlters;
		boolean preventStartUp;

		DdlBuilder(){
			executeAlters = new ArrayList<>();
			quickPrintAlters = new ArrayList<>();
			thoroughPrintAlters = new ArrayList<>();
		}

		void add(Boolean execute, CharSequence alter, boolean required){
			add(execute, Arrays.asList(alter), required, PrintVersion.BOTH);
		}

		void add(Boolean execute, List<CharSequence> alters, boolean required){
			add(execute, alters, required, PrintVersion.BOTH);
		}

		void add(Boolean execute, CharSequence alter, boolean required, PrintVersion printVersion){
			add(execute, Arrays.asList(alter), required, printVersion);
		}

		void add(Boolean execute, List<CharSequence> alters, boolean required, PrintVersion printVersion){
			if(execute){
				executeAlters.addAll(alters);
			}else{
				switch(printVersion){
				case BOTH:
					quickPrintAlters.addAll(alters);
					thoroughPrintAlters.addAll(alters);
					break;
				case QUICK:
					quickPrintAlters.addAll(alters);
					break;
				case THOROUGH:
					thoroughPrintAlters.addAll(alters);
					break;
				}
			}
			if(!alters.isEmpty() && !execute && required && PREVENT_START_UP){
				preventStartUp = true;
			}
		}

		Ddl build(String hostname, String databaseName, String tableName){
			String alterTablePrefix = "alter table " + databaseName + "." + tableName + "\n";
			Optional<String> print;
			if(quickPrintAlters.isEmpty() && thoroughPrintAlters.isEmpty()){
				print = Optional.empty();
			}else{
				Stream<List<CharSequence>> scanner;
				if(quickPrintAlters.equals(thoroughPrintAlters)){
					scanner = Stream.of(quickPrintAlters);
				}else{
					scanner = Stream.of(quickPrintAlters, thoroughPrintAlters);
				}
				print = Optional.of(scanner
						.map(clauses -> makeAlter(alterTablePrefix, clauses) + "\n"
								+ "\n"
								+ percona(hostname, databaseName, tableName, clauses))
						.collect(Collectors.joining("\n\n")));
			}
			return new Ddl(
					makeStatementFromClauses(alterTablePrefix, executeAlters),
					print,
					preventStartUp);
		}

		private String percona(String hostname, String databaseName, String tableName, List<CharSequence> clauses){
			return "pt-online-schema-change "
					+ "h=" + hostname + ",D=" + databaseName + ",t=" + tableName + " "
					+ "--execute "
					+ "--user=root "
					+ "--ask-pass "
					+ "--critical-load \"Threads_running=500\" "
					+ "--alter=\"" + String.join(", ", clauses) + "\"";
		}

		Optional<String> makeStatementFromClauses(String alterTablePrefix, List<CharSequence> alters){
			if(alters.isEmpty()){
				return Optional.empty();
			}
			return Optional.of(makeAlter(alterTablePrefix, alters));
		}

		String makeAlter(String alterTablePrefix, List<CharSequence> alters){
			return alterTablePrefix + String.join(",\n", alters) + ";";
		}

	}

	@Inject
	private SchemaUpdateOptions schemaUpdateOptions;

	public class SqlAlterTableGenerator{

		private final SqlTable current;
		private final SqlTable requested;
		private final String hostname;
		private final String databaseName;

		public SqlAlterTableGenerator(SqlTable current, SqlTable requested, String hostname, String databaseName){
			this.current = current;
			this.requested = requested;
			this.hostname = hostname;
			this.databaseName = databaseName;
		}

		public Ddl generateDdl(){
			SqlTableDiffGenerator diff = new SqlTableDiffGenerator(current, requested);
			if(!diff.isTableModified()){
				return new Ddl(Optional.empty(), Optional.empty(), false);
			}
			DdlBuilder ddlBuilder = generate(diff);
			return ddlBuilder.build(hostname, databaseName, current.getName());
		}

		private boolean printOrExecute(Function<Boolean,Boolean> option){
			return option.apply(false) || option.apply(true);
		}

		private DdlBuilder generate(SqlTableDiffGenerator diff){
			DdlBuilder ddlBuilder = new DdlBuilder();

			if(printOrExecute(schemaUpdateOptions::getAddColumns)){
				ddlBuilder.add(
						schemaUpdateOptions.getAddColumns(false),
						getAlterTableForAddingColumns(diff.getColumnsToAdd()),
						true);
			}
			if(printOrExecute(schemaUpdateOptions::getDeleteColumns)){
				ddlBuilder.add(
						schemaUpdateOptions.getDeleteColumns(false),
						getAlterTableForRemovingColumns(diff.getColumnsToRemove()),
						false);
			}
			if(printOrExecute(schemaUpdateOptions::getModifyColumns)){
				ddlBuilder.add(
						schemaUpdateOptions.getModifyColumns(false),
						getAlterTableForModifyingColumns(diff.getColumnsToModify()),
						false);
			}
			if(printOrExecute(schemaUpdateOptions::getModifyPrimaryKey) && diff.isPrimaryKeyModified()){
				boolean execute = schemaUpdateOptions.getModifyPrimaryKey(false);
				List<String> pkColumnNames = requested.getPrimaryKey().getColumnNames();
				addPk(ddlBuilder, execute, pkColumnNames, PrintVersion.QUICK);
				if(!execute){
					boolean pkUniqueIndexExists = current.getUniqueIndexes().stream()
							.anyMatch(index -> index.getColumnNames().equals(pkColumnNames));
					if(pkUniqueIndexExists){
						addPk(ddlBuilder, execute, pkColumnNames, PrintVersion.THOROUGH);
					}else{
						var sqlIndex = new SqlIndex("temp_pk", pkColumnNames);
						ddlBuilder.add(
								execute,
								getAlterTableForAddingIndexes(Set.of(sqlIndex), true),
								true,
								PrintVersion.THOROUGH);
					}
				}
			}
			if(printOrExecute(schemaUpdateOptions::getDropIndexes)){
				ddlBuilder.add(
						schemaUpdateOptions.getDropIndexes(false),
						getAlterTableForRemovingIndexes(diff.getIndexesToRemove()),
						false);
				ddlBuilder.add(
						schemaUpdateOptions.getDropIndexes(false),
						getAlterTableForRemovingIndexes(diff.getUniqueIndexesToRemove()),
						false);
			}
			if(printOrExecute(schemaUpdateOptions::getAddIndexes)){
				ddlBuilder.add(
						schemaUpdateOptions.getAddIndexes(false),
						getAlterTableForAddingIndexes(diff.getIndexesToAdd(), false),
						true);
				ddlBuilder.add(
						schemaUpdateOptions.getAddIndexes(false),
						getAlterTableForAddingIndexes(diff.getUniqueIndexesToAdd(), true),
						true);
			}
			if(printOrExecute(schemaUpdateOptions::getModifyEngine) && diff.isEngineModified()){
				ddlBuilder.add(
						schemaUpdateOptions.getModifyEngine(false),
						"engine=" + requested.getEngine().toString().toLowerCase(),
						false);
			}
			if(printOrExecute(schemaUpdateOptions::getModifyCharacterSetOrCollation)
					&& (diff.isCharacterSetModified() || diff.isCollationModified())){
				String characterSet = requested.getCharacterSet().toString();
				String collation = requested.getCollation().toString();
				ddlBuilder.add(
						schemaUpdateOptions.getModifyCharacterSetOrCollation(false),
						"character set " + characterSet + " collate " + collation,
						false);
			}
			if(printOrExecute(schemaUpdateOptions::getModifyRowFormat) && diff.isRowFormatModified()){
				ddlBuilder.add(
						schemaUpdateOptions.getModifyRowFormat(false),
						"row_format=" + requested.getRowFormat().value,
						false);
			}
			return ddlBuilder;
		}

		private void addPk(DdlBuilder ddlBuilder, Boolean execute, List<String> pkColumNames,
				PrintVersion printVersion){
			if(current.hasPrimaryKey()){
				ddlBuilder.add(
						execute,
						"drop primary key",
						false,
						printVersion);
			}
			ddlBuilder.add(
					execute,
					"add primary key (" + String.join(",", pkColumNames) + ")",
					false,
					printVersion);
		}

		private List<CharSequence> getAlterTableForAddingColumns(List<SqlColumn> colsToAdd){
			return colsToAdd.stream()
					.map(this::makeAddColumnDefinition)
					.collect(Collectors.toList());
		}

		private List<CharSequence> getAlterTableForRemovingColumns(List<SqlColumn> colsToRemove){
			return colsToRemove.stream()
					.map(SqlColumn::getName)
					.map("drop column "::concat)
					.collect(Collectors.toList());
		}

		private List<CharSequence> getAlterTableForModifyingColumns(List<SqlColumn> columnsToModify){
			return columnsToModify.stream()
					.map(this::makeModifyColumnDefinition)
					.collect(Collectors.toList());
		}

		private StringBuilder makeModifyColumnDefinition(SqlColumn column){
			return column.makeColumnDefinition("modify ");
		}

		private StringBuilder makeAddColumnDefinition(SqlColumn column){
			return column.makeColumnDefinition("add ");
		}

		private List<CharSequence> getAlterTableForRemovingIndexes(Set<SqlIndex> indexesToDrop){
			return indexesToDrop.stream()
					.map(SqlIndex::getName)
					.map("drop index "::concat)
					.collect(Collectors.toList());
		}

		private List<CharSequence> getAlterTableForAddingIndexes(
				Set<SqlIndex> indexesToAdd,
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
					.collect(Collectors.toList());
		}

	}

}
