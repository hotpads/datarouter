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

public class MysqlLiveTableOptions{

	private final MysqlCharacterSet characterSet;
	private final MysqlCollation collation;

	public MysqlLiveTableOptions(MysqlCharacterSet characterSet, MysqlCollation collation){
		this.characterSet = characterSet;
		this.collation = collation;
	}

	public MysqlCharacterSet getCharacterSet(){
		return characterSet;
	}

	public MysqlCollation getCollation(){
		return collation;
	}

	@Override
	public int hashCode(){
		return Objects.hash(characterSet, collation);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		MysqlLiveTableOptions other = (MysqlLiveTableOptions)obj;
		return Objects.equals(other.characterSet, characterSet)
				&& Objects.equals(other.collation, collation);
	}

	public String toString(String prefix){
		return prefix + "CharacterSet=" + characterSet + " " + prefix + "Collation=" + collation;
	}

}
