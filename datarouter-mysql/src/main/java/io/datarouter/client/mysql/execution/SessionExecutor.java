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
package io.datarouter.client.mysql.execution;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException;

import io.datarouter.client.mysql.MysqlConnectionClientManager;
import io.datarouter.client.mysql.TxnClientManager;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ConnectionHandle;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.op.executor.impl.SessionExecutorPleaseRetryException;
import io.datarouter.util.string.StringTool;

@Singleton
public class SessionExecutor{
	private static final Logger logger = LoggerFactory.getLogger(SessionExecutor.class);

	private static final String READ_ONLY_ERROR_MESSAGE = "The MySQL server is running with the --read-only option"
			+ " so it cannot execute this statement";

	public static final Set<Class<?>> ROLLED_BACK_EXCEPTIONS = Set.of(
			MySQLTransactionRollbackException.class);

	@Inject
	private DatarouterClients datarouterClients;

	public <T> SessionExecutorCallable<T> makeCallable(BaseMysqlOp<T> parallelTxnOp, String traceName){
		return () -> run(parallelTxnOp, traceName);
	}

	public <T> T runWithoutRetries(BaseMysqlOp<T> parallelTxnOp){
		return runWithoutRetries(parallelTxnOp, null);
	}

	public <T> T runWithoutRetries(BaseMysqlOp<T> parallelTxnOp, String traceName){
		try{
			return run(parallelTxnOp, traceName);
		}catch(SessionExecutorPleaseRetryException e){
			logger.warn("no retrying operation", e);
			throw new RuntimeException(e);
		}
	}

	public <T> T run(BaseMysqlOp<T> parallelTxnOp) throws SessionExecutorPleaseRetryException{
		return run(parallelTxnOp, null);
	}

	public <T> T run(BaseMysqlOp<T> parallelTxnOp, String traceName) throws SessionExecutorPleaseRetryException{
		ClientId clientId = parallelTxnOp.getClientId();
		TxnClientManager clientManager = (TxnClientManager)datarouterClients.getClientManager(clientId);
		try{
			startTrace(traceName);
			clientManager.reserveConnection(clientId);
			return innerRun(clientId, clientManager, parallelTxnOp);
		}finally{
			finishTrace(traceName);
		}
	}

	private <T> T innerRun(ClientId clientId, TxnClientManager clientManager, BaseMysqlOp<T> parallelTxnOp)
	throws SessionExecutorPleaseRetryException{
		try{
			ConnectionHandle connectionHandle = clientManager.getExistingHandle(clientId);
			if(clientManager.getExistingHandle(clientId).isOutermostHandle()){
				clientManager.beginTxn(clientId, parallelTxnOp.getIsolation(), parallelTxnOp.isAutoCommit());
			}

			//begin user code
			T result = parallelTxnOp.runOnce();
			//end user code

			if(connectionHandle.isOutermostHandle()){
				String spanName = "commit " + clientId.getName();
				try(var $ = TracerTool.startSpan(spanName, TraceSpanGroupType.DATABASE)){
					clientManager.commitTxn(clientId);
				}
			}
			return result;
		}catch(Exception e){
			if(e instanceof DataAccessException){
				Throwable cause = e.getCause();
				if(cause instanceof SQLException && cause.getMessage().equals(READ_ONLY_ERROR_MESSAGE)){
					List<Connection> badConnections = new ArrayList<>();
					if(clientManager instanceof MysqlConnectionClientManager){
						badConnections.add(((MysqlConnectionClientManager)clientManager).getExistingConnection(
								clientId));
					}
					logger.warn("read only mode detected, need to discard the connection(s) {}", badConnections);
				}
			}
			if(wasRolledBackAndShouldRetry(e)){
				//make sure MysqlRollbackRetryingCallable catches this particular exception
				throw new SessionExecutorPleaseRetryException("", e);
			}
			try{
				clientManager.rollbackTxn(clientId);
			}catch(RuntimeException exceptionDuringRollback){
				logger.warn("EXCEPTION THROWN DURING TXN ROLL-BACK", exceptionDuringRollback);
				throw e;
			}
			throw e;
		}finally{
			try{
				clientManager.releaseConnection(clientId);
			}catch(Exception e){
				// This is an unexpected exception because each individual release is done in a try/catch block
				logger.warn("EXCEPTION THROWN DURING RELEASE OF CONNECTIONS", e);
			}
		}
	}

	/*------------------------------ helper  --------------------------------*/

	private boolean shouldTrace(String traceName){
		return StringTool.notEmpty(traceName);
	}

	private void startTrace(String traceName){
		if(shouldTrace(traceName)){
			TracerTool.startSpan(traceName, TraceSpanGroupType.DATABASE);
		}
	}

	private void finishTrace(String traceName){
		if(shouldTrace(traceName)){
			TracerTool.finishSpan();
		}
	}

	private boolean wasRolledBackAndShouldRetry(Exception exception){
		if(exception == null){
			return false;
		}
		if(ROLLED_BACK_EXCEPTIONS.contains(exception.getClass())){
			return true;
		}
		Throwable cause = exception.getCause();
		return cause != null && ROLLED_BACK_EXCEPTIONS.contains(cause.getClass());
	}

}
