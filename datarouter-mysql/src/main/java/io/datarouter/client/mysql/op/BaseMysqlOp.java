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
package io.datarouter.client.mysql.op;

import java.sql.Connection;

import io.datarouter.client.mysql.MysqlConnectionClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientManager;
import io.datarouter.storage.client.DatarouterClients;

public abstract class BaseMysqlOp<T>{

	private final DatarouterClients datarouterClients;
	private final Isolation isolation;
	private final boolean autoCommit;
	private final ClientId clientId;

	public BaseMysqlOp(DatarouterClients datarouterClients, ClientId clientId, Isolation isolation, boolean autoCommit){
		this.datarouterClients = datarouterClients;
		this.clientId = clientId;
		this.isolation = isolation;
		this.autoCommit = autoCommit;
	}

	public BaseMysqlOp(DatarouterClients datarouterClients, ClientId clientId){
		this(datarouterClients, clientId, Isolation.DEFAULT, false);
	}

	public abstract T runOnce();

	public Connection getConnection(){
		ClientManager clientManager = datarouterClients.getClientManager(clientId);
		if(clientManager instanceof MysqlConnectionClientManager mysqlConnectionClientManager){
			return mysqlConnectionClientManager.getExistingConnection(clientId);
		}
		return null;
	}

	public Isolation getIsolation(){
		return isolation;
	}

	public boolean isAutoCommit(){
		return autoCommit;
	}

	public ClientId getClientId(){
		return clientId;
	}

}
