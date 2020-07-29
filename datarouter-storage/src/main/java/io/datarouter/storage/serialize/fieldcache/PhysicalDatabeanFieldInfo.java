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
package io.datarouter.storage.serialize.fieldcache;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.tableconfig.NodewatchConfiguration;
import io.datarouter.util.string.StringTool;

public class PhysicalDatabeanFieldInfo<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends DatabeanFieldInfo<PK,D,F>{

	private final ClientId clientId;
	private final String tableName;
	private final String nodeName;
	private final NodewatchConfiguration nodewatchConfiguration;
	private final boolean disableForcePrimary;
	private final boolean isSystemTable;

	public PhysicalDatabeanFieldInfo(NodeParams<PK,D,F> params){
		super(params);
		this.clientId = params.getClientId();
		boolean entity = StringTool.notEmpty(params.getEntityNodePrefix());
		if(entity){
			this.tableName = params.getPhysicalName();
		}else if(StringTool.notEmpty(params.getPhysicalName())){
			//explicitly set tableName.  do after entity check since that also sets a table name
			this.tableName = params.getPhysicalName();
		}else{//default to using the databean's name as the table name
			this.tableName = params.getDatabeanSupplier().get().getDatabeanName();
		}
		String nodeName = clientId.getName() + "." + tableName;
		if(params.getEntityNodePrefix() != null){
			nodeName += "." + params.getEntityNodePrefix();
		}
		this.nodeName = nodeName;
		this.nodewatchConfiguration = params.getTableConfiguration();
		this.disableForcePrimary = params.getDisableForcePrimary();
		this.isSystemTable = params.getIsSystemTable();
	}

	public ClientId getClientId(){
		return clientId;
	}

	public String getTableName(){
		return tableName;
	}

	public String getNodeName(){
		return nodeName;
	}

	public NodewatchConfiguration getTableConfiguration(){
		return nodewatchConfiguration;
	}

	public boolean getDisableForcePrimary(){
		return disableForcePrimary;
	}

	public boolean getIsSystemTable(){
		return isSystemTable;
	}

	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof PhysicalDatabeanFieldInfo)){
			return false;
		}
		return nodeName.equals(((PhysicalDatabeanFieldInfo<?,?,?>)obj).nodeName);
	}

	@Override
	public int hashCode(){
		return nodeName.hashCode();
	}

	@Override
	public String toString(){
		return nodeName;
	}

}
