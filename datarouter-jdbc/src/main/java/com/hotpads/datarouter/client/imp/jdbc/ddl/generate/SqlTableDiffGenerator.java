package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlTableEngine;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn.SqlColumnNameComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn.SqlColumnNameTypeComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn.SqlColumnNameTypeLengthAutoIncrementDefaultComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex.SqlIndexNameComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;

public class SqlTableDiffGenerator{
	private static final Logger logger = LoggerFactory.getLogger(SqlTableDiffGenerator.class);

	protected SqlTable current, requested;
	protected boolean enforceColumnOrder = false;

	/******************* constructors ****************************/

	public SqlTableDiffGenerator(SqlTable current, SqlTable requested, boolean enforceColumnOrder){
		this.current = current;
		this.requested = requested;
		this.enforceColumnOrder = enforceColumnOrder;
	}

	/****************** primary method ****************************/

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
		if(tableA==null || tableB==null){
			return new ArrayList<>();
		}
		tableAColumns.addAll(tableA.getColumns());
		tableBColumns.addAll(tableB.getColumns());
		return DrListTool.createArrayList(DrCollectionTool.minus(tableAColumns, tableBColumns, comparator));
	}

	public List<SqlColumn> getColumnsToModify(){
		SqlColumnNameTypeLengthAutoIncrementDefaultComparator comparator =
				new SqlColumnNameTypeLengthAutoIncrementDefaultComparator(true);
		Set<SqlColumn> requestedColumns = new TreeSet<>(comparator);
		Set<SqlColumn> currentColumns = new TreeSet<>(comparator);
		if(requested==null || current==null){
			return new ArrayList<>();
		}
		requestedColumns.addAll(requested.getColumns());
		currentColumns.addAll(current.getColumns());
		//TODO too much on one line.  extract the sets into their own variables
		return getColumnsToModifyAfterAddingColumns(requestedColumns,currentColumns,getColumnsToAdd(),comparator);
	}

	private List<SqlColumn> getColumnsToModifyAfterAddingColumns(Set<SqlColumn> requestedColumns,
			Set<SqlColumn> currentColumns, List<SqlColumn> columnsToAddUsingNameComparator,
			SqlColumnNameTypeLengthAutoIncrementDefaultComparator comparator){
		// by getting all the modified columns (the ones we should add) and removing from them the ones
		// we have already added (columnsToAdd)
		List<SqlColumn> listOfColumnsToAddUsingNameTypeComparator = DrListTool.createArrayList(DrCollectionTool.minus(
				requestedColumns, currentColumns, comparator));
		Set<SqlColumn> columnsToModify = DrCollectionTool.minus(listOfColumnsToAddUsingNameTypeComparator,
				columnsToAddUsingNameComparator);
		return DrListTool.createArrayList(columnsToModify);
	}

	public List<SqlColumn> getColumnsWithCharsetOrCollationToConvert(){
		Map<String,SqlColumn> requestedColumnsByName = requested.getColumns().stream()
				.collect(Collectors.toMap(SqlColumn::getName, Function.identity()));
		List<SqlColumn> columnsWithCharsetOrCollationToConvert = new ArrayList<>();
		for(SqlColumn column : current.getColumns()){
			SqlColumn requestedColum = requestedColumnsByName.get(column.getName());
			if(requestedColum == null
					|| column.getCharacterSet() == null
					|| column.getCollation() == null
					|| column.getCharacterSet().equals(requestedColum.getCharacterSet())
							&& column.getCollation().equals(requestedColum.getCollation())){
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
		if(tableA == null || tableB == null){
			return new TreeSet<>();
		}
		SortedSet<SqlIndex> tableAIndexes = tableA.getIndexes();
		SortedSet<SqlIndex> tableBIndexes = tableB.getIndexes();
		TreeSet<SqlIndex> indexesToRemove = DrCollectionTool.minus(tableAIndexes, tableBIndexes,
				new SqlIndexNameComparator());
		return new TreeSet<>(indexesToRemove);
	}

	private static SortedSet<SqlIndex> minusUniqueIndexes(SqlTable tableA, SqlTable tableB){
		if(tableA == null || tableB == null){
			return new TreeSet<>();
		}
		SortedSet<SqlIndex> tableAUniqueIndexes = tableA.getUniqueIndexes();
		SortedSet<SqlIndex> tableBUniqueIndexes = tableB.getUniqueIndexes();
		TreeSet<SqlIndex> uniqueIndexesToRemove = DrCollectionTool.minus(tableAUniqueIndexes, tableBUniqueIndexes,
				new SqlIndexNameComparator());
		return new TreeSet<>(uniqueIndexesToRemove);
	}

	/********************* helper methods *******************************/

	public boolean shouldReorderColumns(){
		return enforceColumnOrder && ! isColumnOrderCorrect();
	}

	public boolean isColumnOrderCorrect(){
		return true;//TODO implement
	}

	public boolean isTableModified(){
		if (isPrimaryKeyModified()){
			return true;
		}
		// TODO too much on one line. extract the sets into their own variables
		SortedSet<SqlColumn> currentColumns = new TreeSet<>(current.getColumns());
		SortedSet<SqlColumn> requestedColumns = new TreeSet<>(requested.getColumns());
		if (!currentColumns.equals(requestedColumns)){
			return true;
		}
		if (isIndexesModified()){
			return true;
		}
		if (isEngineModified()){
			return true;
		}
		if (isCharacterSetModified()){
			return true;
		}
		if (isCollationModified()){
			return true;
		}
		return false;
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

	public boolean isIndexesModified(){
		SortedSet<SqlIndex> currentIndexes = new TreeSet<>(current.getIndexes());
		SortedSet<SqlIndex> requestedIndexes = new TreeSet<>(requested.getIndexes());
		return !currentIndexes.equals(requestedIndexes);
	}

	public boolean isUniqueIndexesModified(){
		SortedSet<SqlIndex> currentUniqueIndexes = new TreeSet<>(current.getUniqueIndexes());
		SortedSet<SqlIndex> requestedUniqueIndexes = new TreeSet<>(requested.getUniqueIndexes());
		return !DrSetTool.containsSameKeys(currentUniqueIndexes, requestedUniqueIndexes);
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

	public SqlTable getRequested(){
		return requested;
	}

	public SqlTable getCurrent(){
		return current;
	}


	/********************* Tests *******************************/

	public static class SqlTableDiffGeneratorTester{
		private final SqlColumn idCol = new SqlColumn("id", MySqlColumnType.BIGINT);
		private final SqlIndex primaryKey1 = new SqlIndex("pk1").addColumn(idCol);

		@Test
		public void testCollation(){
			SqlTable tableWithUtf8BinCollation = new SqlTable("A");
			tableWithUtf8BinCollation.setCollation(MySqlCollation.utf8_bin);
			SqlTable tableWithUtf8GeneralCiCollation = new SqlTable("B");
			tableWithUtf8GeneralCiCollation.setCollation(MySqlCollation.utf8_general_ci);
			SqlTableDiffGenerator generator = new SqlTableDiffGenerator(tableWithUtf8BinCollation,
					tableWithUtf8GeneralCiCollation, true);
			Assert.assertTrue(generator.isCollationModified());
			Assert.assertFalse(generator.isCharacterSetModified());
		}

		@Test
		public void testAutoIncrement(){

			SqlColumn idCol1 = new SqlColumn("id", MySqlColumnType.BIGINT, 8, false, true);
			SqlIndex primaryKey1 = new SqlIndex("pk1").addColumn(idCol1);
			SqlColumn idCol2 = new SqlColumn("id", MySqlColumnType.BIGINT, 8, true, false);
			SqlIndex primaryKey2 = new SqlIndex("pk1").addColumn(idCol2);
			List<SqlColumn> listA = DrListTool.createArrayList(idCol1);
			List<SqlColumn> listA2 = DrListTool.createArrayList(idCol2);
			SqlTable tableA = new SqlTable("A", listA, primaryKey1);
			SqlTable tableA2 = new SqlTable("A", listA2,primaryKey2);

			SqlTableDiffGenerator diffAWithA = new SqlTableDiffGenerator(tableA, tableA, true),
					diffAA2 = new SqlTableDiffGenerator(tableA, tableA2, true);
			Assert.assertFalse(diffAWithA.isTableModified());
			Assert.assertTrue(diffAA2.isTableModified());
			// TABLES WITH DIFFERENT NUMBER OF COLUMNS
			SqlColumn col1 = new SqlColumn("col1", MySqlColumnType.BIGINT);
			SqlColumn col2 = new SqlColumn("col2", MySqlColumnType.BINARY);
			SqlColumn col3 = new SqlColumn("col3", MySqlColumnType.BIT);
			tableA.addColumn(col1);
			tableA2.addColumn(col1);
			tableA2.addColumn(col2);
			Assert.assertTrue(diffAA2.isTableModified());
			// TABLES WITH THE SAME NUMBER OF COLUMNS, BUT 1 OR MORE DIFFERENT COLUMN
			tableA.addColumn(col3);
			Assert.assertTrue(diffAA2.isTableModified());
		}


		@Test
		public void isTableModifiedTest(){
			//TODO don't reuse declaration types anywhere
			List<SqlColumn> listA = new ArrayList<>();
			List<SqlColumn> listA2 = new ArrayList<>();
			//listB = ListTool.createArrayList();
			SqlTable tableA = new SqlTable("A", listA, primaryKey1);
			// SqlTable tableB = new SqlTable("B", listB, primaryKey1);
			SqlTable tableA2 = new SqlTable("A", listA2,primaryKey1);

			SqlTableDiffGenerator diffAWithA = new SqlTableDiffGenerator(tableA, tableA, true),
					diffAA2 = new SqlTableDiffGenerator(tableA, tableA2, true);
			//diffAB = new SqlTableDiffGenerator(tableA, tableB, true);
			Assert.assertFalse(diffAWithA.isTableModified());
			// Assert.assertTrue(diffAB.isTableModified());// WE CAN'T HAVE DIFFERENT TABLES WITH THE SAME NAME
			// TABLES WITH DIFFERENT NUMBER OF COLUMNS
			SqlColumn col1 = new SqlColumn("col1", MySqlColumnType.BIGINT);
			SqlColumn col2 = new SqlColumn("col2", MySqlColumnType.BINARY);
			SqlColumn col3 = new SqlColumn("col3", MySqlColumnType.BIT);
			tableA.addColumn(col1);
			tableA2.addColumn(col1);
			tableA2.addColumn(col2);
			Assert.assertTrue(diffAA2.isTableModified());
			// TABLES WITH THE SAME NUMBER OF COLUMNS, BUT 1 OR MORE DIFFERENT COLUMN
			tableA.addColumn(col3);
			Assert.assertTrue(diffAA2.isTableModified());
		}

		@Test
		public void isPrimaryKeyModifiedTest(){
			List<SqlColumn> list1 = new ArrayList<>();
			List<SqlColumn> list2 = new ArrayList<>();
			SqlColumn idCol = new SqlColumn("id", MySqlColumnType.BIGINT);
			SqlColumn col = new SqlColumn("id", MySqlColumnType.BIGINT);

			SqlIndex primaryKey1 = new SqlIndex("pk1").addColumn(idCol);
			SqlIndex primaryKey2 = new SqlIndex("pk2").addColumn(idCol).addColumn(col);

			SqlTable tableA = new SqlTable("Table 1", list1, primaryKey1 );
			SqlTable tableB = new SqlTable("Table 2", list2, primaryKey2 );
			SqlTable tableA2 = new SqlTable("Table 1", list1, primaryKey2);
			SqlTable tableA0 = new SqlTable("Table 1"); // without pKey

			SqlTableDiffGenerator diffAWithA = new SqlTableDiffGenerator(tableA, tableA, true);
			SqlTableDiffGenerator diffAWithB = new SqlTableDiffGenerator(tableA, tableB, true);
			//TODO need spaces after commas everywhere
			SqlTableDiffGenerator diffAA2 = new SqlTableDiffGenerator(tableA, tableA2, true);
			SqlTableDiffGenerator diffAA0 = new SqlTableDiffGenerator(tableA, tableA0, true);

			Assert.assertFalse(diffAWithA.isPrimaryKeyModified());
			Assert.assertTrue(diffAWithB.isPrimaryKeyModified());
			Assert.assertTrue(diffAA2.isPrimaryKeyModified());
			Assert.assertTrue(diffAA0.isPrimaryKeyModified());
		}

		@Test
		public void getColumnsToAddTest(){
			SqlColumn colA = new SqlColumn("A", MySqlColumnType.BIGINT, 250, true, false);
			SqlColumn colB = new SqlColumn("B", MySqlColumnType.BINARY);
			SqlColumn colC = new SqlColumn("C", MySqlColumnType.BOOLEAN);
			SqlColumn colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
			List<SqlColumn>
			listBAndC = new ArrayList<>(),
			listM = new ArrayList<>();

			listBAndC.add(colB);
			listBAndC.add(colC);
			listM.add(colM);
			SqlTable
			table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC),
			table2 = new SqlTable("TB").addColumn(colA).addColumn(colM);

			SqlTableDiffGenerator diffBWithA = new SqlTableDiffGenerator(table2, table1, true);
			SqlTableDiffGenerator diffAWithB = new SqlTableDiffGenerator(table1, table2, true);
			SqlTableDiffGenerator diffNullNull = new SqlTableDiffGenerator(null, null, true);
			SqlTableDiffGenerator diffANull = new SqlTableDiffGenerator(table1, null, true);
			SqlTableDiffGenerator diffNullA = new SqlTableDiffGenerator(null, table1, true);

			Assert.assertTrue(DrCollectionTool.isEmpty(diffNullNull.getColumnsToAdd()));
			Assert.assertTrue(DrCollectionTool.isEmpty(diffNullA.getColumnsToAdd()));
			Assert.assertTrue(DrCollectionTool.isEmpty(diffANull.getColumnsToAdd()));
			Assert.assertTrue(DrCollectionTool.isEmpty(DrCollectionTool.minus(diffBWithA.getColumnsToAdd(),
					listBAndC)));
			Assert.assertTrue(DrCollectionTool.isEmpty(DrCollectionTool.minus(diffAWithB.getColumnsToAdd(), listM)));

			table1.addColumn(null);
			diffBWithA = new SqlTableDiffGenerator(table2, table1, true);
			diffAWithB = new SqlTableDiffGenerator(table1, table2, true);
			Assert.assertTrue(DrCollectionTool.isEmpty(DrCollectionTool.minus(diffBWithA.getColumnsToAdd(),
					listBAndC)));
			Assert.assertTrue(DrCollectionTool.isEmpty(DrCollectionTool.minus(diffAWithB.getColumnsToAdd(), listM)));

			SqlColumn colA2 = new SqlColumn("A", MySqlColumnType.VARCHAR,200,true, false);
			table1.addColumn(colA2);
			diffBWithA = new SqlTableDiffGenerator(table2, table1, true);
			Assert.assertTrue(DrCollectionTool.isEmpty(DrCollectionTool.minus(diffBWithA.getColumnsToAdd(),
					listBAndC)));
		}

		@Test
		public void getColumnsToRemoveTest(){
			SqlColumn colA = new SqlColumn("A", MySqlColumnType.BIGINT);
			SqlColumn colB = new SqlColumn("B", MySqlColumnType.BINARY);
			SqlColumn colC = new SqlColumn("C", MySqlColumnType.BOOLEAN);
			SqlColumn colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
			List<SqlColumn> listBAndC = new ArrayList<>();
			List<SqlColumn> listM = new ArrayList<>();

			listBAndC.add(colB);
			listBAndC.add(colC);
			listM.add(colM);
			SqlTable table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC);
			SqlTable table2 = new SqlTable("TB").addColumn(colA).addColumn(colM);

			SqlTableDiffGenerator diffBWithA = new SqlTableDiffGenerator(table2, table1, true);
			SqlTableDiffGenerator diffAWithB = new SqlTableDiffGenerator(table1, table2, true);
			SqlTableDiffGenerator diffNullNull = new SqlTableDiffGenerator(null, null, true);
			SqlTableDiffGenerator diffANull = new SqlTableDiffGenerator(table1, null, true);
			SqlTableDiffGenerator diffNullA = new SqlTableDiffGenerator(null, table1, true);

			Assert.assertTrue(DrCollectionTool.isEmpty(diffNullNull.getColumnsToRemove()));
			Assert.assertTrue(DrCollectionTool.isEmpty(diffNullA.getColumnsToRemove()));
			Assert.assertTrue(DrCollectionTool.isEmpty(diffANull.getColumnsToRemove()));
			Assert.assertTrue(DrCollectionTool.isEmpty(DrCollectionTool.minus(diffBWithA.getColumnsToRemove(),
					listM)));
			Assert.assertTrue(DrCollectionTool.isEmpty(DrCollectionTool.minus(diffAWithB.getColumnsToRemove(),
					listBAndC)));

			table1.addColumn(null);
			diffBWithA = new SqlTableDiffGenerator(table2, table1, true);
			diffAWithB = new SqlTableDiffGenerator(table1, table2, true);
			Assert.assertTrue(DrCollectionTool.isEmpty(DrCollectionTool.minus(diffBWithA.getColumnsToRemove(),
					listM)));
			Assert.assertTrue(DrCollectionTool.isEmpty(DrCollectionTool.minus(diffAWithB.getColumnsToRemove(),
					listBAndC)));
		}

		@Test
		public void getIndexesToAddTest(){
			IndexDiffHolder indexDiffHolder = prepareIndexDiff();
			Assert.assertEquals("index", DrCollectionTool.getFirst(indexDiffHolder.diffAWithB.getIndexesToAdd())
					.getName());
			Assert.assertTrue(DrCollectionTool.isEmpty(DrCollectionTool.minus(DrCollectionTool.getFirst(
					indexDiffHolder.diffAWithB.getIndexesToAdd()).getColumns(), indexDiffHolder.listM)));
			Assert.assertTrue(DrCollectionTool.isEmpty(DrCollectionTool.minus(DrCollectionTool.getFirst(
					indexDiffHolder.diffBWithA.getIndexesToAdd()).getColumns(), indexDiffHolder.listBAndC)));
			Assert.assertTrue(DrCollectionTool.isEmpty(indexDiffHolder.diffNullNull.getIndexesToAdd()));
			Assert.assertTrue(DrCollectionTool.isEmpty(indexDiffHolder.diffANull.getIndexesToAdd()));
			Assert.assertTrue(DrCollectionTool.isEmpty(indexDiffHolder.diffNullA.getIndexesToAdd()));
		}

		@Test
		public void getIndexesToRemove(){
			IndexDiffHolder indexDiffHolder = prepareIndexDiff();
			Assert.assertEquals("index", DrCollectionTool.getFirst(indexDiffHolder.diffAWithB.getIndexesToRemove())
					.getName());
			Assert.assertTrue(DrCollectionTool.isEmpty(DrCollectionTool.minus(DrCollectionTool.getFirst(
					indexDiffHolder.diffAWithB.getIndexesToRemove()).getColumns(), indexDiffHolder.listBAndC)));
			Assert.assertTrue(DrCollectionTool.isEmpty(DrCollectionTool.minus(DrCollectionTool.getFirst(
					indexDiffHolder.diffBWithA.getIndexesToRemove()).getColumns(), indexDiffHolder.listM)));
			Assert.assertTrue(DrCollectionTool.isEmpty(indexDiffHolder.diffNullNull.getIndexesToRemove()));
			Assert.assertTrue(DrCollectionTool.isEmpty(indexDiffHolder.diffANull.getIndexesToRemove()));
			Assert.assertTrue(DrCollectionTool.isEmpty(indexDiffHolder.diffNullA.getIndexesToRemove()));
		}

		private IndexDiffHolder prepareIndexDiff(){
			SqlColumn colA = new SqlColumn("A", MySqlColumnType.BIGINT, 250, true, false);
			SqlColumn colB = new SqlColumn("B", MySqlColumnType.BINARY);
			SqlColumn colC = new SqlColumn("C", MySqlColumnType.BOOLEAN);
			SqlColumn colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
			List<SqlColumn> listBAndC = new ArrayList<>();
			List<SqlColumn> listM = new ArrayList<>();

			listBAndC.add(colB);
			listBAndC.add(colC);
			listM.add(colM);
			SqlIndex index = new SqlIndex("index", listBAndC);
			SqlIndex index2 = new SqlIndex("index", listM);

			SqlTable table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC);
			SqlTable table2 = new SqlTable("TB").addColumn(colA).addColumn(colM);

			table1.addIndex(index);
			table2.addIndex(index2);
			IndexDiffHolder indexDiffHolder = new IndexDiffHolder();
			indexDiffHolder.diffBWithA = new SqlTableDiffGenerator(table2, table1, true);
			indexDiffHolder.diffAWithB = new SqlTableDiffGenerator(table1, table2, true);
			indexDiffHolder.diffNullNull = new SqlTableDiffGenerator(null, null, true);
			indexDiffHolder.diffANull = new SqlTableDiffGenerator(table1, null, true);
			indexDiffHolder.diffNullA = new SqlTableDiffGenerator(null, table1, true);
			indexDiffHolder.listBAndC = listBAndC;
			indexDiffHolder.listM = listM;
			return indexDiffHolder;
		}

		private class IndexDiffHolder{
			private List<SqlColumn> listM;
			private List<SqlColumn> listBAndC;
			private SqlTableDiffGenerator diffBWithA;
			private SqlTableDiffGenerator diffAWithB;
			private SqlTableDiffGenerator diffNullNull;
			private SqlTableDiffGenerator diffANull;
			private SqlTableDiffGenerator diffNullA;
		}

		@Test
		public void getColumnsToModifyTest(){
			SqlColumn colA = new SqlColumn("A", MySqlColumnType.BIGINT, 20, true, false);
			SqlColumn colA2 = new SqlColumn("A", MySqlColumnType.INT, 10, true, false);
			SqlColumn colB = new SqlColumn("B", MySqlColumnType.BINARY);
			SqlColumn colC = new SqlColumn("C", MySqlColumnType.BOOLEAN);
			SqlColumn colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
			List<SqlColumn> listBAndC = new ArrayList<>();
			List<SqlColumn> listM = new ArrayList<>();

			listBAndC.add(colB);
			listBAndC.add(colC);
			listM.add(colM);
			SqlIndex index = new SqlIndex("index", listBAndC),
					index2 = new SqlIndex("index", listM);

			SqlTable table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC);
			SqlTable table2 = new SqlTable("TB").addColumn(colA2).addColumn(colM);

			table1.addIndex(index);
			table2.addIndex(index2);
			SqlTableDiffGenerator diffBWithA = new SqlTableDiffGenerator(table2, table1, true);
			SqlTableDiffGenerator diffAWithB = new SqlTableDiffGenerator(table1, table2, true);
			SqlTableDiffGenerator diffNullNull = new SqlTableDiffGenerator(null, null, true);
			SqlTableDiffGenerator diffANull = new SqlTableDiffGenerator(table1, null, true);
			SqlTableDiffGenerator diffNullA = new SqlTableDiffGenerator(null, table1, true);

			logger.warn(diffAWithB.getColumnsToModify().toString());
			//TODO too much on one line
			List<SqlColumn> colsToModify = diffAWithB.getColumnsToModify();
			ArrayList<SqlColumn> expected = DrListTool.createArrayList(colA2);
			SqlColumnNameTypeLengthAutoIncrementDefaultComparator comparator =
					new SqlColumnNameTypeLengthAutoIncrementDefaultComparator(true);
			Assert.assertTrue(areEqual(colsToModify, expected, comparator));


			logger.warn(diffBWithA.getColumnsToModify().toString());
			//TODO too much on one line
			Assert.assertTrue(DrCollectionTool.isEmpty(DrCollectionTool.minus(diffBWithA.getColumnsToModify(),
					DrListTool.createArrayList(colA2), new SqlColumnNameTypeComparator(true))));
			Assert.assertTrue(DrCollectionTool.isEmpty(diffNullNull.getColumnsToModify()));
			Assert.assertTrue(DrCollectionTool.isEmpty(diffANull.getColumnsToModify()));
			Assert.assertTrue(DrCollectionTool.isEmpty(diffNullA.getColumnsToModify()));


		}

		@Test
		public void getColumnsToModifyBugTest(){
			SqlColumn colActive = new SqlColumn("active", MySqlColumnType.BIT, 1, true, false);
			SqlColumn colActiveTiny = new SqlColumn("active", MySqlColumnType.TINYINT, 1, true, false);
			SqlColumn colIncludeInSiteMap = new SqlColumn("includeInSiteMap", MySqlColumnType.BIT, 1, true, false);
			SqlColumn colIncludeInSiteMapTiny = new SqlColumn("includeInSiteMap", MySqlColumnType.TINYINT, 1, true,
					false);
			SqlColumn colType = new SqlColumn("type", MySqlColumnType.INT, 11, true, false);
			SqlColumn colUseBoundedLayout = new SqlColumn("useBoundedLayout", MySqlColumnType.BIT, 1, true, false);
			SqlColumn colUseBoundedLayoutTiny = new SqlColumn("useBoundedLayout", MySqlColumnType.TINYINT, 1, true,
					false);
			SqlColumn colRedirect = new SqlColumn("redirect", MySqlColumnType.VARCHAR, 255, true, false);
			SqlColumn colBody = new SqlColumn("body", MySqlColumnType.MEDIUMTEXT, 16777216, true, false);
			SqlColumn colId = new SqlColumn("id", MySqlColumnType.VARCHAR, 255, true, false);
			SqlColumn colMetaKeywords = new SqlColumn("metaKeywords", MySqlColumnType.VARCHAR, 255, true, false);
			SqlColumn colTitle = new SqlColumn("title", MySqlColumnType.VARCHAR, 255, true, false);
			SqlColumn colMetaDescription = new SqlColumn("metaDescription", MySqlColumnType.VARCHAR, 255, true, false);
			SqlColumn colAttributes = new SqlColumn("attributes", MySqlColumnType.VARCHAR, 255, true, false);
			SqlColumn lastModified = new SqlColumn("lastModified", MySqlColumnType.DATETIME);

			SqlTable table1 = new SqlTable("TA").addColumn(colActive)
					.addColumn(colIncludeInSiteMap)
					.addColumn(colType)
					.addColumn(colUseBoundedLayout)
					.addColumn(colRedirect)
					.addColumn(colBody)
					.addColumn(colId)
					.addColumn(colMetaKeywords)
					.addColumn(colTitle)
					.addColumn(colMetaDescription)
					.addColumn(colAttributes)
					.addColumn(lastModified);
			SqlTable table2  = new SqlTable("TB").addColumn(colActiveTiny)
					.addColumn(colIncludeInSiteMapTiny)
					.addColumn(colType)
					.addColumn(colUseBoundedLayoutTiny)
					.addColumn(colRedirect)
					.addColumn(colBody)
					.addColumn(colId)
					.addColumn(colMetaKeywords)
					.addColumn(colTitle)
					.addColumn(colMetaDescription)
					.addColumn(colAttributes)
					.addColumn(lastModified);

			SqlTableDiffGenerator diffAWithB = new SqlTableDiffGenerator(table1, table2, true);
			List<SqlColumn> colsToModify = diffAWithB.getColumnsToModify();
			ArrayList<SqlColumn> expected = DrListTool.createArrayList(colActiveTiny, colIncludeInSiteMapTiny,
					colUseBoundedLayoutTiny);
			SqlColumnNameTypeLengthAutoIncrementDefaultComparator comparator =
					new SqlColumnNameTypeLengthAutoIncrementDefaultComparator(true);
			Assert.assertTrue(areEqual(colsToModify, expected, comparator));

			Set<SqlColumn> requestedColumns = new TreeSet<>(comparator);
			Set<SqlColumn> currentColumns = new TreeSet<>(comparator);
			requestedColumns.addAll(diffAWithB.getCurrent().getColumns());
			currentColumns.addAll(diffAWithB.getRequested().getColumns());
			Assert.assertEquals(requestedColumns.size(),diffAWithB.getRequested().getColumns().size());
			Assert.assertEquals(currentColumns.size(),diffAWithB.getCurrent().getColumns().size());
		}
		private boolean areEqual(List<SqlColumn> colsToModify, ArrayList<SqlColumn> expected,
				Comparator<SqlColumn> comparator){
			return DrCollectionTool.isEmpty(DrCollectionTool.minus(colsToModify, expected, comparator));
		}

	}

}
