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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.datarouter.bytes.KvString;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.ddl.domain.SqlColumn.SqlColumnByName;
import io.datarouter.client.mysql.ddl.domain.SqlIndex;
import io.datarouter.client.mysql.ddl.domain.SqlTable;
import io.datarouter.scanner.Scanner;

public class SqlTableDiffGenerator{

	private final SqlTable current;
	private final SqlTable requested;

	public SqlTableDiffGenerator(SqlTable current, SqlTable requested){
		this.current = current;
		this.requested = requested;
	}

	public List<SqlColumn> getColumnsToAdd(){
		return findDifferentColumnsByName(requested, current);
	}

	public List<SqlColumn> getColumnsToRemove(){
		return findDifferentColumnsByName(current, requested);
	}

	private static List<SqlColumn> findDifferentColumnsByName(SqlTable first, SqlTable second){
		Set<SqlColumnByName> differentColumns = SqlColumnByName.wrap(first.getColumns());
		differentColumns.removeAll(SqlColumnByName.wrap(second.getColumns()));
		return Scanner.of(differentColumns)
				.map(SqlColumnByName::sqlColumn)
				.list();
	}

	private static Map<SqlColumn,SqlColumn> findSameColumnsByName(List<SqlColumn> columns1, List<SqlColumn> columns2){
		Map<String,SqlColumn> nameToColumns2 = Scanner.of(columns2)
				.toMapSupplied(SqlColumn::getName, LinkedHashMap::new);
		Map<SqlColumn,SqlColumn> intersectionMap = Scanner.of(columns1)
				.toMap(col1 -> col1, col1 -> nameToColumns2.get(col1.getName()));
		intersectionMap.values().removeIf(Objects::isNull);
		return intersectionMap;
	}

	public List<SqlColumn> getColumnsToModify(){
		Set<SqlColumn> modifiedColumns = new HashSet<>(requested.getColumns());// start with all requested columns
		modifiedColumns.removeAll(current.getColumns());// remove current columns that don't need changes
		modifiedColumns.removeAll(getColumnsToAdd());// remove new columns
		return new ArrayList<>(modifiedColumns);
	}

	public Set<SqlIndex> getIndexesToAdd(){
		return findDifferentIndexes(requested.getIndexes(), current.getIndexes());
	}

	public Set<SqlIndex> getIndexesToRemove(){
		return findDifferentIndexes(current.getIndexes(), requested.getIndexes());
	}

	public Set<SqlIndex> getUniqueIndexesToAdd(){
		return findDifferentIndexes(requested.getUniqueIndexes(), current.getUniqueIndexes());
	}

	public Set<SqlIndex> getUniqueIndexesToRemove(){
		return findDifferentIndexes(current.getUniqueIndexes(), requested.getUniqueIndexes());
	}

	private static Set<SqlIndex> findDifferentIndexes(Set<SqlIndex> first, Set<SqlIndex> second){
		Set<SqlIndex> differentIndexes = new HashSet<>(first);
		differentIndexes.removeAll(second);
		return differentIndexes;
	}

	/*------------------------------ helper ---------------------------------*/

	public boolean isTableModified(){
		return isPrimaryKeyModified()
				|| areColumnsModified()
				|| isIndexesModified()
				|| isUniqueIndexesModified()
				|| isEngineModified()
				|| isCharacterSetModified()
				|| isCollationModified()
				|| isRowFormatModified();
	}

	public boolean isPrimaryKeyModified(){
		return !current.hasPrimaryKey() || !current.getPrimaryKey().equals(requested.getPrimaryKey());
	}

	private boolean areColumnsModified(){
		return !new HashSet<>(current.getColumns()).equals(new HashSet<>(requested.getColumns()));
	}

	public void throwIfColumnTypesModified(){
		Map<SqlColumn,SqlColumn> columnsToCompareTypes = findSameColumnsByName(
				current.getColumns(),
				requested.getColumns());
		columnsToCompareTypes.forEach((currCol, newCol) -> {
			if(!currCol.getType().equals(newCol.getType())){
				throw new RuntimeException("Do not change the type of a MySQL column, instead add a new column and "
						+ "migrate the data. "
						+ new KvString()
						.add("TableName", current.getName())
						.add("ColumnName", currCol.getName())
						.add("CurrentColumnType", currCol.getType().toString())
						.add("NewColumnType", newCol.getType().toString()));
			}
		});
	}

	public boolean isIndexesModified(){
		return !current.getIndexes().equals(requested.getIndexes());
	}

	public boolean isUniqueIndexesModified(){
		return !current.getUniqueIndexes().equals(requested.getUniqueIndexes());
	}

	public boolean isEngineModified(){
		return current.getEngine() != requested.getEngine();
	}

	public boolean isCharacterSetModified(){
		return current.getCharacterSet() != requested.getCharacterSet();
	}

	public boolean isCollationModified(){
		return current.getCollation() != requested.getCollation();
	}

	public boolean isRowFormatModified(){
		return current.getRowFormat() != requested.getRowFormat();
	}

}
