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

import io.datarouter.client.mysql.TxnClient;
import io.datarouter.client.mysql.op.TxnOp;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.storage.client.Client;
import io.datarouter.storage.client.ConnectionHandle;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.collection.CollectionTool;

public abstract class BaseTxnExecutor<T>
extends BaseClientExecutor
implements TxnExecutor{
	private static final Logger logger = LoggerFactory.getLogger(BaseTxnExecutor.class);

	private final TxnOp<T> parallelTxnOp;

	public BaseTxnExecutor(DatarouterClients datarouterClients, TxnOp<T> parallelTxnOp){
		super(datarouterClients, parallelTxnOp);
		this.parallelTxnOp = parallelTxnOp;
	}

	/*------------------------------ txn code -------------------------------*/

	@Override
	public void beginTxns(){
		for(Client client : CollectionTool.nullSafe(getClients())){
			if(!(client instanceof TxnClient)){
				continue;
			}
			TxnClient txnClient = (TxnClient)client;
			ConnectionHandle connectionHandle = txnClient.getExistingHandle();
			if(connectionHandle.isOutermostHandle()){
				txnClient.beginTxn(parallelTxnOp.getIsolation(), parallelTxnOp.isAutoCommit());
			}
			DatarouterCounters.incClient(txnClient.getType(), "beginTxn", txnClient.getName(), 1L);
		}
	}

	@Override
	public void commitTxns(){
		for(Client client : CollectionTool.nullSafe(getClients())){
			if(!(client instanceof TxnClient)){
				continue;
			}
			TxnClient txnClient = (TxnClient)client;
			ConnectionHandle connectionHandle = txnClient.getExistingHandle();
			if(connectionHandle.isOutermostHandle()){
				txnClient.commitTxn();
			}
			DatarouterCounters.incClient(txnClient.getType(), "commitTxn", txnClient.getName(), 1L);
		}
	}

	@Override
	public void rollbackTxns(){
		for(Client client : CollectionTool.nullSafe(getClients())){
			if(!(client instanceof TxnClient)){
				continue;
			}
			TxnClient txnClient = (TxnClient)client;
			try{
				txnClient.rollbackTxn();
				DatarouterCounters.incClient(txnClient.getType(), "rollbackTxn", txnClient.getName(), 1L);
			}catch(Exception e){
				logger.warn("", e);
				throw new DataAccessException("EXCEPTION THROWN DURING ROLLBACK OF SINGLE TXN:" + txnClient
						.getExistingHandle(), e);
			}
		}
	}

}
