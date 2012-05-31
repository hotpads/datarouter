package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn.*;
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
		Set<SqlColumn> requestedColumns = new TreeSet<SqlColumn>(c), currentColumns = new TreeSet<SqlColumn>(c);
		if(requested==null || current==null){
			return ListTool.createArrayList();
		}
		else{
			requestedColumns.addAll(requested.getColumns());
			currentColumns.addAll( current.getColumns());
		}
		return ListTool.createArrayList(CollectionTool.minus(requestedColumns,currentColumns,c));
	}

	public List<SqlColumn> getColumnsToRemove(){
		SqlColumnNameComparator c = new SqlColumnNameComparator(true);
		Set<SqlColumn> requestedColumns = new TreeSet<SqlColumn>(c), currentColumns = new TreeSet<SqlColumn>(c);
		if(requested==null || current==null){
			return ListTool.createArrayList();
		}
		else{
			requestedColumns.addAll(requested.getColumns());
			currentColumns.addAll( current.getColumns());
		}
		return ListTool.createArrayList(CollectionTool.minus(currentColumns, requestedColumns,c));
	}
	
	public List<SqlColumn> getColumnsToModify(){
		SqlColumnNameTypeComparator c = new SqlColumnNameTypeComparator(true);
		Set<SqlColumn> requestedColumns = new TreeSet<SqlColumn>(c), currentColumns = new TreeSet<SqlColumn>(c);
		if(requested==null || current==null){
			return ListTool.createArrayList();
		}
		else{
			requestedColumns.addAll(requested.getColumns());
			currentColumns.addAll( current.getColumns());
		}
		List<SqlColumn> listOfColumnsToAddWithNameTypeComparator = ListTool.createArrayList(CollectionTool.minus(requestedColumns,currentColumns,c));
		
		return ListTool.createArrayList(CollectionTool.minus(listOfColumnsToAddWithNameTypeComparator,getColumnsToAdd()));
	}
	
	public List<SqlColumn> getListOfColumnPossiblyTheSame(int maxDistanceAllowed){
		SqlColumnNameComparatorUsingLevenshteinDistance c = new SqlColumnNameComparatorUsingLevenshteinDistance(true,maxDistanceAllowed);
		Set<SqlColumn> requestedColumns = new TreeSet<SqlColumn>(c), currentColumns = new TreeSet<SqlColumn>(c);
		if(requested==null || current==null){
			return ListTool.createArrayList();
		}
		else{
			requestedColumns.addAll(requested.getColumns());
			currentColumns.addAll( current.getColumns());
		}
		 return ListTool.createArrayList(CollectionTool.intersection(currentColumns, requestedColumns,c));
	}
	
	public List<SqlIndex> getIndexesToAdd(){
		if(requested==null || current==null){
			return ListTool.createArrayList();
		}
		return ListTool.createArrayList(CollectionTool.minus(requested.getIndexes(), current.getIndexes()));
	}

	public List<SqlIndex> getIndexesToRemove(){
		if(requested==null || current==null){
			return ListTool.createArrayList();
		}
		return ListTool.createArrayList(CollectionTool.minus(current.getIndexes(), requested.getIndexes()));
	}
	
	/********************* helper methods *******************************/
	
	public boolean shouldReorderColumns() {
		return enforceColumnOrder && ! isColumnOrderCorrect();
	}
	
	public boolean isColumnOrderCorrect() {
		return true;//TODO implement
	}

	public boolean isTableModified(){
		if(isPrimaryKeyModified()){ return true; }
		if(!SetTool.containsSameKeys(SetTool.createTreeSet(current.getColumns()), SetTool.createTreeSet(requested
				.getColumns()))){ return true; }
		if(isIndexesModified()){ return true; }
		return false;
	}

	public boolean isIndexesModified() {
		return !SetTool.containsSameKeys(SetTool.createTreeSet(current.getIndexes()), SetTool.createTreeSet(requested
				.getIndexes()));
	}

	public boolean isPrimaryKeyModified(){
		if(!CollectionTool.equalsAllElementsInIteratorOrder(current.getPrimaryKey().getColumns(), 
				requested.getPrimaryKey().getColumns())){ 
			return true; 
		}
		return false;
	}

	public SqlTable getRequested() {
		return requested;
	}

	
	/********************* Tests *******************************/
	
	public static class SqlTableDiffGeneratorTester{
		@Test public void isTableModifiedTest(){
			SqlColumn 
					idCol = new SqlColumn("id", MySqlColumnType.BIGINT);
			SqlIndex primaryKey1 = new SqlIndex("pk1").addColumn(idCol);
			List<SqlColumn> 
					listA = ListTool.createArrayList(), 
					listA2 = ListTool.createArrayList();
					//listB = ListTool.createArrayList();
			SqlTable 
					tableA = new SqlTable("A", listA, primaryKey1), 
					// tableB = new SqlTable("B", listB, primaryKey1), 
					tableA2 = new SqlTable("A", listA2,primaryKey1);
			
			SqlTableDiffGenerator diffAA = new SqlTableDiffGenerator(tableA, tableA, true),
								  diffAA2 = new SqlTableDiffGenerator(tableA, tableA2, true);
								//diffAB = new SqlTableDiffGenerator(tableA, tableB, true);
			Assert.assertFalse(diffAA.isTableModified());
			// Assert.assertTrue(diffAB.isTableModified()); 						// WE CAN'T HAVE DIFFERENT TABLES WITH THE SAME NAME
			// TABLES WITH DIFFERENT NUMBER OF COLUMNS
			SqlColumn 
					col1 = new SqlColumn("col1", MySqlColumnType.BIGINT), 
					col2 = new SqlColumn("col2", MySqlColumnType.BINARY), 
					col3 = new SqlColumn("col3", MySqlColumnType.BIT);
			tableA.addColumn(col1);
			tableA2.addColumn(col1);
			tableA2.addColumn(col2);
			Assert.assertTrue(diffAA2.isTableModified());
			// TABLES WITH THE SAME NUMBER OF COLUMNS, BUT 1 OR MORE DIFFERENT COLUMN
			tableA.addColumn(col3);
			Assert.assertTrue(diffAA2.isTableModified());
		}

		@Test public void isPrimaryKeyModifiedTest(){
			List<SqlColumn> 
					list1 = ListTool.createArrayList(),
					list2 = ListTool.createArrayList();
			SqlColumn 
					idCol = new SqlColumn("id", MySqlColumnType.BIGINT),
					col	= new SqlColumn("id", MySqlColumnType.BIGINT);
			
			SqlIndex 
					primaryKey1 = new SqlIndex("pk1").addColumn(idCol),
					primaryKey2 = new SqlIndex("pk2").addColumn(idCol).addColumn(col);
			
			SqlTable 
					A = new SqlTable("Table 1", list1, primaryKey1 ),
					B = new SqlTable("Table 2", list2, primaryKey2 ),
					A2 = new SqlTable("Table 1", list1, primaryKey2),
					A0 = new SqlTable("Table 1"); // without pKey
			
			SqlTableDiffGenerator 
					diffAA = new SqlTableDiffGenerator(A, A, true),
					diffAB = new SqlTableDiffGenerator(A, B, true),
					diffAA2 = new SqlTableDiffGenerator(A,A2,true),
					diffAA0 = new SqlTableDiffGenerator(A,A0,true);
			
			Assert.assertFalse(diffAA.isPrimaryKeyModified());
			Assert.assertTrue(diffAB.isPrimaryKeyModified());
			Assert.assertTrue(diffAA2.isPrimaryKeyModified());
			Assert.assertTrue(diffAA0.isPrimaryKeyModified());
		}
	
		@Test public void getColumnsToAddTest(){
			SqlColumn 
					colA = new SqlColumn("A", MySqlColumnType.BIGINT,250,true),
					colB = new SqlColumn("B", MySqlColumnType.BINARY),
					colC = new SqlColumn("C", MySqlColumnType.BOOLEAN),
					colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
			List<SqlColumn> 
					listBC = ListTool.createArrayList(),
					listM = ListTool.createArrayList();
			
			listBC.add(colB);
			listBC.add(colC);
			listM.add(colM);
			SqlTable 
					table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC),
					table2 = new SqlTable("TB").addColumn(colA).addColumn(colM);
			
			SqlTableDiffGenerator 
					diffBA = new SqlTableDiffGenerator(table2, table1, true),
					diffAB = new SqlTableDiffGenerator(table1, table2, true),
					diffNullNull = new SqlTableDiffGenerator(null, null, true),
					diffANull = new SqlTableDiffGenerator(table1, null, true),
					diffNullA = new SqlTableDiffGenerator(null, table1, true);
			
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
			SqlColumn 
					colA = new SqlColumn("A", MySqlColumnType.BIGINT),
					colB = new SqlColumn("B", MySqlColumnType.BINARY),
					colC = new SqlColumn("C", MySqlColumnType.BOOLEAN),
					colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
			List<SqlColumn> 
					listBC = ListTool.createArrayList(),
					listM = ListTool.createArrayList();
	
			listBC.add(colB);
			listBC.add(colC);
			listM.add(colM);
			SqlTable 
					table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC),
					table2 = new SqlTable("TB").addColumn(colA).addColumn(colM);
			
			SqlTableDiffGenerator 
					diffBA = new SqlTableDiffGenerator(table2, table1, true),
					diffAB = new SqlTableDiffGenerator(table1, table2, true),
					diffNullNull = new SqlTableDiffGenerator(null, null, true),
					diffANull = new SqlTableDiffGenerator(table1, null, true),
					diffNullA = new SqlTableDiffGenerator(null, table1, true);
			
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
	
		@Test public void getListOfColumnPossiblyTheSame(){
			SqlColumn ABC = new SqlColumn("ABC", MySqlColumnType.BIGINT),
					  ABCD = new SqlColumn("ABCED", MySqlColumnType.BIGINT);
			
			SqlTable tableABC = new SqlTable("t1"),
					 tableABCD = new SqlTable("t1");
			tableABC.addColumn(ABC);
			tableABCD.addColumn(ABCD);
			
			SqlTableDiffGenerator diffGenerator = new SqlTableDiffGenerator(tableABC, tableABCD, true),
									diffGenerator2 = new SqlTableDiffGenerator(tableABCD, tableABC, true);
			System.out.println("List of columns possibly the same :");
			System.out.println(diffGenerator.getListOfColumnPossiblyTheSame(1));
			System.out.println(diffGenerator2.getListOfColumnPossiblyTheSame(1));
		}
	
		@Test public void getIndexesToAddTest(){
			SqlColumn 
			colA = new SqlColumn("A", MySqlColumnType.BIGINT,250,true),
			colB = new SqlColumn("B", MySqlColumnType.BINARY),
			colC = new SqlColumn("C", MySqlColumnType.BOOLEAN),
			colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
			List<SqlColumn> 
					listBC = ListTool.createArrayList(),
					listM = ListTool.createArrayList();
			
			listBC.add(colB);
			listBC.add(colC);
			listM.add(colM);
			SqlIndex index = new SqlIndex("index", listBC),
					index2 = new SqlIndex("index", listM);
			
			SqlTable 
					table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC),
					table2 = new SqlTable("TB").addColumn(colA).addColumn(colM);
			
			table1.addIndex(index);
			table2.addIndex(index2);
			SqlTableDiffGenerator 
					diffBA 		 = new SqlTableDiffGenerator(table2, table1, true),
					diffAB		 = new SqlTableDiffGenerator(table1, table2, true),
					diffNullNull = new SqlTableDiffGenerator(null, null, true),
					diffANull 	 = new SqlTableDiffGenerator(table1, null, true),
					diffNullA 	 = new SqlTableDiffGenerator(null, table1, true);
			
			Assert.assertEquals("index", diffAB.getIndexesToAdd().get(0).getName());
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffAB.getIndexesToAdd().get(0).getColumns(),listM)));
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffBA.getIndexesToAdd().get(0).getColumns(),listBC)));
			Assert.assertTrue(CollectionTool.isEmpty(diffNullNull.getIndexesToAdd()));
			Assert.assertTrue(CollectionTool.isEmpty(diffANull.getIndexesToAdd()));
			Assert.assertTrue(CollectionTool.isEmpty(diffNullA.getIndexesToAdd()));
		}
	
		@Test public void getIndexesToRemove(){
			SqlColumn 
			colA = new SqlColumn("A", MySqlColumnType.BIGINT,250,true),
			colB = new SqlColumn("B", MySqlColumnType.BINARY),
			colC = new SqlColumn("C", MySqlColumnType.BOOLEAN),
			colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
			List<SqlColumn> 
					listBC = ListTool.createArrayList(),
					listM = ListTool.createArrayList();
			
			listBC.add(colB);
			listBC.add(colC);
			listM.add(colM);
			SqlIndex index = new SqlIndex("index", listBC),
					index2 = new SqlIndex("index", listM);
			
			SqlTable 
					table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC),
					table2 = new SqlTable("TB").addColumn(colA).addColumn(colM);
			
			table1.addIndex(index);
			table2.addIndex(index2);
			SqlTableDiffGenerator 
				diffBA 		 = new SqlTableDiffGenerator(table2, table1, true),
				diffAB		 = new SqlTableDiffGenerator(table1, table2, true),
				diffNullNull = new SqlTableDiffGenerator(null, null, true),
				diffANull 	 = new SqlTableDiffGenerator(table1, null, true),
				diffNullA 	 = new SqlTableDiffGenerator(null, table1, true);
	
			Assert.assertEquals("index", diffAB.getIndexesToRemove().get(0).getName());
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffAB.getIndexesToRemove().get(0).getColumns(), listBC)));
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffBA.getIndexesToRemove().get(0).getColumns(), listM)));
			Assert.assertTrue(CollectionTool.isEmpty(diffNullNull.getIndexesToRemove()));
			Assert.assertTrue(CollectionTool.isEmpty(diffANull.getIndexesToRemove()));
			Assert.assertTrue(CollectionTool.isEmpty(diffNullA.getIndexesToRemove()));
		}
	
		@Test public void getColumnsToModifyTest(){
			SqlColumn 
			colA = new SqlColumn("A", MySqlColumnType.BIGINT,20,true),
			colA2 = new SqlColumn("A", MySqlColumnType.INT,10,true),
			colB = new SqlColumn("B", MySqlColumnType.BINARY),
			colC = new SqlColumn("C", MySqlColumnType.BOOLEAN),
			colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
			List<SqlColumn> 
					listBC = ListTool.createArrayList(),
					listM = ListTool.createArrayList();
			
			listBC.add(colB);
			listBC.add(colC);
			listM.add(colM);
			SqlIndex index = new SqlIndex("index", listBC),
					index2 = new SqlIndex("index", listM);
			
			SqlTable 
					table1 = new SqlTable("TA").addColumn(colA).addColumn(colB).addColumn(colC),
					table2 = new SqlTable("TB").addColumn(colA2).addColumn(colM);
			
			table1.addIndex(index);
			table2.addIndex(index2);
			SqlTableDiffGenerator 
				diffBA 		 = new SqlTableDiffGenerator(table2, table1, true),
				diffAB		 = new SqlTableDiffGenerator(table1, table2, true),
				diffNullNull = new SqlTableDiffGenerator(null, null, true),
				diffANull 	 = new SqlTableDiffGenerator(table1, null, true),
				diffNullA 	 = new SqlTableDiffGenerator(null, table1, true);

			System.out.println(diffAB.getColumnsToModify());
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffAB.getColumnsToModify(), ListTool.createArrayList(colA2),  new SqlColumnNameTypeComparator(true))));
			System.out.println(diffBA.getColumnsToModify());
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffBA.getColumnsToModify(), ListTool.createArrayList(colA2),  new SqlColumnNameTypeComparator(true))));
			Assert.assertTrue(CollectionTool.isEmpty(diffNullNull.getColumnsToModify()));
			Assert.assertTrue(CollectionTool.isEmpty(diffANull.getColumnsToModify()));
			Assert.assertTrue(CollectionTool.isEmpty(diffNullA.getColumnsToModify()));
		}
	
	}

}
