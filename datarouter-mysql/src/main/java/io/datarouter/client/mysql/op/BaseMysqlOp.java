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
import java.util.Collection;
import java.util.List;

import io.datarouter.client.mysql.MysqlConnectionClient;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.Client;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.op.aware.ConnectionAware;

public abstract class BaseMysqlOp<T> implements TxnOp<T>, ConnectionAware{

	private Datarouter datarouter;
	private List<String> clientNames;
	private Isolation isolation;
	private boolean autoCommit;

	public BaseMysqlOp(Datarouter datarouter, List<String> clientNames, Isolation isolation, boolean autoCommit){
		this.datarouter = datarouter;
		this.clientNames = clientNames;
		this.isolation = isolation;
		this.autoCommit = autoCommit;
	}

	public BaseMysqlOp(Datarouter datarouter, List<String> clientNames){
		this(datarouter, clientNames, Isolation.DEFAULT, false);
	}

	@Override
	public Connection getConnection(String clientName){
		Client client = datarouter.getClientPool().getClient(clientName);
		if(client == null){
			return null;
		}
		if(client instanceof MysqlConnectionClient){
			MysqlConnectionClient mysqlConnectionClient = (MysqlConnectionClient)client;
			Connection connection = mysqlConnectionClient.getExistingConnection();
			return connection;
		}
		return null;
	}

	/*------------------ abstract methods default to no-op ----------------- */

	@Override
	public T runOnce(){
		return null;
	}

	@Override
	public T runOncePerClient(Client client){
		return null;
	}

	@Override
	public T mergeResults(T fromOnce, Collection<T> fromEachClient){
		return fromOnce;
	}

	@Override
	@Deprecated
	public DatarouterClients getDatarouterClients(){
		return datarouter.getClientPool();
	}

	@Override
	public List<String> getClientNames(){
		return clientNames;
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
