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

import java.util.Objects;

public class CharSequenceSqlColumn extends SqlColumn{

	private MysqlCharacterSet characterSet;
	private MysqlCollation collation;

	public CharSequenceSqlColumn(
			String name,
			MysqlColumnType type,
			Integer maxLength,
			Boolean nullable,
			Boolean autoIncrement,
			String defaultValue,
			MysqlCharacterSet characterSet,
			MysqlCollation collation){
		super(name, type, maxLength, nullable, autoIncrement, defaultValue);
		this.characterSet = characterSet;
		this.collation = collation;
	}

	public MysqlCharacterSet getCharacterSet(){
		return characterSet;
	}

	public void setCharacterSet(MysqlCharacterSet characterSet){
		this.characterSet = characterSet;
	}

	public MysqlCollation getCollation(){
		return collation;
	}

	public void setCollation(MysqlCollation collation){
		this.collation = collation;
	}

	@Override
	public StringBuilder appendDataTypeDefinition(StringBuilder sb){
		return super.appendDataTypeDefinition(sb)
				.append(" character set ").append(characterSet)
				.append(" collate ").append(collation);
	}

	@Override
	public boolean equals(Object otherObject){
		if(otherObject == this){
			return true;
		}
		if(!(otherObject instanceof CharSequenceSqlColumn)){
			return false;
		}
		CharSequenceSqlColumn other = (CharSequenceSqlColumn)otherObject;
		return super.equals(other)
				&& Objects.equals(characterSet, other.characterSet)
				&& Objects.equals(collation, other.collation);
	}

	@Override
	public int hashCode(){
		return Objects.hash(super.hashCode(), characterSet, collation);
	}

}
