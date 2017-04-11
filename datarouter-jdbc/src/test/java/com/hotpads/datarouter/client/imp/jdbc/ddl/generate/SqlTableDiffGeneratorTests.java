package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

import java.util.Arrays;
import java.util.Collections;

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

	@Test
	public void testSameTableModified(){
		SqlColumn id = new SqlColumn("id", MySqlColumnType.BIGINT);
		SqlIndex pk = SqlIndex.createPrimaryKey(Arrays.asList(id));
		SqlTable table = new SqlTable("degemer", pk, Arrays.asList(id), Collections.emptySet(), Collections.emptySet(),
				MySqlCharacterSet.utf8mb4, MySqlCollation.utf8mb4_bin, MySqlRowFormat.COMPACT, MySqlTableEngine.INNODB);
		Assert.assertFalse(new SqlTableDiffGenerator(table, table).isTableModified());
	}

}
