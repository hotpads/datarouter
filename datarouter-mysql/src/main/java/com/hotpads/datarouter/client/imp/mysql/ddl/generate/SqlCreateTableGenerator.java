package com.hotpads.datarouter.client.imp.mysql.ddl.generate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.SqlTable;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrStringTool;

@Singleton
public class SqlCreateTableGenerator{

	private DatarouterProperties datarouterProperties;

	@Inject
	public SqlCreateTableGenerator(DatarouterProperties datarouterProperties){
		this.datarouterProperties = datarouterProperties;
	}

	public String generateDdl(SqlTable table, String databaseName){
		StringBuilder sb = new StringBuilder("create table ");
		if(!DrStringTool.isEmpty(databaseName)){
			sb.append(databaseName + ".");
		}
		sb.append(table.getName() + " (\n");
		int numberOfColumns = table.getColumns().size();
		SqlColumn col;
		String typeString;
		MySqlColumnType type;
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
		for(SqlIndex index : DrIterableTool.nullSafe(table.getUniqueIndexes())){
			sb.append(",\n");
			sb.append(" unique index " + index.getName() + " (");
			boolean appendedAnyCol = false;
			for(String columnName: index.getColumnNames()){
				if(appendedAnyCol){
					 sb.append(", ");
				}
				appendedAnyCol = true;
				sb.append(columnName);
			}
			sb.append(")");
		}

		int numIndexes = DrCollectionTool.size(table.getIndexes());
		if(numIndexes > 0){
			sb.append(",");
		}
		sb.append("\n");
		boolean appendedAnyIndex = false;
		for(SqlIndex index : DrIterableTool.nullSafe(table.getIndexes())){
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
		String comment = "created by " + datarouterProperties.getServerName() + " ["
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
