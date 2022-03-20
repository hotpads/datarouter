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
package io.datarouter.storage.node.tableconfig;

import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.string.StringTool;

public class ClientTableEntityPrefixNameWrapper implements Comparable<ClientTableEntityPrefixNameWrapper>{

	private final String clientName;
	private final String tableName;
	private final String subEntityPrefix;

	public ClientTableEntityPrefixNameWrapper(PhysicalNode<?,?,?> node){
		this(node.getFieldInfo().getClientId().getName(),
				node.getFieldInfo().getTableName(),
				node.getFieldInfo().getEntityNodePrefix());
	}

	public ClientTableEntityPrefixNameWrapper(String clientName, String tableName, String subEntityPrefix){
		this.clientName = clientName;
		this.tableName = tableName;
		this.subEntityPrefix = subEntityPrefix;
	}

	@Override
	public String toString(){
		String clientAndTable = clientName + "." + tableName;
		return StringTool.isEmpty(subEntityPrefix) ? clientAndTable : clientAndTable + "." + subEntityPrefix;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + (clientName == null ? 0 : clientName.hashCode());
		result = prime * result + (subEntityPrefix == null ? 0 : subEntityPrefix.hashCode());
		result = prime * result + (tableName == null ? 0 : tableName.hashCode());
		return result;
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
		ClientTableEntityPrefixNameWrapper other = (ClientTableEntityPrefixNameWrapper)obj;
		if(clientName == null){
			if(other.clientName != null){
				return false;
			}
		}else if(!clientName.equals(other.clientName)){
			return false;
		}
		if(subEntityPrefix == null){
			if(other.subEntityPrefix != null){
				return false;
			}
		}else if(!subEntityPrefix.equals(other.subEntityPrefix)){
			return false;
		}
		if(tableName == null){
			if(other.tableName != null){
				return false;
			}
		}else if(!tableName.equals(other.tableName)){
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(ClientTableEntityPrefixNameWrapper that){
		int diff = ComparableTool.nullFirstCompareTo(this.clientName, that.clientName);
		if(diff != 0){
			return diff;
		}
		diff = ComparableTool.nullFirstCompareTo(this.tableName, that.tableName);
		if(diff != 0){
			return diff;
		}
		return ComparableTool.nullFirstCompareTo(this.subEntityPrefix, that.subEntityPrefix);
	}

	public boolean hasSubEntityPrefix(){
		return StringTool.notEmpty(subEntityPrefix);
	}

	public String getClientName(){
		return clientName;
	}

	public String getTableName(){
		return tableName;
	}

	public String getSubEntityPrefix(){
		return subEntityPrefix;
	}

}
