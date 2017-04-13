package com.hotpads.datarouter.client.imp.jdbc.ddl.generate;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class SqlCreateTableGenerator{

	private final SqlTable table;
	private final String databaseName;

	public SqlCreateTableGenerator(SqlTable table){
		this(table, "");
	}

	public SqlCreateTableGenerator(SqlTable table, String databaseName){
		this.table = table;
		this.databaseName = databaseName;
	}

	public String generateDdl(){
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
		sb.append(" engine=" + table.getEngine() + " character set = " + table.getCharacterSet() + " collate "
				+ table.getCollation() + " row_format = " + table.getRowFormat().getPersistentString());
		sb.append(";");
		return sb.toString();

	}
}
