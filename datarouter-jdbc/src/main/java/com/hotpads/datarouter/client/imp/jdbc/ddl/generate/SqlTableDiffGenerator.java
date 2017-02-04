package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlTableEngine;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn.SqlColumnNameComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn.SqlColumnNameTypeLengthAutoIncrementDefaultComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex.SqlIndexNameComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.test.SqlTableMocks;
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
		modifiedColumns.removeAll(current.getColumns());// remove current columns that don't need changes
		modifiedColumns.removeAll(getColumnsToAdd());// remove new columns
		return new ArrayList<>(modifiedColumns);
	}

	public List<SqlColumn> getColumnsWithCharsetOrCollationToConvert(){
		Map<String,SqlColumn> requestedColumnsByName = requested.getColumns().stream()
				.collect(Collectors.toMap(SqlColumn::getName, Function.identity()));
		List<SqlColumn> columnsWithCharsetOrCollationToConvert = new ArrayList<>();
		for(SqlColumn column : current.getColumns()){
			SqlColumn requestedColum = requestedColumnsByName.get(column.getName());
			if(requestedColum == null
					|| Objects.equals(column.getCharacterSet(), requestedColum.getCharacterSet())
							&& Objects.equals(column.getCollation(), requestedColum.getCollation())){
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
		SortedSet<SqlColumn> currentColumns = new TreeSet<>(current.getColumns());
		SortedSet<SqlColumn> requestedColumns = new TreeSet<>(requested.getColumns());
		return !currentColumns.equals(requestedColumns);
	}

	public boolean isEngineModified(){
		MySqlTableEngine currentEngine = MySqlTableEngine.valueOf(current.getEngine().toString());
		MySqlTableEngine requestedEngine = MySqlTableEngine.valueOf(requested.getEngine().toString());
		return currentEngine != requestedEngine;
	}

	public boolean isCharacterSetModified(){
		MySqlCharacterSet currentCharacterSet = MySqlCharacterSet.valueOf(current.getCharacterSet().toString());
		MySqlCharacterSet requestedCharacterSet = MySqlCharacterSet.valueOf(requested.getCharacterSet().toString());
		return currentCharacterSet != requestedCharacterSet;
	}

	public boolean isCollationModified(){
		MySqlCollation currentCollation = MySqlCollation.valueOf(current.getCollation().toString());
		MySqlCollation requestedCollation = MySqlCollation.valueOf(requested.getCollation().toString());
		return currentCollation != requestedCollation;
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

	public static class SqlTableDiffGeneratorTests{

		@Test
		public void testGetColumnsToAdd(){
			Assert.assertEquals(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.getColumnsToAdd(),
					Arrays.asList(SqlTableMocks.CHARRETTE));
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.getColumnsToAdd(),
					Arrays.asList(SqlTableMocks.SAFARI));
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.getColumnsToAdd(), Collections.emptyList());
		}

		@Test
		public void testGetColumnsToRemove(){
			Assert.assertEquals(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.getColumnsToRemove(),
					Arrays.asList(SqlTableMocks.SAFARI));
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.getColumnsToRemove(),
					Arrays.asList(SqlTableMocks.CHARRETTE));
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.getColumnsToRemove(), Collections.emptyList());
		}

		@Test
		public void testGetColumnsToModify(){
			Assert.assertEquals(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.getColumnsToModify(),
					Arrays.asList(SqlTableMocks.KWILU));
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.getColumnsToModify(),
					Arrays.asList(SqlTableMocks.KWILU_TEXT));
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.getColumnsToModify(), Collections.emptyList());
		}

		@Test
		public void testGetColumnsWithCharsetOrCollationToConvert(){
			Assert.assertEquals(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.getColumnsWithCharsetOrCollationToConvert(),
					Arrays.asList(SqlTableMocks.SAVANNA));
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.getColumnsWithCharsetOrCollationToConvert(),
					Arrays.asList(SqlTableMocks.SAVANNA_BIN));
			Assert.assertEquals(new SqlTableDiffGenerator(SqlTableMocks.ABHAINN_DEARG, SqlTableMocks.ABERFELDY)
					.getColumnsWithCharsetOrCollationToConvert(), Arrays.asList(SqlTableMocks.SAVANNA_LATIN));
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.getColumnsWithCharsetOrCollationToConvert(),
					Collections.emptyList());
		}

		@Test
		public void testGetIndexesToAdd(){
			Assert.assertEquals(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.getIndexesToAdd(), Collections.emptyList());
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.getIndexesToAdd(),
					Arrays.asList(SqlTableMocks.BOMBORA));
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.getIndexesToAdd(), Collections.emptyList());
		}

		@Test
		public void testGetIndexesToRemove(){
			Assert.assertEquals(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.getIndexesToRemove(),
					Arrays.asList(SqlTableMocks.BOMBORA));
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.getIndexesToRemove(), Collections.emptyList());
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.getIndexesToRemove(), Collections.emptyList());
		}

		@Test
		public void testGetUniqueIndexesToAdd(){
			Assert.assertEquals(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.getUniqueIndexesToAdd(),
					Arrays.asList(SqlTableMocks.COORANBONG));
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.getUniqueIndexesToAdd(), Collections.emptyList());
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.getUniqueIndexesToAdd(), Collections.emptyList());
		}

		@Test
		public void testGetUniqueIndexesToRemove(){
			Assert.assertEquals(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.getUniqueIndexesToRemove(),
					Collections.emptyList());
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.getUniqueIndexesToRemove(),
					Arrays.asList(SqlTableMocks.COORANBONG));
			Assert.assertEquals(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.getUniqueIndexesToRemove(),
					Collections.emptyList());
		}

		@Test
		public void testIsTableModified(){
			Assert.assertTrue(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.isTableModified());
			Assert.assertTrue(new SqlTableDiffGenerator(SqlTableMocks.ALISA_BAY, SqlTableMocks.ABERFELDY)
					.isTableModified());
			Assert.assertTrue(new SqlTableDiffGenerator(SqlTableMocks.ALLT_A_BHAINNE, SqlTableMocks.ABERFELDY)
					.isTableModified());
			Assert.assertTrue(new SqlTableDiffGenerator(SqlTableMocks.ANNANDALE, SqlTableMocks.ABERFELDY)
					.isTableModified());
			Assert.assertTrue(new SqlTableDiffGenerator(SqlTableMocks.ARBIKIE, SqlTableMocks.ABERFELDY)
					.isTableModified());
			Assert.assertTrue(new SqlTableDiffGenerator(SqlTableMocks.ARDBEG, SqlTableMocks.ABERFELDY)
					.isTableModified());
			Assert.assertFalse(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.isTableModified());
		}

		@Test
		public void testAreColumnsModified(){
			Assert.assertTrue(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.areColumnsModified());
			Assert.assertTrue(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.areColumnsModified());
			Assert.assertFalse(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.areColumnsModified());
		}

		@Test
		public void testIsEngineModified(){
			Assert.assertTrue(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.isEngineModified());
			Assert.assertTrue(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.isEngineModified());
			Assert.assertFalse(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.isEngineModified());
		}

		@Test
		public void testIsCharacterSetModified(){
			Assert.assertTrue(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.isCharacterSetModified());
			Assert.assertTrue(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.isCharacterSetModified());
			Assert.assertFalse(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.isCharacterSetModified());
		}

		@Test
		public void testIsCollationModified(){
			Assert.assertTrue(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.isCollationModified());
			Assert.assertTrue(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.isCollationModified());
			Assert.assertFalse(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.isCollationModified());
		}

		@Test
		public void testIsRowFormatModified(){
			Assert.assertTrue(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.isRowFormatModified());
			Assert.assertTrue(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.isRowFormatModified());
			Assert.assertFalse(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.isRowFormatModified());
		}

		@Test
		public void testIsIndexesModified(){
			Assert.assertTrue(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.isIndexesModified());
			Assert.assertTrue(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.isIndexesModified());
			Assert.assertFalse(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.isIndexesModified());
		}

		@Test
		public void testIsUniqueIndexesModified(){
			Assert.assertTrue(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.isUniqueIndexesModified());
			Assert.assertTrue(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.isUniqueIndexesModified());
			Assert.assertFalse(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.isUniqueIndexesModified());
		}

		@Test
		public void testIsPrimaryKeyModified(){
			Assert.assertTrue(SqlTableMocks.DIFF_ABERFELDY_ABERLOUR.isPrimaryKeyModified());
			Assert.assertTrue(SqlTableMocks.DIFF_ABERLOUR_ABERFELDY.isPrimaryKeyModified());
			Assert.assertFalse(SqlTableMocks.DIFF_ABERLOUR_ABERLOUR.isPrimaryKeyModified());
		}

	}

}
