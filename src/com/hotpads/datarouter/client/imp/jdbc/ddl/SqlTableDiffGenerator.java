package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public class SqlTableDiffGenerator {

	protected SqlTable current, requested;

	public SqlTableDiffGenerator(SqlTable current, SqlTable requested) {
		this.current = current;
		this.requested = requested;
	}

	public List<SqlColumn> getColumnsToAdd() {
		List<SqlColumn> columnsToAdd = ListTool.createArrayList();

		return columnsToAdd;
	}

	public List<SqlColumn> getColumnsToRemove() {
		List<SqlColumn> columnsToAdd = ListTool.createArrayList();

		return columnsToAdd;
	}

	public List<SqlColumn> getIndexesToAdd() {
		List<SqlColumn> columnsToAdd = ListTool.createArrayList();

		return columnsToAdd;
	}

	public List<SqlColumn> getIndexesToRemove() {
		List<SqlColumn> columnsToAdd = ListTool.createArrayList();

		return columnsToAdd;
	}

	public boolean isTableModified() {
		if (!current.getName().equals(requested)){
			return true;
		}
		if (current.getNumberOfColumns() != requested.getNumberOfColumns()){
			return true;
		}

		if (current.getIndexes().size() == requested.getIndexes().size()){
			return true;
		}

		// CHANGES IN THE COLUMNS OF THE TABLE
				Set<SqlColumn> set = SetTool.createTreeSet(current.getColumns());
				int n = set.size();
				for (SqlColumn col : requested.getColumns()) {
					set.add(col); // SAME COLUMNS ARE NOT ADDED TO THE SET
				}
				if (set.size() != n) {
					return true; // IF A COLUMN HAS BEEN ADDED THEN THERE HAVE BEEN A
									// CHANGE
				}
				
		return false;
	}

	public boolean isPrimaryKeyModified() {

		if (current.getPrimaryKey().getNumberOfColumns() != requested
				.getPrimaryKey().getNumberOfColumns()) {
			return true;
		}

		// CHANGES IN THE PRIMARY KEY COLUMNS
		Set<SqlColumn> set = SetTool.createTreeSet(current.getPrimaryKey().getColumns());
		int n = set.size();
		for (SqlColumn col : requested.getPrimaryKey().getColumns()) {
			set.add(col); // SAME COLUMNS ARE NOT ADDED TO THE SET
		}
		if (set.size() != n) {
			return true; // IF A COLUMN HAS BEEN ADDED THEN THERE HAVE BEEN A
							// CHANGE
		}
		return false; // TODO calculate
	}
	

	// etc

	public static class TestSqlTableDiffGenerator {
		@Test
		public void testIsTableModified() {
			// TABLES WITH DIFFERENT NAME
			
			List<SqlColumn> list = ListTool.createArrayList();
			SqlTable tableA = new SqlTable("A",list), tableB = new SqlTable("B",list);
			SqlTableDiffGenerator diffAB = new SqlTableDiffGenerator(tableA, tableB), diffAA = new SqlTableDiffGenerator(tableA, tableA);
			Assert.assertFalse(diffAA.isTableModified());
			Assert.assertTrue(diffAB.isTableModified());
			// TABLES ...
		}
	}
}
