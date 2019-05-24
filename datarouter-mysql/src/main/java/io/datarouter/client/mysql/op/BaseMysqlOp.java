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
package io.datarouter.client.mysql.op;

import java.sql.Connection;

import io.datarouter.client.mysql.MysqlConnectionClientManager;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientManager;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.op.aware.ConnectionAware;

public abstract class BaseMysqlOp<T> implements TxnOp<T>, ConnectionAware{

	private final Datarouter datarouter;
	private final Isolation isolation;
	private final boolean autoCommit;
	protected final ClientId clientId;

	public BaseMysqlOp(Datarouter datarouter, ClientId clientId, Isolation isolation, boolean autoCommit){
		this.datarouter = datarouter;
		this.clientId = clientId;
		this.isolation = isolation;
		this.autoCommit = autoCommit;
	}

	public BaseMysqlOp(Datarouter datarouter, ClientId clientId){
		this(datarouter, clientId, Isolation.DEFAULT, false);
	}

	@Override
	public Connection getConnection(ClientId clientId){
		ClientManager clientManager = datarouter.getClientPool().getClientManager(clientId);
		if(clientManager instanceof MysqlConnectionClientManager){
			MysqlConnectionClientManager mysqlConnectionClientManager = (MysqlConnectionClientManager)clientManager;
			return mysqlConnectionClientManager.getExistingConnection(clientId);
		}
		return null;
	}

	/*------------------ abstract methods default to no-op ----------------- */

	@Override
	@Deprecated
	public DatarouterClients getDatarouterClients(){
		return datarouter.getClientPool();
	}

	@Override
	public ClientId getClientId(){
		return clientId;
	}

	@Override
	public Isolation getIsolation(){
		return isolation;
	}

	@Override
	public boolean isAutoCommit(){
		return autoCommit;
	}

}
