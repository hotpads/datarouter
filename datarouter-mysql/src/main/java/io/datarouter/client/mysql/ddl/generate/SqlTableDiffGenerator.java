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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.ddl.domain.SqlColumn.SqlColumnByName;
import io.datarouter.client.mysql.ddl.domain.SqlIndex;
import io.datarouter.client.mysql.ddl.domain.SqlTable;
import io.datarouter.util.iterable.IterableTool;

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
		return IterableTool.map(differentColumns, SqlColumnByName::getSqlColumn);
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
