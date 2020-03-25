/**
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

import javax.inject.Singleton;

import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.IterableTool;

@Singleton
public class SpannerTableOperationsGenerator{

	public String getListOfTables(){
		return "SELECT table_name FROM information_schema.tables WHERE table_catalog = '' and table_schema = '';";
	}

	public String getTableSchema(String tableName){
		StringBuilder ddl = new StringBuilder();
		ddl.append("SELECT COLUMN_NAME, ORDINAL_POSITION, IS_NULLABLE, SPANNER_TYPE FROM information_schema.columns "
				+ "WHERE table_name = '");
		ddl.append(tableName);
		ddl.append("'");
		return ddl.toString();
	}

	public String getTableIndexSchema(String tableName){
		StringBuilder ddl = new StringBuilder();
		ddl.append("SELECT INDEX_NAME, INDEX_TYPE FROM information_schema.indexes WHERE table_name = '");
		ddl.append(tableName);
		ddl.append("'");
		return ddl.toString();
	}

	public String getTableIndexColumnsSchema(String tableName, String indexName){
		StringBuilder ddl = new StringBuilder();
		ddl.append("SELECT INDEX_NAME, COLUMN_NAME, ORDINAL_POSITION, IS_NULLABLE, SPANNER_TYPE FROM information_schema"
				+ ".index_columns WHERE table_name = '");
		ddl.append(tableName);
		ddl.append("' AND index_name = '");
		ddl.append(indexName);
		ddl.append("'ORDER BY ORDINAL_POSITION");
		return ddl.toString();
	}

	public String createTable(
			String name,
			List<SpannerColumn> primaryKeyColumns,
			List<SpannerColumn> nonKeyColumns,
			String parentTableName){
		StringBuilder ddl = new StringBuilder();
		ddl.append("CREATE TABLE ");
		ddl.append(name);
		ddl.append("(");
		List<SpannerColumn> columns = ListTool.concatenate(primaryKeyColumns, nonKeyColumns);
		ddl.append(String.join(", ", IterableTool.map(columns, SpannerColumn::generateColumnDef)));
		ddl.append(") PRIMARY KEY (");
		primaryKeyColumns.forEach(col -> {
			ddl.append(col.getName());
			ddl.append(", ");
		});
		ddl.delete(ddl.length() - 2, ddl.length() - 1);
		ddl.append(")");
		if(parentTableName != null){
			ddl.append(", INTERLEAVE IN PARENT ");
			ddl.append(parentTableName);
			ddl.append(" ON DELETE CASCADE");
		}
		return ddl.toString();
	}

	public String createIndex(
			String tableName,
			String indexName,
			List<SpannerColumn> keyColumns,
			List<SpannerColumn> nonKeyColumns,
			Boolean unique){
		StringBuilder ddl = new StringBuilder();
		ddl.append("CREATE ");
		if(unique){
			ddl.append("UNIQUE ");
		}
		ddl.append("INDEX ");
		ddl.append(indexName);
		ddl.append(" ON ");
		ddl.append(tableName);
		ddl.append("(");
		ddl.append(String.join(", ", IterableTool.map(keyColumns, SpannerColumn::getName)));
		ddl.append(")");
		if(!nonKeyColumns.isEmpty()){
			ddl.append(" STORING (");
			ddl.append(String.join(", ", IterableTool.map(nonKeyColumns, SpannerColumn::getName)));
			ddl.append(")");
		}
		return ddl.toString();
	}

	public String addColumns(String tableName, SpannerColumn colToAdd){
		StringBuilder ddl = new StringBuilder();
		ddl.append("ALTER TABLE ");
		ddl.append(tableName);
		ddl.append(" ADD COLUMN ");
		ddl.append(colToAdd.generateColumnDef());
		return ddl.toString();
	}

	public String dropColumns(String tableName, SpannerColumn colToRemove){
		StringBuilder ddl = new StringBuilder();
		ddl.append("ALTER TABLE ");
		ddl.append(tableName);
		ddl.append(" DROP COLUMN ");
		ddl.append(colToRemove.getName());
		return ddl.toString();
	}

	public String alterColumns(String tableName, SpannerColumn colToAlter){
		StringBuilder ddl = new StringBuilder();
		ddl.append("ALTER TABLE ");
		ddl.append(tableName);
		ddl.append(" ALTER COLUMN ");
		ddl.append(colToAlter.getName());
		return ddl.toString();
	}

	public String dropIndex(String indexName){
		StringBuilder ddl = new StringBuilder();
		ddl.append("DROP INDEX ");
		ddl.append(indexName);
		return ddl.toString();
	}

}
