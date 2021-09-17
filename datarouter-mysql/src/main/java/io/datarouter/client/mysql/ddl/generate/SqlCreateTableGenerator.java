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
package io.datarouter.client.mysql.ddl.generate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.ddl.domain.SqlIndex;
import io.datarouter.client.mysql.ddl.domain.SqlTable;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.util.string.StringTool;

@Singleton
public class SqlCreateTableGenerator{

	@Inject
	private ServerName serverName;

	public String generateDdl(SqlTable table, String databaseName){
		StringBuilder sb = new StringBuilder("create table ");
		if(StringTool.notEmpty(databaseName)){
			sb.append(databaseName + ".");
		}
		sb.append(table.getName() + " (\n");
		int numberOfColumns = table.getColumns().size();
		SqlColumn col;
		String typeString;
		MysqlColumnType type;
		for(int i = 0; i < numberOfColumns; i++){
			col = table.getColumns().get(i);
			type = col.getType();
			typeString = type.toString().toLowerCase();
			sb.append(" " + col.getName() + " " + typeString);
			if(type.shouldSpecifyLength(col.getMaxLength())){
				sb.append("(" + col.getMaxLength() + ")");
			}
			sb.append(col.getDefaultValueStatement());
			if(col.getAutoIncrement()){
				sb.append(" auto_increment");
			}
			if(i < numberOfColumns - 1){
				sb.append(",\n");
			}
		}

		if(table.hasPrimaryKey()){
			sb.append(",\n");
			sb.append(" primary key (");
			int numberOfColumnsInPrimaryKey = table.getPrimaryKey().getColumnNames().size();
			for(int i = 0; i < numberOfColumnsInPrimaryKey; i++){
				sb.append(table.getPrimaryKey().getColumnNames().get(i));
				if(i != numberOfColumnsInPrimaryKey - 1){
					sb.append(",");
				}
			}
			sb.append(")");
		}
		for(SqlIndex index : table.getUniqueIndexes()){
			sb.append(",\n");
			sb.append(" unique index " + index.getName() + " (");
			boolean appendedAnyCol = false;
			for(String columnName : index.getColumnNames()){
				if(appendedAnyCol){
					 sb.append(", ");
				}
				appendedAnyCol = true;
				sb.append(columnName);
			}
			sb.append(")");
		}

		int numIndexes = table.getIndexes().size();
		if(numIndexes > 0){
			sb.append(",");
		}
		sb.append("\n");
		boolean appendedAnyIndex = false;
		for(SqlIndex index : table.getIndexes()){
			if(appendedAnyIndex){
				sb.append(",\n");
			}
			appendedAnyIndex = true;
			sb.append(" index " + index.getName() + " (");

			boolean appendedAnyIndexCol = false;
			for(String columnName : index.getColumnNames()){
				if(appendedAnyIndexCol){
					sb.append(", ");
				}
				appendedAnyIndexCol = true;
				sb.append(columnName);
			}
			sb.append(")");
		}
		sb.append(")");
		Map<String,String> tableOptions = new LinkedHashMap<>();
		tableOptions.put("engine", table.getEngine().toString());
		tableOptions.put("character set", table.getCharacterSet().toString());
		tableOptions.put("collate", table.getCollation().toString());
		String comment = "created by " + serverName.get() + " ["
				+ DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()) + "]";
		tableOptions.put("comment", "'" + comment + "'");
		tableOptions.put("row_format", table.getRowFormat().getPersistentString());
		tableOptions.entrySet().stream()
				.map(entry -> entry.getKey() + " " + entry.getValue())
				.collect(Collectors.collectingAndThen(Collectors.joining(", "), sb::append));
		sb.append(";");
		return sb.toString();

	}
}
