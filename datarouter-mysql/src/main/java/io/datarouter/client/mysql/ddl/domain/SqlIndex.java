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
package io.datarouter.client.mysql.ddl.domain;

import java.util.List;
import java.util.Objects;

public class SqlIndex{

	private final String name;
	private final List<String> columnNames;

	public SqlIndex(String name, List<String> columns){
		this.name = name;
		this.columnNames = columns;
	}

	public String getName(){
		return name;
	}

	public List<String> getColumnNames(){
		return columnNames;
	}

	@Override
	public int hashCode(){
		return Objects.hash(name, columnNames);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(!(obj instanceof SqlIndex)){
			return false;
		}
		SqlIndex other = (SqlIndex)obj;
		return Objects.equals(name, other.name)
				&& Objects.equals(columnNames, other.columnNames);
	}

	public static SqlIndex createPrimaryKey(List<String> columns){
		return new SqlIndex("PRIMARY", columns);
	}

}
