package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public class SqlTableDiffGenerator{

	protected SqlTable current, requested;
	protected boolean enforceColumnOrder = false;

	public SqlTableDiffGenerator(SqlTable current, SqlTable requested, boolean enforceColumnOrder){
		this.current = current;
		this.requested = requested;
		this.enforceColumnOrder = enforceColumnOrder;
	}

	public List<SqlColumn> getColumnsToAdd(){
		return ListTool.createArrayList(CollectionTool.minus(requested.getColumns(), current.getColumns()));
	}

	public List<SqlColumn> getColumnsToRemove(){
		return ListTool.createArrayList(CollectionTool.minus(current.getColumns(), requested.getColumns()));
	}
	
	public boolean shouldReorderColumns() {
		return enforceColumnOrder && ! isColumnOrderCorrect();
	}
	
	public boolean isColumnOrderCorrect() {
		return true;//TODO implement
	}

	public List<SqlIndex> getIndexesToAdd(){
		return ListTool.createArrayList(CollectionTool.minus(requested.getIndexes(), current.getIndexes()));
	}

	public List<SqlIndex> getIndexesToRemove(){
		return ListTool.createArrayList(CollectionTool.minus(current.getIndexes(), requested.getIndexes()));
	}

	public boolean isTableModified(){
//		if(CollectionTool.notEmpty(getColumnsToAdd())) { return true; }
//		if(CollectionTool.notEmpty(getColumnsToRemove())) { return true; }
//		return false;
		// if (!current.getName().equals(requested.getName())){
		// System.out.println("The name of the table has changed.");
		//
		// return true;
		// }
		if(isPrimaryKeyModified()){ return true; }

		if(!SetTool.containsSameKeys(SetTool.createTreeSet(current.getColumns()), SetTool.createTreeSet(requested
				.getColumns()))){ return true; }

		if(!SetTool.containsSameKeys(SetTool.createTreeSet(current.getIndexes()), SetTool.createTreeSet(requested
				.getIndexes()))){ return true; }
		return false;
		// if (current.getNumberOfColumns() != requested.getNumberOfColumns()){
		// System.out.println("The number of columnshas changed.");
		// return true;
		// }
		//
		// if (current.getIndexes().size() != requested.getIndexes().size()){
		// System.out.println("The number of indexes has changed.");
		// return true;
		// }
		//
		// // CHANGES IN THE COLUMNS OF THE TABLE
		// Set<SqlColumn> set = SetTool.createTreeSet(current.getColumns());
		// int n = set.size();
		// for (SqlColumn col : requested.getColumns()) {
		// set.add(col); // SAME COLUMNS ARE NOT ADDED TO THE SET
		// }
		// if (set.size() != n) {
		// System.out.println("One of the columns of the table has changed.");
		// return true; // IF A COLUMN HAS BEEN ADDED THEN THERE HAVE BEEN A
		// // CHANGE
		// }
		//
		// return false;
	}

	public boolean isPrimaryKeyModified(){
		if(!CollectionTool.equalsAllElementsInIteratorOrder(current.getPrimaryKey().getColumns(), 
				requested.getPrimaryKey().getColumns())){ 
			return true; 
		}
		return false; // TODO calculate
	}
	
	
	
	// etc

	public SqlTable getRequested() {
		return requested;
	}



	public static class SqlTableDiffGeneratorTester{
		@Test
		public void isTableModifiedTest(){

			SqlColumn 
					idCol = new SqlColumn("id", MySqlColumnType.BIGINT);
			SqlIndex primaryKey1 = new SqlIndex("pk1").addColumn(idCol);
			List<SqlColumn> 
					listA = ListTool.createArrayList(), 
					listA2 = ListTool.createArrayList(), 
					listB = ListTool.createArrayList();
			SqlTable 
					tableA = new SqlTable("A", listA, primaryKey1), 
//					tableB = new SqlTable("B", listB, primaryKey1), 
					tableA2 = new SqlTable("A", listA2,primaryKey1);
//			SqlTableDiffGenerator diffAB = new SqlTableDiffGenerator(tableA, tableB, true);
			SqlTableDiffGenerator diffAA = new SqlTableDiffGenerator(tableA, tableA, true);
			SqlTableDiffGenerator diffAA2 = new SqlTableDiffGenerator(tableA, tableA2, true);
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

		@Test
		public void isPrimaryKeyModifiedTest(){
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
					A2 = new SqlTable("Table 1", list1, primaryKey2);
			
			SqlTableDiffGenerator 
					diffAA = new SqlTableDiffGenerator(A, A, true),
					diffAB = new SqlTableDiffGenerator(A, B, true),
					diffAA2 = new SqlTableDiffGenerator(A,A2,true);
			
			Assert.assertFalse(diffAA.isPrimaryKeyModified());
			Assert.assertTrue(diffAB.isPrimaryKeyModified());
			Assert.assertTrue(diffAA2.isPrimaryKeyModified());
		}
	
		@Test public void getColumnsToAddTest(){
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
					diffAB = new SqlTableDiffGenerator(table1, table2, true);
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffBA.getColumnsToAdd(), listBC)));
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffAB.getColumnsToAdd(), listM)));
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
						diffAB = new SqlTableDiffGenerator(table1, table2, true);
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffBA.getColumnsToRemove(), listM)));
			Assert.assertTrue(CollectionTool.isEmpty(CollectionTool.minus(diffAB.getColumnsToRemove(), listBC)));
			
		}
	}
	
	
}
