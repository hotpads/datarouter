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
package io.datarouter.gcp.spanner.ddl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import com.google.cloud.spanner.ResultSet;

import io.datarouter.bytes.KvString;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.schema.InvalidSchemaUpdateException;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SpannerTableAlterSchemaService{

	@Inject
	private SchemaUpdateOptions updateOptions;

	public void generateUpdateStatementColumns(
			String tableName,
			List<SpannerColumn> currentColumns,
			List<SpannerColumn> currentPkColumns,
			ResultSet columnResult,
			ResultSet primaryKeyResult,
			SpannerUpdateStatements statements){
		List<SpannerColumn> primaryKeyColumns = extractColumns(primaryKeyResult);
		if(!primaryKeyColumns.equals(currentPkColumns)){
			throw new RuntimeException("Cannot modify primary key columns in spanner tableName=" + tableName);
		}
		List<SpannerColumn> columns = extractColumns(columnResult);
		Map<SpannerColumn,SpannerColumn> columnsToCompareTypes = columnNameIntersection(currentColumns, columns);
		columnsToCompareTypes.forEach((currCol, newCol) -> {
			if(!currCol.getType().equals(newCol.getType())){
				throw new InvalidSchemaUpdateException(
						"Do not change the type of a Spanner column, instead add a new column and migrate the data."
						+ new KvString()
						.add("TableName", tableName)
						.add("ColumnName", currCol.getName())
						.add("CurrentColumnType", currCol.getType().toString())
						.add("NewColumnType", newCol.getType().toString()));
			}
		});
		List<SpannerColumn> colToAdd = columnNameDifferences(currentColumns, columns);
		List<SpannerColumn> colToRemove = columnNameDifferences(columns, currentColumns);
		List<SpannerColumn> colToAlter = columnsToAlter(currentColumns, columns);
		colToAdd.forEach(col -> statements.updateFunction(
				SpannerTableOperationsTool.addColumns(tableName, col),
				updateOptions::getAddColumns,
				true));
		colToRemove.forEach(col -> statements.updateFunction(
				SpannerTableOperationsTool.dropColumns(tableName, col),
				updateOptions::getDeleteColumns,
				false));
		colToAlter.forEach(col -> statements.updateFunction(
				SpannerTableOperationsTool.alterColumns(tableName, col),
				updateOptions::getModifyColumns,
				false));
	}

	public Set<String> getIndexes(ResultSet rs){
		Set<String> result = new HashSet<>();
		while(rs.next()){
			result.add(rs.getString(SpannerTableOperationsTool.INDEX_NAME));
		}
		result.remove(SpannerTableOperationsTool.PRIMARY_KEY);
		return result;
	}

	public record SpannerIndexChanges(
			String indexName,
			List<String> existingKeyColumns,
			List<String> proposedKeyColumns){
	}

	public Optional<SpannerIndexChanges> computeChanges(
			List<SpannerColumn> primaryKeyColumns,
			SpannerIndex proposedIndex,
			ResultSet existingIndex){
		Set<String> primaryKeyColumnNames = Scanner.of(primaryKeyColumns)
				.map(SpannerColumn::getName)
				.collect(HashSet::new);
		List<String> proposedKeyColumns = Scanner.of(proposedIndex.keyFields())
				.map(Field::getKey)
				.map(FieldKey::getColumnName)
				.list();
		Set<String> proposedCoveringColumns = Scanner.of(proposedIndex.getNonKeyFields())
				.map(Field::getKey)
				.map(FieldKey::getColumnName)
				// Spanner automatically adds these to the STORING clause implicitly and will throw an exception if
				// we explicitly add them.
				.exclude(primaryKeyColumnNames::contains)
				.collect(HashSet::new);
		Map<Integer,String> existingKeyColumnsByIndex = new TreeMap<>();
		Set<String> existingCoveringColumns = new HashSet<>();
		while(existingIndex.next()){
			String columnName = existingIndex.getString(SpannerTableOperationsTool.COLUMN_NAME);
			if(existingIndex.isNull(SpannerTableOperationsTool.ORDINAL_POSITION)){
				existingCoveringColumns.add(columnName);
			}else{
				int ordinalPosition = (int)existingIndex.getLong(SpannerTableOperationsTool.ORDINAL_POSITION);
				// Spanner ordinal positions are 1-indexed
				existingKeyColumnsByIndex.put(ordinalPosition - 1, columnName);
			}
		}
		if(!proposedCoveringColumns.equals(existingCoveringColumns)){
			throw new InvalidSchemaUpdateException(
					"Cannot change the covering columns of an index=" + proposedIndex.indexName() + " from existing="
							+ existingCoveringColumns + " to proposed=" + proposedCoveringColumns);
		}
		List<String> existingKeyColumns = new ArrayList<>(existingKeyColumnsByIndex.values());
		return proposedKeyColumns.equals(existingKeyColumns) ? Optional.empty() : Optional.of(
				new SpannerIndexChanges(proposedIndex.indexName(), existingKeyColumns, proposedKeyColumns));
	}

	private List<SpannerColumn> extractColumns(ResultSet rs){
		List<SpannerColumn> columns = new ArrayList<>();
		while(rs.next()){
			String columnName = rs.getString(SpannerTableOperationsTool.COLUMN_NAME);
			boolean isNullable = rs.getString(SpannerTableOperationsTool.IS_NULLABLE).equalsIgnoreCase("YES");
			SpannerColumnType type = SpannerColumnType.fromSchemaString(
					rs.getString(SpannerTableOperationsTool.SPANNER_TYPE));
			columns.add(new SpannerColumn(columnName, type, isNullable));
		}
		return columns;
	}

	private List<SpannerColumn> columnNameDifferences(List<SpannerColumn> columns1, List<SpannerColumn> columns2){
		Map<String,SpannerColumn> col1Map = Scanner.of(columns1)
				.toMapSupplied(SpannerColumn::getName, LinkedHashMap::new);
		columns2.forEach(col -> col1Map.remove(col.getName()));
		return new ArrayList<>(col1Map.values());
	}

	private Map<SpannerColumn,SpannerColumn> columnNameIntersection(
			List<SpannerColumn> columns1,
			List<SpannerColumn> columns2){
		Map<String,SpannerColumn> nameToColumns2 = Scanner.of(columns2)
				.toMapSupplied(SpannerColumn::getName, LinkedHashMap::new);
		Map<SpannerColumn,SpannerColumn> intersectionMap = Scanner.of(columns1)
				.toMap(col1 -> col1, col1 -> nameToColumns2.get(col1.getName()));
		intersectionMap.values().removeIf(Objects::isNull);
		return intersectionMap;
	}

	private List<SpannerColumn> columnsToAlter(List<SpannerColumn> currentColumns, List<SpannerColumn> existingColumns){
		Map<String,SpannerColumn> columnMap = Scanner.of(existingColumns)
				.toMapSupplied(SpannerColumn::getName, LinkedHashMap::new);
		return Scanner.of(currentColumns)
				.include(col -> columnMap.containsKey(col.getName()))
				.exclude(col -> columnMap.get(col.getName()).getType() == col.getType())
				.list();
	}

}
