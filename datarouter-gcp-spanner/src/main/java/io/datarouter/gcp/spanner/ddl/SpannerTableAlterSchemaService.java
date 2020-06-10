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
package io.datarouter.gcp.spanner.ddl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.cloud.spanner.ResultSet;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;

@Singleton
public class SpannerTableAlterSchemaService{

	@Inject
	private SpannerTableOperationsGenerator tableOperationsGenerator;
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
			throw new RuntimeException("Cannot modify primary key columns in spanner");
		}
		List<SpannerColumn> columns = extractColumns(columnResult);
		List<SpannerColumn> colToAdd = columnNameDifferences(currentColumns, columns);
		List<SpannerColumn> colToRemove = columnNameDifferences(columns, currentColumns);
		List<SpannerColumn> colToAlter = colmumnsToAlter(currentColumns, columns);
		if(!colToAdd.isEmpty()){
			colToAdd.forEach(col -> statements.updateFunction(
					tableOperationsGenerator.addColumns(tableName, col),
					updateOptions::getAddColumns,
					true));
		}
		if(!colToRemove.isEmpty()){
			colToRemove.forEach(col -> statements.updateFunction(
					tableOperationsGenerator.dropColumns(tableName, col),
					updateOptions::getDeleteColumns,
					false));
		}
		if(!colToAlter.isEmpty()){
			colToAlter.forEach(col -> statements.updateFunction(
					tableOperationsGenerator.alterColumns(tableName, col),
					updateOptions::getModifyColumns,
					false));
		}
	}

	public Set<String> getIndexes(ResultSet rs){
		Set<String> result = new HashSet<>();
		while(rs.next()){
			result.add(rs.getString("INDEX_NAME"));
		}
		result.remove("PRIMARY_KEY");
		return result;
	}

	public boolean indexEqual(SpannerIndex index, ResultSet rs){
		List<String> indexKeyColumns = index.getKeyFields().stream()
				.map(Field::getKey)
				.map(FieldKey::getColumnName)
				.collect(Collectors.toList());
		List<String> nonKeyColumns = index.getNonKeyFields().stream()
				.map(Field::getKey)
				.map(FieldKey::getColumnName)
				.collect(Collectors.toList());
		boolean runOnce = false;
		while(rs.next()){
			runOnce = true;
			String columnName = rs.getString("COLUMN_NAME");
			if(rs.isNull("ORDINAL_POSITION")){
				if(!nonKeyColumns.contains(columnName)){
					return false;
				}
			}else{
				if(!indexKeyColumns.contains(columnName)){
					return false;
				}
			}
		}
		return runOnce;
	}

	private List<SpannerColumn> extractColumns(ResultSet rs){
		List<SpannerColumn> columns = new ArrayList<>();
		while(rs.next()){
			String columnName = rs.getString("COLUMN_NAME");
			boolean isNullable = rs.getString("IS_NULLABLE").equalsIgnoreCase("YES");
			SpannerColumnType type = SpannerColumnType.fromSchemaString(rs.getString("SPANNER_TYPE"));
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

	private List<SpannerColumn> colmumnsToAlter(
			List<SpannerColumn> currentColumns,
			List<SpannerColumn> existingColumns){
		Map<String,SpannerColumn> columnMap = Scanner.of(existingColumns)
				.toMapSupplied(SpannerColumn::getName, LinkedHashMap::new);
		return currentColumns.stream()
				.filter(col -> columnMap.containsKey(col.getName()))
				.filter(col -> !columnMap.get(col.getName()).getType().equals(col.getType()))
				.collect(Collectors.toList());
	}

}
