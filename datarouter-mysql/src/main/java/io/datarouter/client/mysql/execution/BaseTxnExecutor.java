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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.mysql.TxnClientManager;
import io.datarouter.client.mysql.op.TxnOp;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientManager;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.ConnectionHandle;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.util.DatarouterCounters;

public abstract class BaseTxnExecutor<T>
extends BaseClientExecutor
implements TxnExecutor{
	private static final Logger logger = LoggerFactory.getLogger(BaseTxnExecutor.class);

	private final DatarouterClients datarouterClients;
	private final TxnOp<T> parallelTxnOp;

	public BaseTxnExecutor(DatarouterClients datarouterClients, TxnOp<T> parallelTxnOp){
		super(datarouterClients, parallelTxnOp);
		this.datarouterClients = datarouterClients;
		this.parallelTxnOp = parallelTxnOp;
	}

	/*------------------------------ txn code -------------------------------*/

	@Override
	public void beginTxns(){
		ClientId clientId = parallelTxnOp.getClientId();
		ClientManager clientManager = datarouterClients.getClientManager(clientId);
		if(!(clientManager instanceof TxnClientManager)){
			return;
		}
		TxnClientManager txnClientManager = (TxnClientManager)clientManager;
		ConnectionHandle connectionHandle = txnClientManager.getExistingHandle(clientId);
		if(connectionHandle.isOutermostHandle()){
			txnClientManager.beginTxn(clientId, parallelTxnOp.getIsolation(), parallelTxnOp.isAutoCommit());
		}
		ClientType<?,?> clientType = datarouterClients.getClientTypeInstance(clientId);
		DatarouterCounters.incClient(clientType, "beginTxn", clientId.getName(), 1L);
	}

	@Override
	public void commitTxns(){
		ClientId clientId = parallelTxnOp.getClientId();
		ClientManager clientManager = datarouterClients.getClientManager(clientId);
		if(!(clientManager instanceof TxnClientManager)){
			return;
		}
		TxnClientManager txnClientManager = (TxnClientManager)clientManager;
		ConnectionHandle connectionHandle = txnClientManager.getExistingHandle(clientId);
		if(connectionHandle.isOutermostHandle()){
			txnClientManager.commitTxn(clientId);
		}
		ClientType<?,?> clientType = datarouterClients.getClientTypeInstance(clientId);
		DatarouterCounters.incClient(clientType, "commitTxn", clientId.getName(), 1L);
	}

	@Override
	public void rollbackTxns(){
		ClientId clientId = parallelTxnOp.getClientId();
		ClientManager clientManager = datarouterClients.getClientManager(clientId);
		if(!(clientManager instanceof TxnClientManager)){
			return;
		}
		TxnClientManager txnClientManager = (TxnClientManager)clientManager;
		try{
			txnClientManager.rollbackTxn(clientId);
			ClientType<?,?> clientType = datarouterClients.getClientTypeInstance(clientId);
			DatarouterCounters.incClient(clientType, "rollbackTxn", clientId.getName(), 1L);
		}catch(Exception e){
			logger.warn("", e);
			ConnectionHandle connectionHandle = txnClientManager.getExistingHandle(clientId);
			throw new DataAccessException("EXCEPTION THROWN DURING ROLLBACK OF SINGLE TXN:" + connectionHandle, e);
		}
	}

}
