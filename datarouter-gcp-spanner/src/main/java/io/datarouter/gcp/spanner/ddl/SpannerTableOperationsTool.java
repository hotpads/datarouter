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
package io.datarouter.gcp.spanner.ddl;

import java.util.List;

import io.datarouter.scanner.Scanner;

public class SpannerTableOperationsTool{

	public static final String COLUMN_NAME = "COLUMN_NAME",
			INDEX_NAME = "INDEX_NAME",
			IS_NULLABLE = "IS_NULLABLE",
			ORDINAL_POSITION = "ORDINAL_POSITION",
			PRIMARY_KEY = "PRIMARY_KEY",
			SPANNER_TYPE = "SPANNER_TYPE";

	public static String getListOfTables(){
		return "SELECT table_name, spanner_state FROM information_schema.tables WHERE table_catalog = ''"
				+ " and table_schema = '';";
	}

	public static String getTableSchema(String tableName){
		var ddl = new StringBuilder();
		ddl.append("SELECT COLUMN_NAME, ORDINAL_POSITION, IS_NULLABLE, SPANNER_TYPE FROM information_schema.columns "
				+ "WHERE table_name = '");
		ddl.append(tableName);
		ddl.append("'");
		return ddl.toString();
	}

	public static String getTableIndexSchema(String tableName){
		var ddl = new StringBuilder();
		ddl.append("SELECT INDEX_NAME, INDEX_TYPE FROM information_schema.indexes WHERE table_name = '");
		ddl.append(tableName);
		ddl.append("'");
		return ddl.toString();
	}

	public static String getTableIndexColumnsSchema(String tableName, String indexName){
		var ddl = new StringBuilder();
		ddl.append("SELECT INDEX_NAME, COLUMN_NAME, ORDINAL_POSITION, IS_NULLABLE, SPANNER_TYPE FROM information_schema"
				+ ".index_columns WHERE table_name = '");
		ddl.append(tableName);
		ddl.append("' AND index_name = '");
		ddl.append(indexName);
		ddl.append("'ORDER BY ORDINAL_POSITION");
		return ddl.toString();
	}

	public static String createTable(
			String name,
			List<SpannerColumn> primaryKeyColumns,
			List<SpannerColumn> nonKeyColumns,
			String parentTableName){
		var ddl = new StringBuilder();
		ddl.append("CREATE TABLE ");
		ddl.append(name);
		ddl.append("(");
		List<SpannerColumn> columns = Scanner.concat(primaryKeyColumns, nonKeyColumns).list();
		ddl.append(String.join(", ", Scanner.of(columns)
				.map(column -> column.generateColumnDef(false))
				.list()));
		ddl.append(") PRIMARY KEY (");
		ddl.append(String.join(", ", primaryKeyColumns.stream()
				.map(SpannerColumn::getName)
				.toList()));
		ddl.append(")");
		if(parentTableName != null){
			ddl.append(", INTERLEAVE IN PARENT ");
			ddl.append(parentTableName);
			ddl.append(" ON DELETE CASCADE");
		}
		return ddl.toString();
	}

	public static String createIndex(
			String tableName,
			String indexName,
			List<SpannerColumn> keyColumns,
			List<SpannerColumn> nonKeyColumns,
			Boolean unique){
		var ddl = new StringBuilder();
		ddl.append("CREATE ");
		if(unique){
			ddl.append("UNIQUE ");
		}
		ddl.append("INDEX ");
		ddl.append(indexName);
		ddl.append(" ON ");
		ddl.append(tableName);
		ddl.append("(");
		ddl.append(String.join(", ", Scanner.of(keyColumns).map(SpannerColumn::getName).list()));
		ddl.append(")");
		if(!nonKeyColumns.isEmpty()){
			ddl.append(" STORING (");
			ddl.append(String.join(", ", Scanner.of(nonKeyColumns).map(SpannerColumn::getName).list()));
			ddl.append(")");
		}
		return ddl.toString();
	}

	public static String addColumns(String tableName, SpannerColumn colToAdd){
		var ddl = new StringBuilder();
		ddl.append("ALTER TABLE ");
		ddl.append(tableName);
		ddl.append(" ADD COLUMN ");
		ddl.append(colToAdd.generateColumnDef(false));
		return ddl.toString();
	}

	public static String dropColumns(String tableName, SpannerColumn colToRemove){
		var ddl = new StringBuilder();
		ddl.append("ALTER TABLE ");
		ddl.append(tableName);
		ddl.append(" DROP COLUMN ");
		ddl.append(colToRemove.getName());
		return ddl.toString();
	}

	public static String alterColumns(String tableName, SpannerColumn colToAlter){
		var ddl = new StringBuilder();
		ddl.append("ALTER TABLE ");
		ddl.append(tableName);
		ddl.append(" ALTER COLUMN ");
		ddl.append(colToAlter.getName());
		return ddl.toString();
	}

	public static String dropIndex(String indexName){
		var ddl = new StringBuilder();
		ddl.append("DROP INDEX ");
		ddl.append(indexName);
		return ddl.toString();
	}

}
