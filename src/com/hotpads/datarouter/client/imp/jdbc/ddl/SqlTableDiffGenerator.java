package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlTableEngine;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn.SqlColumnNameComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn.SqlColumnNameTypeComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn.SqlColumnNameTypeLengthComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex.SqlIndexNameComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public class SqlTableDiffGenerator{

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
		SqlColumnNameComparator c = new SqlColumnNameComparator(true);
		Set<SqlColumn> requestedColumns = new TreeSet<SqlColumn>(c);
		Set<SqlColumn> currentColumns = new TreeSet<SqlColumn>(c);
		if(requested==null || current==null){
			return ListTool.createArrayList();
		}else{
			requestedColumns.addAll(requested.getColumns());
			currentColumns.addAll(current.getColumns());
		}
		return ListTool.createArrayList(CollectionTool.minus(requestedColumns, currentColumns,c));
	}

	public List<SqlColumn> getColumnsToRemove(){
		SqlColumnNameComparator c = new SqlColumnNameComparator(true);
		Set<SqlColumn> requestedColumns = new TreeSet<SqlColumn>(c);
		Set<SqlColumn> currentColumns = new TreeSet<SqlColumn>(c);
		if(requested==null || current==null){
			return ListTool.createArrayList();
		}else{
			requestedColumns.addAll(requested.getColumns());
			currentColumns.addAll(current.getColumns());
		}
		return ListTool.createArrayList(CollectionTool.minus(currentColumns, requestedColumns, c));
	}
	
	public List<SqlColumn> getColumnsToModify(){
		SqlColumnNameTypeLengthComparator c = new SqlColumnNameTypeLengthComparator(true);
		Set<SqlColumn> requestedColumns = new TreeSet<SqlColumn>(c);
		Set<SqlColumn> currentColumns = new TreeSet<SqlColumn>(c);
		if(requested==null || current==null){
			return ListTool.createArrayList();
		}else{
			requestedColumns.addAll(requested.getColumns());
			currentColumns.addAll(current.getColumns());
		}
				//TODO too much on one line.  extract the sets into their own variables
		return getColumnsToModifyAfterAddingColumns(requestedColumns,currentColumns,getColumnsToAdd(),c);
	}
	
	private List<SqlColumn> getColumnsToModifyAfterAddingColumns(Set<SqlColumn> requestedColumns,
			Set<SqlColumn> currentColumns, 
			List<SqlColumn> columnsToAddUsingNameComparator, SqlColumnNameTypeLengthComparator c){
		// by getting all the modified columns (the ones we should add) and removing from them the ones
		// we have already added (columnsToAdd)
		List<SqlColumn> listOfColumnsToAddUsingNameTypeComparator = ListTool.createArrayList(CollectionTool.minus(
				requestedColumns, currentColumns, c));
		Set<SqlColumn> columnsToModify = CollectionTool.minus(listOfColumnsToAddUsingNameTypeComparator, columnsToAddUsingNameComparator);
		return ListTool.createArrayList(columnsToModify);
	}
	
	public List<SqlIndex> getIndexesToAdd(){
		if(requested == null || current == null){ return ListTool.createArrayList(); }
				//TODO too much on one line.  extract the sets into their own variables
		List<SqlIndex> requestedIndexes = requested.getIndexes();
		List<SqlIndex> currentIndexes = current.getIndexes();
		Set<SqlIndex> IndexesToAdd = CollectionTool.minus(requestedIndexes, currentIndexes);
		return ListTool.createArrayList(IndexesToAdd);
	}

	public List<SqlIndex> getIndexesToRemove(){
		if(requested == null || current == null){ return ListTool.createArrayList(); }
				//TODO too much on one line.  extract the sets into their own variables
		List<SqlIndex> requestedIndexes = requested.getIndexes();
		List<SqlIndex> currentIndexes = current.getIndexes();
		TreeSet<SqlIndex> indexesToRemove = CollectionTool.minus(currentIndexes, requestedIndexes,
					new SqlIndexNameComparator());
		return ListTool.createArrayList(indexesToRemove);
	}
	
	/********************* helper methods *******************************/
	
	public boolean shouldReorderColumns(){
		return enforceColumnOrder && ! isColumnOrderCorrect();
	}
	
	public boolean isColumnOrderCorrect(){
		return true;//TODO implement
	}

	public boolean isTableModified(){
		if(isPrimaryKeyModified()){ return true; }
				//TODO too much on one line.  extract the sets into their own variables
		SortedSet<SqlColumn> currentColumns = SetTool.createTreeSet(current.getColumns());
		SortedSet<SqlColumn> requestedColumns = SetTool.createTreeSet(requested.getColumns());
		if(!theTwoColumnSetsContainTheSameKeys(currentColumns, requestedColumns)){ return true; }
		if(isIndexesModified()){ return true; }
		if(isEngineModified()){ return true; }
		if(isCharacterSetModified()){ return true; }
		if(isCollationModified()){ return true; }
		return false;
	}

	private boolean theTwoColumnSetsContainTheSameKeys(SortedSet<SqlColumn> treeSet, SortedSet<SqlColumn> treeSet2){
		return SetTool.containsSameKeys(treeSet,treeSet2);
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
				//TODO too much on one line.  extract the sets into their own variables
		SortedSet<SqlIndex> currentIndexes = SetTool.createTreeSet(current.getIndexes());
		SortedSet<SqlIndex> requestedIndexes = SetTool.createTreeSet(requested.getIndexes());
		return !SetTool.containsSameKeys(currentIndexes, requestedIndexes);
	}

	public boolean isPrimaryKeyModified(){
				//TODO too much on one line.  extract the sets into their own variables
		List<SqlColumn> currentPrimaryKeyColumns = current.getPrimaryKey().getColumns();
		List<SqlColumn> requestedPrimaryKeyColumns = requested.getPrimaryKey().getColumns();
		if(!haveTheSameColumnsinTheSameOrder(currentPrimaryKeyColumns, requestedPrimaryKeyColumns)){ 
			return true; 
		}
		return false;
	}

	private boolean haveTheSameColumnsinTheSameOrder(List<SqlColumn> currentPrimaryKeyColumns,
			List<SqlColumn> requestedPrimaryKeyColumns){
		return CollectionTool.equalsAllElementsInIteratorOrder(currentPrimaryKeyColumns, requestedPrimaryKeyColumns);
	}

	public SqlTable getRequested(){
		return requested;
	}
	
	public SqlTable getCurrent(){
		return current;
	}


	/********************* Tests *******************************/
	
	public static class SqlTableDiffGeneratorTester{
		private SqlColumn idCol = new SqlColumn("id", MySqlColumnType.BIGINT);
		private SqlIndex primaryKey1 = new SqlIndex("pk1").addColumn(idCol);
		
		@Test public void isTableModifiedTest(){
					//TODO don't reuse declaration types anywhere
			List<SqlColumn> listA = ListTool.createArrayList();
			List<SqlColumn> listA2 = ListTool.createArrayList();
					//listB = ListTool.createArrayList();
			SqlTable tableA = new SqlTable("A", listA, primaryKey1);
			// SqlTable tableB = new SqlTable("B", listB, primaryKey1);
			SqlTable tableA2 = new SqlTable("A", listA2,primaryKey1);
			
			SqlTableDiffGenerator diffAA = new SqlTableDiffGenerator(tableA, tableA, true),
								  diffAA2 = new SqlTableDiffGenerator(tableA, tableA2, true);
								//diffAB = new SqlTableDiffGenerator(tableA, tableB, true);
			Assert.assertFalse(diffAA.isTableModified());
			// Assert.assertTrue(diffAB.isTableModified()); 						// WE CAN'T HAVE DIFFERENT TABLES WITH THE SAME NAME
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

		@Test public void isPrimaryKeyModifiedTest(){
			List<SqlColumn> list1 = ListTool.createArrayList();
			List<SqlColumn> list2 = ListTool.createArrayList();
			SqlColumn idCol = new SqlColumn("id", MySqlColumnType.BIGINT);
			SqlColumn col = new SqlColumn("id", MySqlColumnType.BIGINT);
			
			SqlIndex primaryKey1 = new SqlIndex("pk1").addColumn(idCol);
			SqlIndex primaryKey2 = new SqlIndex("pk2").addColumn(idCol).addColumn(col);
			
			SqlTable A = new SqlTable("Table 1", list1, primaryKey1 );
			SqlTable B = new SqlTable("Table 2", list2, primaryKey2 );
			SqlTable A2 = new SqlTable("Table 1", list1, primaryKey2);
			SqlTable A0 = new SqlTable("Table 1"); // without pKey
			
			SqlTableDiffGenerator diffAA = new SqlTableDiffGenerator(A, A, true);
			SqlTableDiffGenerator diffAB = new SqlTableDiffGenerator(A, B, true);
					//TODO need spaces after commas everywhere
			SqlTableDiffGenerator diffAA2 = new SqlTableDiffGenerator(A, A2, true);
			SqlTableDiffGenerator diffAA0 = new SqlTableDiffGenerator(A, A0, true);
			
			Assert.assertFalse(diffAA.isPrimaryKeyModified());
			Assert.assertTrue(diffAB.isPrimaryKeyModified());
			Assert.assertTrue(diffAA2.isPrimaryKeyModified());
			Assert.assertTrue(diffAA0.isPrimaryKeyModified());
		}
	
		@Test public void getColumnsToAddTest(){
			SqlColumn colA = new SqlColumn("A", MySqlColumnType.BIGINT, 250, true);
			SqlColumn colB = new SqlColumn("B", MySqlColumnType.BINARY);
			SqlColumn colC = new SqlColumn("C", MySqlColumnType.BOOLEAN);
			SqlColumn colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
			List<SqlColumn> 
					listBC = ListTool.createArrayList(),
					listM = ListTool.createArrayList();
			
			listBC.add(colB);
			listBC.add(colC);
			listM.add(colM);
			SqlTable 
					table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC),
					table2 = new SqlTable("TB").addColumn(colA).addColumn(colM);
			
			SqlTableDiffGenerator diffBA = new SqlTableDiffGenerator(table2, table1, true);
			SqlTableDiffGenerator diffAB = new SqlTableDiffGenerator(table1, table2, true);
			SqlTableDiffGenerator diffNullNull = new SqlTableDiffGenerator(null, null, true);
			SqlTableDiffGenerator diffANull = new SqlTableDiffGenerator(table1, null, true);
			SqlTableDiffGenerator diffNullA = new SqlTableDiffGenerator(null, table1, true);
			
			Assert.assertTrue(CollectionTool.isEmpty(diffNullNull.getColumnsToAdd()));
			Assert.assertTrue(CollectionTool.isEmpty(diffNullA.getColumnsToAdd()));
			Assert.assertTrue(CollectionTool.isEmpty(diffANull.getColumnsToAdd()));
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffBA.getColumnsToAdd(), listBC)));
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffAB.getColumnsToAdd(), listM)));
			
			table1.addColumn(null);
			diffBA = new SqlTableDiffGenerator(table2, table1, true);
			diffAB = new SqlTableDiffGenerator(table1, table2, true);
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffBA.getColumnsToAdd(), listBC)));
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffAB.getColumnsToAdd(), listM)));
			
			SqlColumn ColA2 = new SqlColumn("A", MySqlColumnType.VARCHAR,200,true);
			table1.addColumn(ColA2);
			diffBA = new SqlTableDiffGenerator(table2, table1, true);
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffBA.getColumnsToAdd(), listBC)));
		}
		
		@Test public void getColumnsToRemoveTest(){
			SqlColumn colA = new SqlColumn("A", MySqlColumnType.BIGINT);
			SqlColumn colB = new SqlColumn("B", MySqlColumnType.BINARY);
			SqlColumn colC = new SqlColumn("C", MySqlColumnType.BOOLEAN);
			SqlColumn colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
			List<SqlColumn> listBC = ListTool.createArrayList();
			List<SqlColumn> listM = ListTool.createArrayList();
	
			listBC.add(colB);
			listBC.add(colC);
			listM.add(colM);
			SqlTable table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC);
			SqlTable table2 = new SqlTable("TB").addColumn(colA).addColumn(colM);
			
			SqlTableDiffGenerator diffBA = new SqlTableDiffGenerator(table2, table1, true);
			SqlTableDiffGenerator diffAB = new SqlTableDiffGenerator(table1, table2, true);
			SqlTableDiffGenerator diffNullNull = new SqlTableDiffGenerator(null, null, true);
			SqlTableDiffGenerator diffANull = new SqlTableDiffGenerator(table1, null, true);
			SqlTableDiffGenerator diffNullA = new SqlTableDiffGenerator(null, table1, true);

			Assert.assertTrue(CollectionTool.isEmpty(diffNullNull.getColumnsToRemove()));
			Assert.assertTrue(CollectionTool.isEmpty(diffNullA.getColumnsToRemove()));
			Assert.assertTrue(CollectionTool.isEmpty(diffANull.getColumnsToRemove()));
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffBA.getColumnsToRemove(), listM)));
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffAB.getColumnsToRemove(), listBC)));
			
			table1.addColumn(null);
			diffBA = new SqlTableDiffGenerator(table2, table1, true);
			diffAB = new SqlTableDiffGenerator(table1, table2, true);
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffBA.getColumnsToRemove(), listM)));
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffAB.getColumnsToRemove(), listBC)));
		}
	
		@Test public void getIndexesToAddTest(){
			SqlColumn colA = new SqlColumn("A", MySqlColumnType.BIGINT,250,true);
			SqlColumn colB = new SqlColumn("B", MySqlColumnType.BINARY);
			SqlColumn colC = new SqlColumn("C", MySqlColumnType.BOOLEAN);
			SqlColumn colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
			List<SqlColumn> listBC = ListTool.createArrayList();
			List<SqlColumn> listM = ListTool.createArrayList();
			
			listBC.add(colB);
			listBC.add(colC);
			listM.add(colM);
			SqlIndex index = new SqlIndex("index", listBC);
			SqlIndex index2 = new SqlIndex("index", listM);
			
			SqlTable table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC);
			SqlTable table2 = new SqlTable("TB").addColumn(colA).addColumn(colM);

			table1.addIndex(index);
			table2.addIndex(index2);
			SqlTableDiffGenerator diffBA = new SqlTableDiffGenerator(table2, table1, true);
			SqlTableDiffGenerator diffAB = new SqlTableDiffGenerator(table1, table2, true);
			SqlTableDiffGenerator diffNullNull = new SqlTableDiffGenerator(null, null, true);
			SqlTableDiffGenerator diffANull = new SqlTableDiffGenerator(table1, null, true);
			SqlTableDiffGenerator diffNullA = new SqlTableDiffGenerator(null, table1, true);
			
			Assert.assertEquals("index", diffAB.getIndexesToAdd().get(0).getName());
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffAB.getIndexesToAdd().get(0).getColumns(),
					listM)));
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffBA.getIndexesToAdd().get(0).getColumns(),
					listBC)));
			Assert.assertTrue(CollectionTool.isEmpty(diffNullNull.getIndexesToAdd()));
			Assert.assertTrue(CollectionTool.isEmpty(diffANull.getIndexesToAdd()));
			Assert.assertTrue(CollectionTool.isEmpty(diffNullA.getIndexesToAdd()));
		}
	
		@Test public void getIndexesToRemove(){
			SqlColumn colA = new SqlColumn("A", MySqlColumnType.BIGINT, 250, true);
			SqlColumn colB = new SqlColumn("B", MySqlColumnType.BINARY);
			SqlColumn colC = new SqlColumn("C", MySqlColumnType.BOOLEAN);
			SqlColumn colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
			List<SqlColumn> listBC = ListTool.createArrayList();
			List<SqlColumn> listM = ListTool.createArrayList();
			
			listBC.add(colB);
			listBC.add(colC);
			listM.add(colM);
			SqlIndex index = new SqlIndex("index", listBC);
			SqlIndex index2 = new SqlIndex("index", listM);
			
			SqlTable table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC);
			SqlTable table2 = new SqlTable("TB").addColumn(colA).addColumn(colM);
			
			table1.addIndex(index);
			table2.addIndex(index2);
			SqlTableDiffGenerator diffBA = new SqlTableDiffGenerator(table2, table1, true);
			SqlTableDiffGenerator diffAB = new SqlTableDiffGenerator(table1, table2, true);
			SqlTableDiffGenerator diffNullNull = new SqlTableDiffGenerator(null, null, true);
			SqlTableDiffGenerator diffANull = new SqlTableDiffGenerator(table1, null, true);
			SqlTableDiffGenerator diffNullA = new SqlTableDiffGenerator(null, table1, true);

			Assert.assertEquals("index", diffAB.getIndexesToRemove().get(0).getName());
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffAB.getIndexesToRemove().get(0).getColumns(), listBC)));
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffBA.getIndexesToRemove().get(0).getColumns(), listM)));
			Assert.assertTrue(CollectionTool.isEmpty(diffNullNull.getIndexesToRemove()));
			Assert.assertTrue(CollectionTool.isEmpty(diffANull.getIndexesToRemove()));
			Assert.assertTrue(CollectionTool.isEmpty(diffNullA.getIndexesToRemove()));
		}
	
		@Test
		public void getColumnsToModifyTest(){
			SqlColumn colA = new SqlColumn("A", MySqlColumnType.BIGINT, 20, true);
			SqlColumn colA2 = new SqlColumn("A", MySqlColumnType.INT, 10, true);
			SqlColumn colB = new SqlColumn("B", MySqlColumnType.BINARY);
			SqlColumn colC = new SqlColumn("C", MySqlColumnType.BOOLEAN);
			SqlColumn colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
			List<SqlColumn> listBC = ListTool.createArrayList();
			List<SqlColumn> listM = ListTool.createArrayList();
			
			listBC.add(colB);
			listBC.add(colC);
			listM.add(colM);
			SqlIndex index = new SqlIndex("index", listBC),
					index2 = new SqlIndex("index", listM);
			
			SqlTable table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC); 
			SqlTable table2 = new SqlTable("TB").addColumn(colA2).addColumn(colM);
			
			table1.addIndex(index);
			table2.addIndex(index2);
			SqlTableDiffGenerator diffBA = new SqlTableDiffGenerator(table2, table1, true);
			SqlTableDiffGenerator diffAB = new SqlTableDiffGenerator(table1, table2, true);
			SqlTableDiffGenerator diffNullNull = new SqlTableDiffGenerator(null, null, true);
			SqlTableDiffGenerator diffANull = new SqlTableDiffGenerator(table1, null, true);
			SqlTableDiffGenerator diffNullA = new SqlTableDiffGenerator(null, table1, true);

			System.out.println(diffAB.getColumnsToModify());
					//TODO too much on one line
			List<SqlColumn> colsToModify = diffAB.getColumnsToModify();
			ArrayList<SqlColumn> expected = ListTool.createArrayList(colA2);
			SqlColumnNameTypeLengthComparator c = new SqlColumnNameTypeLengthComparator(true);
			Assert.assertTrue(areEqual(colsToModify, expected, c));
			
			
			System.out.println(diffBA.getColumnsToModify());
			//TODO too much on one line
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffBA.getColumnsToModify(), ListTool
					.createArrayList(colA2), new SqlColumnNameTypeComparator(true))));
			Assert.assertTrue(CollectionTool.isEmpty(diffNullNull.getColumnsToModify()));
			Assert.assertTrue(CollectionTool.isEmpty(diffANull.getColumnsToModify()));
			Assert.assertTrue(CollectionTool.isEmpty(diffNullA.getColumnsToModify()));
			

		}

		@Test
		public void getColumnsToModifyBugTest(){
			SqlColumn col_active = new SqlColumn("active", MySqlColumnType.BIT, 1, true);
			SqlColumn col_activeTiny = new SqlColumn("active", MySqlColumnType.TINYINT, 1, true);
			SqlColumn col_includeInSiteMap = new SqlColumn("includeInSiteMap", MySqlColumnType.BIT, 1, true);
			SqlColumn col_includeInSiteMapTiny = new SqlColumn("includeInSiteMap", MySqlColumnType.TINYINT, 1, true);
			SqlColumn col_type = new SqlColumn("type", MySqlColumnType.INT, 11, true);
			SqlColumn col_useBoundedLayout = new SqlColumn("useBoundedLayout", MySqlColumnType.BIT, 1, true);
			SqlColumn col_useBoundedLayoutTiny = new SqlColumn("useBoundedLayout", MySqlColumnType.TINYINT, 1, true);
			SqlColumn col_redirect = new SqlColumn("redirect", MySqlColumnType.VARCHAR, 255, true);
			SqlColumn col_body = new SqlColumn("body", MySqlColumnType.MEDIUMTEXT, 16777216, true);
			SqlColumn col_id = new SqlColumn("id", MySqlColumnType.VARCHAR, 255, true);
			SqlColumn col_metaKeywords = new SqlColumn("metaKeywords", MySqlColumnType.VARCHAR, 255, true);
			SqlColumn col_title = new SqlColumn("title", MySqlColumnType.VARCHAR, 255, true);
			SqlColumn col_metaDescription = new SqlColumn("metaDescription", MySqlColumnType.VARCHAR, 255, true);
			SqlColumn col_attributes = new SqlColumn("attributes", MySqlColumnType.VARCHAR, 255, true);
			SqlColumn lastModified = new SqlColumn("lastModified", MySqlColumnType.DATETIME);

			SqlTable table1 = new SqlTable("TA").addColumn(col_active)
						.addColumn(col_includeInSiteMap)
						.addColumn(col_type)
						.addColumn(col_useBoundedLayout)
						.addColumn(col_redirect)
						.addColumn(col_body)
						.addColumn(col_id)
						.addColumn(col_metaKeywords)
						.addColumn(col_title)
						.addColumn(col_metaDescription)
						.addColumn(col_attributes)
						.addColumn(lastModified);
			SqlTable table2  = new SqlTable("TB").addColumn(col_activeTiny)
					.addColumn(col_includeInSiteMapTiny)
					.addColumn(col_type)
					.addColumn(col_useBoundedLayoutTiny)
					.addColumn(col_redirect)
					.addColumn(col_body)
					.addColumn(col_id)
					.addColumn(col_metaKeywords)
					.addColumn(col_title)
					.addColumn(col_metaDescription)
					.addColumn(col_attributes)
					.addColumn(lastModified);
			
			SqlTableDiffGenerator diffAB = new SqlTableDiffGenerator(table1, table2, true);
			List<SqlColumn> colsToModify = diffAB.getColumnsToModify();
			ArrayList<SqlColumn> expected = ListTool.createArrayList(col_activeTiny, col_includeInSiteMapTiny, col_useBoundedLayoutTiny);
			SqlColumnNameTypeLengthComparator c = new SqlColumnNameTypeLengthComparator(true);
			Assert.assertTrue(areEqual(colsToModify, expected, c));
			
			Set<SqlColumn> requestedColumns = new TreeSet<SqlColumn>(c);
			Set<SqlColumn> currentColumns = new TreeSet<SqlColumn>(c);
			requestedColumns.addAll(diffAB.getCurrent().getColumns());
			currentColumns.addAll(diffAB.getRequested().getColumns());
			Assert.assertEquals(requestedColumns.size(),diffAB.getRequested().getColumns().size());
			Assert.assertEquals(currentColumns.size(),diffAB.getCurrent().getColumns().size());
		}
		private boolean areEqual(List<SqlColumn> colsToModify, ArrayList<SqlColumn> expected,
				Comparator<SqlColumn> c){
			return CollectionTool.isEmpty(CollectionTool.minus(colsToModify, expected, c));
		}
	
	}

}
