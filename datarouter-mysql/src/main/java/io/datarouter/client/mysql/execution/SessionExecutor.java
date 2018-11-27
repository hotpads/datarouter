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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException;

import io.datarouter.client.mysql.MysqlClient;
import io.datarouter.client.mysql.op.TxnOp;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.storage.client.Client;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.op.executor.impl.SessionExecutorPleaseRetryException;
import io.datarouter.util.StreamTool;
import io.datarouter.util.string.StringTool;

public class SessionExecutor<T>extends BaseTxnExecutor<T> implements Callable<T>{
	private static final Logger logger = LoggerFactory.getLogger(SessionExecutor.class);

	private static final String READ_ONLY_ERROR_MESSAGE = "The MySQL server is running with the --read-only option"
			+ " so it cannot execute this statement";

	public static final Set<Class<?>> ROLLED_BACK_EXCEPTION_SIMPLE_NAMES = ImmutableSet.of(
			MySQLTransactionRollbackException.class);

	private final TxnOp<T> parallelTxnOp;
	private final String traceName;

	public SessionExecutor(DatarouterClients datarouterClients, TxnOp<T> parallelTxnOp, String traceName){
		super(datarouterClients, parallelTxnOp);
		this.parallelTxnOp = parallelTxnOp;
		this.traceName = traceName;
	}

	@Deprecated
	public SessionExecutor(TxnOp<T> parallelTxnOp, String traceName){
		this(parallelTxnOp.getDatarouterClients(), parallelTxnOp, traceName);
	}

	/*-----------------------------------------------------------------------*/

	@Override
	public T call(){
		T onceResult;
		Collection<T> clientResults = new LinkedList<>();
		Collection<Client> clients = getClients();
		try{
			startTrace();
			reserveConnections();
			beginTxns();

			//begin user code
			onceResult = parallelTxnOp.runOnce();
			for(Client client : clients){  //TODO threading
				T clientResult = parallelTxnOp.runOncePerClient(client);
				clientResults.add(clientResult);
			}
			//end user code

			commitTxns();
			return parallelTxnOp.mergeResults(onceResult, clientResults);

		}catch(Exception e){
			if(e instanceof SQLException && e.getMessage().equals(READ_ONLY_ERROR_MESSAGE)){
				List<Connection> badConnections = clients.stream()
						.flatMap(StreamTool.instancesOf(MysqlClient.class))
						.map(MysqlClient::getExistingConnection)
						.collect(Collectors.toList());
				logger.warn("read only mode detected, need to discard the connection(s) {}", badConnections);
			}
			if(wasRolledBackAndShouldRetry(e)){
				//make sure MysqlRollbackRetryingCallable catches this particular exception
				throw new SessionExecutorPleaseRetryException(e);
			}
			try{
				rollbackTxns();
			}catch(RuntimeException exceptionDuringRollback){
				logger.warn("EXCEPTION THROWN DURING TXN ROLL-BACK", exceptionDuringRollback);
				throw e;
			}
			throw e;
		}finally{
			finishTrace();
			try{
				releaseConnections();
			}catch(Exception e){
				//This is an unexpected exception because each individual release is done in a try/catch block
				logger.warn("EXCEPTION THROWN DURING RELEASE OF CONNECTIONS", e);
			}
		}
	}

	/*------------------------------ helper  --------------------------------*/

	private boolean shouldTrace(){
		return StringTool.notEmpty(traceName);
	}

	private void startTrace(){
		if(shouldTrace()){
			TracerTool.startSpan(TracerThreadLocal.get(), traceName);
		}
	}

	private void finishTrace(){
		if(shouldTrace()){
			TracerTool.finishSpan(TracerThreadLocal.get());
		}
	}

	private boolean wasRolledBackAndShouldRetry(Exception exception){
		if(exception == null){
			return false;
		}
		if(ROLLED_BACK_EXCEPTION_SIMPLE_NAMES.contains(exception.getClass())){
			return true;
		}
		Throwable cause = exception.getCause();//unwrap hibernate exception
		return cause != null && ROLLED_BACK_EXCEPTION_SIMPLE_NAMES.contains(cause.getClass());
	}

	public static <T> T run(TxnOp<T> parallelTxnOp){
		return new SessionExecutor<>(parallelTxnOp, parallelTxnOp.getClass().getSimpleName()).call();
	}

}
