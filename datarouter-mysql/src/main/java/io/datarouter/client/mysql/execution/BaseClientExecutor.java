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
package io.datarouter.client.mysql.execution;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.mysql.op.ClientOp;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.storage.client.Client;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.client.type.ConnectionClient;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.collection.CollectionTool;

public abstract class BaseClientExecutor implements ClientExecutor{
	private static final Logger logger = LoggerFactory.getLogger(BaseClientExecutor.class);

	private final DatarouterClients datarouterClients;
	private final ClientOp parallelClientOp;

	public BaseClientExecutor(DatarouterClients datarouterClients, ClientOp parallelClientOp){
		this.datarouterClients = datarouterClients;
		this.parallelClientOp = parallelClientOp;
	}


	@Override
	public List<Client> getClients(){
		return datarouterClients.getClients(parallelClientOp.getClientNames());
	}

	/*------------------------------ txn code -------------------------------*/

	@Override
	public void reserveConnections(){
		for(Client client : CollectionTool.nullSafe(getClients())){
			if(!(client instanceof ConnectionClient)){
				continue;
			}
			ConnectionClient connectionClient = (ConnectionClient)client;
			connectionClient.reserveConnection();
			DatarouterCounters.incClient(connectionClient.getType(), "reserveConnection", connectionClient.getName(),
					1L);
		}
	}

	@Override
	public void releaseConnections(){
		for(Client client : CollectionTool.nullSafe(getClients())){
			if(!(client instanceof ConnectionClient)){
				continue;
			}
			ConnectionClient connectionClient = (ConnectionClient)client;
			try{
				connectionClient.releaseConnection();
				DatarouterCounters.incClient(connectionClient.getType(), "releaseConnection", connectionClient
						.getName(), 1L);
			}catch(Exception e){
				logger.warn("", e);
				throw new DataAccessException("EXCEPTION THROWN DURING RELEASE OF SINGLE CONNECTION, handle now=:"
						+ connectionClient.getExistingHandle(), e);
			}
		}
	}

}
