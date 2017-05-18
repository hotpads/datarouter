package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn.SqlColumnByName;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.util.core.stream.StreamTool;

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
		return StreamTool.map(differentColumns, SqlColumnByName::getSqlColumn);
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

	/********************* helper methods *******************************/

	public boolean isTableModified(){
		if(isPrimaryKeyModified()){
			return true;
		}
		if(areColumnsModified()){
			return true;
		}
		if(isIndexesModified()){
			return true;
		}
		if(isEngineModified()){
			return true;
		}
		if(isCharacterSetModified()){
			return true;
		}
		if(isCollationModified()){
			return true;
		}
		return false;
	}

	private boolean areColumnsModified(){
		return !new HashSet<>(current.getColumns()).equals(new HashSet<>(requested.getColumns()));
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

	public boolean isIndexesModified(){
		return !current.getIndexes().equals(requested.getIndexes());
	}

	public boolean isUniqueIndexesModified(){
		return !current.getUniqueIndexes().equals(requested.getUniqueIndexes());
	}

	public boolean isPrimaryKeyModified(){
		return !current.getPrimaryKey().equals(requested.getPrimaryKey());
	}

}
