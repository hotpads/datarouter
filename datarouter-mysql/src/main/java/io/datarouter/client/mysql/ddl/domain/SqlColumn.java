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
package io.datarouter.client.mysql.ddl.domain;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.serialization.GsonTool;

public class SqlColumn{

	private static final String NOT_NULL = " not null";

	private final String name;
	private final MysqlColumnType type;
	private final Integer maxLength;
	private final Boolean nullable;
	private final Boolean autoIncrement;
	private final String defaultValue;

	//constructor that specifies the value to override the default value for the column
	public SqlColumn(String name, MysqlColumnType type, Integer maxLength, Boolean nullable, Boolean autoIncrement,
			String defaultValue){
		this.name = name;
		this.type = type;
		this.maxLength = maxLength;
		this.nullable = nullable;
		this.autoIncrement = autoIncrement;
		this.defaultValue = defaultValue;
	}

	public SqlColumn(String name, MysqlColumnType type, Integer maxLength, Boolean nullable, Boolean autoIncrement){
		this(name, type, maxLength, nullable, autoIncrement, null);
	}

	public SqlColumn(String name, MysqlColumnType type){
		this(name, type, null, true, false);
	}

	@Override
	public String toString(){
		return GsonTool.GSON.toJson(this);
	}

	@Override
	public SqlColumn clone(){
		return new SqlColumn(name, type, maxLength, nullable, autoIncrement, defaultValue);
	}

	@Override
	public boolean equals(Object otherObject){
		if(otherObject == this){
			return true;
		}
		if(!(otherObject instanceof SqlColumn)){
			return false;
		}
		SqlColumn other = (SqlColumn)otherObject;
		return Objects.equals(name, other.name)
				&& Objects.equals(type, other.type)
				&& Objects.equals(maxLength, other.maxLength)
				&& Objects.equals(nullable, other.nullable)
				&& Objects.equals(autoIncrement, other.autoIncrement)
				&& Objects.equals(defaultValue, other.defaultValue);
	}

	@Override
	public int hashCode(){
		return Objects.hash(name, type, maxLength, autoIncrement, defaultValue, nullable);
	}

	public String getName(){
		return name;
	}

	public MysqlColumnType getType(){
		return type;
	}

	public String getDefaultValue(){
		return defaultValue;
	}

	public Integer getMaxLength(){
		return maxLength;
	}

	public Boolean getNullable(){
		return nullable;
	}

	public final Boolean getAutoIncrement(){
		return autoIncrement;
	}

	public String getDefaultValueStatement(){
		String toReturn = "";
		if(!getNullable()){
			toReturn += NOT_NULL;
		}
		if(type.isDefaultValueSupported() && getDefaultValue() != null){
			toReturn += " default '" + getDefaultValue() + "'";
		}
		return toReturn;
	}

	public StringBuilder appendDataTypeDefinition(StringBuilder sb){
		sb.append(type.toString().toLowerCase());
		if(type.shouldSpecifyLength(maxLength)){
			sb.append("(").append(maxLength).append(")");
		}
		return sb;
	}

	public StringBuilder makeColumnDefinition(String prefix){
		StringBuilder sb = new StringBuilder(prefix).append(name).append(" ");
		appendDataTypeDefinition(sb);
		sb.append(getDefaultValueStatement());
		if(autoIncrement){
			sb.append(" auto_increment");
		}
		return sb;
	}

	public static class SqlColumnByName{

		private final SqlColumn sqlColumn;

		public SqlColumnByName(SqlColumn sqlColumn){
			this.sqlColumn = sqlColumn;
		}

		public SqlColumn getSqlColumn(){
			return sqlColumn;
		}

		@Override
		public boolean equals(Object obj){
			return sqlColumn.getName().equals(((SqlColumnByName)obj).sqlColumn.getName());
		}

		@Override
		public int hashCode(){
			return sqlColumn.getName().hashCode();
		}

		public static Set<SqlColumnByName> wrap(Collection<SqlColumn> columns){
			return IterableTool.mapToSet(columns, SqlColumnByName::new);
		}

	}

}
