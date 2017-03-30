package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn.SqlColumnNameComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn.SqlColumnNameTypeLengthAutoIncrementDefaultComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex.SqlIndexNameComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class SqlTableDiffGenerator{

	private final SqlTable current;
	private final SqlTable requested;

	public SqlTableDiffGenerator(SqlTable current, SqlTable requested){
		this.current = current;
		this.requested = requested;
	}

	public List<SqlColumn> getColumnsToAdd(){
		return minusColumns(requested, current);
	}

	public List<SqlColumn> getColumnsToRemove(){
		return minusColumns(current,requested);
	}

	private static List<SqlColumn> minusColumns(SqlTable tableA, SqlTable tableB){
		SqlColumnNameComparator comparator = new SqlColumnNameComparator(true);
		Set<SqlColumn> tableAColumns = new TreeSet<>(comparator);
		Set<SqlColumn> tableBColumns = new TreeSet<>(comparator);
		tableAColumns.addAll(tableA.getColumns());
		tableBColumns.addAll(tableB.getColumns());
		return new ArrayList<>(DrCollectionTool.minus(tableAColumns, tableBColumns, comparator));
	}

	public List<SqlColumn> getColumnsToModify(){
		Set<SqlColumn> modifiedColumns = new TreeSet<>(new SqlColumnNameTypeLengthAutoIncrementDefaultComparator());
		modifiedColumns.addAll(requested.getColumns());// start with all requested columns
		current.getColumns().forEach(modifiedColumns::remove);// remove current columns that don't need changes
		modifiedColumns.removeAll(getColumnsToAdd());// remove new columns
		return new ArrayList<>(modifiedColumns);
	}

	public List<SqlColumn> getColumnsWithCharsetOrCollationToConvert(){
		Map<String,SqlColumn> requestedColumnsByName = requested.getColumns().stream()
				.collect(Collectors.toMap(SqlColumn::getName, Function.identity()));
		List<SqlColumn> columnsWithCharsetOrCollationToConvert = new ArrayList<>();
		for(SqlColumn column : current.getColumns()){
			SqlColumn requestedColumn = requestedColumnsByName.get(column.getName());
			if(requestedColumn == null
					|| column.getCharacterSet() == null
					|| column.getCollation() == null
					|| Objects.equals(column.getCharacterSet(), requestedColumn.getCharacterSet())
							&& Objects.equals(column.getCollation(), requestedColumn.getCollation())){
				continue;
			}
			columnsWithCharsetOrCollationToConvert.add(column);
		}
		return columnsWithCharsetOrCollationToConvert;
	}

	public SortedSet<SqlIndex> getIndexesToAdd(){
		return minusIndexes(requested, current);
	}

	public SortedSet<SqlIndex> getIndexesToRemove(){
		return minusIndexes(current, requested);
	}

	public SortedSet<SqlIndex> getUniqueIndexesToAdd(){
		return minusUniqueIndexes(requested, current);
	}

	public SortedSet<SqlIndex> getUniqueIndexesToRemove(){
		return minusUniqueIndexes(current, requested);
	}

	/**
	 * returns tableA.indexes - tableB.indexes
	 */
	private static SortedSet<SqlIndex> minusIndexes(SqlTable tableA, SqlTable tableB){
		Set<SqlIndex> tableAIndexes = tableA.getIndexes();
		Set<SqlIndex> tableBIndexes = tableB.getIndexes();
		TreeSet<SqlIndex> indexesToRemove = DrCollectionTool.minus(tableAIndexes, tableBIndexes,
				new SqlIndexNameComparator());
		return new TreeSet<>(indexesToRemove);
	}

	private static SortedSet<SqlIndex> minusUniqueIndexes(SqlTable tableA, SqlTable tableB){
		Set<SqlIndex> tableAUniqueIndexes = tableA.getUniqueIndexes();
		Set<SqlIndex> tableBUniqueIndexes = tableB.getUniqueIndexes();
		TreeSet<SqlIndex> uniqueIndexesToRemove = DrCollectionTool.minus(tableAUniqueIndexes, tableBUniqueIndexes,
				new SqlIndexNameComparator());
		return new TreeSet<>(uniqueIndexesToRemove);
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
		SortedSet<SqlColumn> currentColumns = new TreeSet<>(
				new SqlColumnNameTypeLengthAutoIncrementDefaultComparator());
		currentColumns.addAll(current.getColumns());
		SortedSet<SqlColumn> requestedColumns = new TreeSet<>(
				new SqlColumnNameTypeLengthAutoIncrementDefaultComparator());
		requestedColumns.addAll(requested.getColumns());
		return !currentColumns.equals(requestedColumns);
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
		SortedSet<SqlIndex> currentIndexes = new TreeSet<>(current.getIndexes());
		SortedSet<SqlIndex> requestedIndexes = new TreeSet<>(requested.getIndexes());
		return !currentIndexes.equals(requestedIndexes);
	}

	public boolean isUniqueIndexesModified(){
		SortedSet<SqlIndex> currentUniqueIndexes = new TreeSet<>(current.getUniqueIndexes());
		SortedSet<SqlIndex> requestedUniqueIndexes = new TreeSet<>(requested.getUniqueIndexes());
		return !currentUniqueIndexes.equals(requestedUniqueIndexes);
	}

	public boolean isPrimaryKeyModified(){
		List<SqlColumn> currentPrimaryKeyColumns = current.getPrimaryKey().getColumns();
		List<SqlColumn> requestedPrimaryKeyColumns = requested.getPrimaryKey().getColumns();
		if(!haveTheSameColumnsinTheSameOrder(currentPrimaryKeyColumns, requestedPrimaryKeyColumns)){
			return true;
		}
		return false;
	}

	private boolean haveTheSameColumnsinTheSameOrder(List<SqlColumn> currentPrimaryKeyColumns,
			List<SqlColumn> requestedPrimaryKeyColumns){
		return currentPrimaryKeyColumns.equals(requestedPrimaryKeyColumns);
	}

}
