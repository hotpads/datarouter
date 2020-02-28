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

import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.datarouter.client.mysql.ddl.generate.SqlTableDiffGenerator;

public class SqlTable{

	private final String name;
	private final List<SqlColumn> columns;
	private final SqlIndex primaryKey;
	private final Set<SqlIndex> indexes;
	private final Set<SqlIndex> uniqueIndexes;
	private final MysqlCollation collation;
	private final MysqlCharacterSet characterSet;
	private final MysqlRowFormat rowFormat;
	private final MysqlTableEngine engine;

	public SqlTable(String name, SqlIndex primaryKey, List<SqlColumn> columns, Set<SqlIndex> indexes,
			Set<SqlIndex> uniqueIndexes, MysqlCharacterSet characterSet, MysqlCollation collation,
			MysqlRowFormat rowFormat, MysqlTableEngine engine){
		this.name = name;
		this.primaryKey = primaryKey;
		this.columns = columns;
		this.indexes = indexes;
		this.uniqueIndexes = uniqueIndexes;
		this.characterSet = characterSet;
		this.collation = collation;
		this.rowFormat = rowFormat;
		this.engine = engine;
	}

	public boolean hasPrimaryKey(){
		return getPrimaryKey() != null && getPrimaryKey().getColumnNames().size() > 0;
	}

	public boolean containsColumn(String columnName){
		for(SqlColumn col : getColumns()){
			if(col.getName().equals(columnName)){
				return true;
			}
		}
		return false;
	}

	public boolean containsIndex(String string){
		for(SqlIndex index : getIndexes()){
			if(index.getName().equals(string)){
				return true;
			}
		}
		return false;
	}

	// text before the first parenthesis, example "show create table Zebra"
	public static String getHeader(String phrase){
		int index = phrase.indexOf('(');
		return phrase.substring(0, index);
	}

	// text inside the parentheses which is a csv separated list of column definitions
	public static String getColumnDefinitionSection(String phrase){
		int index1 = phrase.indexOf('('), index2 = phrase.lastIndexOf(')');
		return phrase.substring(index1 + 1, index2);
	}

	// text after the closing parenthesis. specifies table engine, charset, collation
	public static String getTail(String phrase){
		int index = phrase.lastIndexOf(')');
		return phrase.substring(index + 1);
	}

	@Override
	public boolean equals(Object otherObject){
		if(!(otherObject instanceof SqlTable)){
			return false;
		}
		SqlTable other = (SqlTable)otherObject;
		return !new SqlTableDiffGenerator(this, other).isTableModified();
	}

	@Override
	public int hashCode(){
		return Objects.hash(name, columns, primaryKey, indexes, uniqueIndexes, collation, characterSet, rowFormat,
				engine);
	}

	public String getName(){
		return name;
	}

	public List<SqlColumn> getColumns(){
		return columns;
	}

	public SqlIndex getPrimaryKey(){
		return primaryKey;
	}

	public Set<SqlIndex> getIndexes(){
		return indexes;
	}

	public Set<SqlIndex> getUniqueIndexes(){
		return uniqueIndexes;
	}

	public int getNumberOfColumns(){
		return getColumns().size();
	}

	public MysqlTableEngine getEngine(){
		return engine;
	}

	public MysqlCollation getCollation(){
		return collation;
	}

	public MysqlCharacterSet getCharacterSet(){
		return characterSet;
	}

	public MysqlRowFormat getRowFormat(){
		return rowFormat;
	}

}
