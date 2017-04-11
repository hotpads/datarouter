package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlRowFormat;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlTableEngine;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;

public class SqlTableDiffGeneratorTests{

	private static final SqlColumn id = new SqlColumn("id", MySqlColumnType.BIGINT);
	private static final SqlIndex pk = SqlIndex.createPrimaryKey(Arrays.asList(id));

	@Test
	public void testSameTableModified(){
		SqlTable table = new SqlTable("degemer", pk, Arrays.asList(id), Collections.emptySet(), Collections.emptySet(),
				MySqlCharacterSet.utf8mb4, MySqlCollation.utf8mb4_bin, MySqlRowFormat.COMPACT, MySqlTableEngine.INNODB);
		Assert.assertFalse(new SqlTableDiffGenerator(table, table).isTableModified());
	}

	@Test
	public void testIndexModifiedWithSameName(){
		SqlColumn column1 = new SqlColumn("foo", MySqlColumnType.BIGINT);
		SqlColumn column2 = new SqlColumn("bar", MySqlColumnType.BIGINT);
		SqlIndex previousIndex = new SqlIndex("foo", Arrays.asList(column1));
		SqlIndex nextIndex = new SqlIndex("foo", Arrays.asList(column1, column2));
		Set<SqlIndex> previousIndexes = new HashSet<>(Arrays.asList(previousIndex));
		Set<SqlIndex> nextIndexes = new HashSet<>(Arrays.asList(nextIndex));
		SqlTable previousTable = new SqlTable("degemer", pk, Arrays.asList(id), previousIndexes, Collections.emptySet(),
				MySqlCharacterSet.utf8mb4, MySqlCollation.utf8mb4_bin, MySqlRowFormat.COMPACT, MySqlTableEngine.INNODB);
		SqlTable nextTable = new SqlTable("degemer", pk, Arrays.asList(id), nextIndexes, Collections.emptySet(),
				MySqlCharacterSet.utf8mb4, MySqlCollation.utf8mb4_bin, MySqlRowFormat.COMPACT, MySqlTableEngine.INNODB);
		SqlTableDiffGenerator diff = new SqlTableDiffGenerator(previousTable, nextTable);
		Assert.assertTrue(diff.isTableModified());
		Assert.assertTrue(diff.isIndexesModified());
		Assert.assertEquals(diff.getIndexesToAdd(), nextIndexes);
		Assert.assertEquals(diff.getIndexesToRemove(), previousIndexes);

	}

}
