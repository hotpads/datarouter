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
package io.datarouter.storage.client;

import java.util.Objects;

public class ClientTableNodeNames{

	private final ClientId clientId;
	private final String tableName;
	private final String nodeName;

	public ClientTableNodeNames(ClientId clientId, String tableName, String nodeName){
		this.clientId = clientId;
		this.tableName = tableName;
		this.nodeName = nodeName;
	}

	public ClientId getClientId(){
		return clientId;
	}

	public String getClientName(){
		return clientId.getName();
	}

	public String getTableName(){
		return tableName;
	}

	public String getNodeName(){
		return nodeName;
	}

	@Override
	public int hashCode(){
		return Objects.hash(clientId, nodeName, tableName);
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
		ClientTableNodeNames other = (ClientTableNodeNames)obj;
		return Objects.equals(clientId, other.clientId)
				&& Objects.equals(nodeName, other.nodeName)
				&& Objects.equals(tableName, other.tableName);
	}


	@Override
	public String toString(){
		return "ClientTableNodeNames [clientId=" + clientId + ", tableName=" + tableName + ", nodeName=" + nodeName
				+ "]";
	}

}
