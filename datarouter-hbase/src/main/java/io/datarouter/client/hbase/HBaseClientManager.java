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
package io.datarouter.client.hbase;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.util.ShutdownHookManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.hbase.client.HBaseConnectionHolder;
import io.datarouter.client.hbase.client.HBaseOptions;
import io.datarouter.client.hbase.pool.HBaseTablePool;
import io.datarouter.client.hbase.pool.HBaseTablePoolHolder;
import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.config.schema.SchemaUpdateResult;
import io.datarouter.storage.exception.UnavailableException;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.mutable.MutableString;
import io.datarouter.util.timer.PhaseTimer;

@Singleton
public class HBaseClientManager extends BaseClientManager{
	private static final Logger logger = LoggerFactory.getLogger(HBaseClientManager.class);

	// these are used for databeans with no values outside the PK. we fake a value as we need at least 1 cell in a row
	public static final byte[] DEFAULT_FAMILY_QUALIFIER = {(byte)'a'};
	public static final byte[] DUMMY_COL_NAME_BYTES = new byte[]{0};
	public static final String DUMMY_COL_NAME = new String(DUMMY_COL_NAME_BYTES);
	// Byte.MIN_VALUE for legacy reasons, but could probably be changed
	public static final byte[] DUMMY_FIELD_VALUE = new byte[]{Byte.MIN_VALUE};

	@Inject
	private HBaseTablePoolHolder hBaseTablePoolHolder;
	@Inject
	private SchemaUpdateOptions schemaUpdateOptions;
	@Inject
	private HBaseConnectionHolder hBaseConnectionHolder;
	@Inject
	private HBaseOptions hBaseOptions;
	@Inject
	private HBaseSchemaUpdateService hBaseSchemaUpdateService;

	@Override
	protected void safeInitClient(ClientId clientId){
		logger.info("activating HBase client " + clientId.getName());
		PhaseTimer timer = new PhaseTimer(clientId.getName());
		Connection connection = makeConnection(clientId.getName());
		timer.add("init hbase connection");
		hBaseTablePoolHolder.register(clientId, connection);
		timer.add("init hbase pool");
		logger.warn(timer.add("done").toString());
	}

	public Connection getConnection(ClientId clientId){
		initClient(clientId);
		return hBaseConnectionHolder.getConnection(clientId);
	}

	public Admin getAdmin(ClientId clientId){
		try{
			return getConnection(clientId).getAdmin();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public Table getTable(ClientId clientId, String tableName){
		try{
			return getConnection(clientId).getTable(TableName.valueOf(tableName));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	protected Connection makeConnection(String clientName){
		String zkQuorum = hBaseOptions.zookeeperQuorum(clientName);
		Configuration hbaseConfig = HBaseConfiguration.create();
		hbaseConfig.set(HConstants.ZOOKEEPER_QUORUM, zkQuorum);
		Connection connection;
		try{
			connection = ConnectionFactory.createConnection(hbaseConfig);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		if(connection.isClosed()){
			String log = "couldn't open connection because hBaseAdmin.getConnection().isClosed()";
			logger.warn(log);
			throw new UnavailableException(log);
		}
		return connection;
	}

	@Override
	protected Future<Optional<SchemaUpdateResult>> doSchemaUpdate(PhysicalNode<?,?,?> node){
		if(schemaUpdateOptions.getEnabled()){
			return hBaseSchemaUpdateService.queueNodeForSchemaUpdate(node.getFieldInfo().getClientId(), node);
		}
		return CompletableFuture.completedFuture(Optional.empty());
	}

	public HBaseTablePool getHTablePool(ClientId clientId){
		initClient(clientId);
		return hBaseTablePoolHolder.getHBaseTablePool(clientId);
	}

	public Table checkOutTable(ClientId clientId, String name, MutableString progress){
		return getHTablePool(clientId).checkOut(name, progress);
	}

	public void checkInTable(ClientId clientId, Table table, boolean possiblyTarnished){
		getHTablePool(clientId).checkIn(table, possiblyTarnished);
	}

	@Override
	public void shutdown(ClientId clientId){
		hBaseSchemaUpdateService.gatherSchemaUpdates(true);
		getHTablePool(clientId).shutdown();
		eagerlyInitializeShutdownHookManager();
	}

	private void eagerlyInitializeShutdownHookManager(){
		ReflectionTool.invoke(ShutdownHookManager.get(), "getShutdownHooksInOrder");
	}

}
