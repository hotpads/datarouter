/*
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
package io.datarouter.client.mysql.test.client.imp.mysql.ddl.generate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.client.mysql.ddl.domain.MysqlCharacterSet;
import io.datarouter.client.mysql.ddl.domain.MysqlCollation;
import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.MysqlRowFormat;
import io.datarouter.client.mysql.ddl.domain.MysqlTableEngine;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.ddl.domain.SqlIndex;
import io.datarouter.client.mysql.ddl.domain.SqlTable;
import io.datarouter.client.mysql.ddl.generate.SqlTableDiffGenerator;

public class SqlTableDiffGeneratorTests{

	private static final String ID_STRING = "id";
	private static final SqlColumn id = new SqlColumn(ID_STRING, MysqlColumnType.BIGINT);
	private static final SqlIndex pk = SqlIndex.createPrimaryKey(List.of(ID_STRING));

	@Test
	public void testSameTableModified(){
		SqlTable table = new SqlTable(
				"degemer",
				pk,
				List.of(id),
				Set.of(),
				Set.of(),
				MysqlCharacterSet.utf8mb4,
				MysqlCollation.utf8mb4_bin,
				MysqlRowFormat.COMPACT,
				MysqlTableEngine.INNODB);
		Assert.assertFalse(new SqlTableDiffGenerator(table, table).isTableModified());
	}

	@Test
	public void testIndexModifiedWithSameName(){
		String column1 = "foo";
		String column2 = "bar";
		var previousIndex = new SqlIndex("foo", List.of(column1));
		var nextIndex = new SqlIndex("foo", List.of(column1, column2));
		Set<SqlIndex> previousIndexes = new HashSet<>(List.of(previousIndex));
		Set<SqlIndex> nextIndexes = new HashSet<>(List.of(nextIndex));
		var previousTable = new SqlTable(
				"degemer",
				pk,
				List.of(id),
				previousIndexes,
				Set.of(),
				MysqlCharacterSet.utf8mb4,
				MysqlCollation.utf8mb4_bin,
				MysqlRowFormat.COMPACT,
				MysqlTableEngine.INNODB);
		var nextTable = new SqlTable(
				"degemer",
				pk,
				List.of(id),
				nextIndexes,
				Set.of(),
				MysqlCharacterSet.utf8mb4,
				MysqlCollation.utf8mb4_bin,
				MysqlRowFormat.COMPACT,
				MysqlTableEngine.INNODB);
		var diff = new SqlTableDiffGenerator(previousTable, nextTable);
		Assert.assertTrue(diff.isTableModified());
		Assert.assertTrue(diff.isIndexesModified());
		Assert.assertEquals(diff.getIndexesToAdd(), nextIndexes);
		Assert.assertEquals(diff.getIndexesToRemove(), previousIndexes);
	}

}
