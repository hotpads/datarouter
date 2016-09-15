package com.hotpads.datarouter.client.imp.hbase;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.client.imp.hbase.client.HBaseClient;
import com.hotpads.datarouter.client.imp.hbase.pool.HBaseTablePool;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.concurrent.FutureTool;
import com.hotpads.util.datastructs.MutableString;

public class HBaseClientImp
extends BaseClient
implements HBaseClient{
	private static final Logger logger = LoggerFactory.getLogger(HBaseClientImp.class);

	private final Connection connection;
	private final Configuration hbaseConfiguration;
	private final Admin admin;
	private final HBaseTablePool pool;
	private final ExecutorService executorService;
	private final Map<String,Class<? extends PrimaryKey<?>>> primaryKeyClassByName;
	private final ClientType clientType;
	private final boolean schemaUpdateEnabled;

	/**************************** constructor  **********************************/

	public HBaseClientImp(String name, Connection connection, Admin hbaseAdmin, HBaseTablePool pool,
			Map<String,Class<? extends PrimaryKey<?>>> primaryKeyClassByName, ClientAvailabilitySettings
			clientAvailabilitySettings, ExecutorService executorService, ClientType clientType,
			boolean schemaUpdateEnabled){
		super(name, clientAvailabilitySettings);
		this.connection = connection;
		this.clientType = clientType;
		this.hbaseConfiguration = connection.getConfiguration();
		this.admin = hbaseAdmin;
		this.pool = pool;
		this.executorService = executorService;
		this.primaryKeyClassByName = primaryKeyClassByName;
		this.schemaUpdateEnabled = schemaUpdateEnabled;
	}

	@Override
	public ClientType getType(){
		return clientType;
	}

	/****************************** HBaseClient methods *************************/

	@Override
	public Admin getAdmin(){
		return admin;
	}

	@Override
	public Table getTable(String name){
		try{
			return connection.getTable(TableName.valueOf(name));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public Table checkOutTable(String name, MutableString progress){
		return pool.checkOut(name, progress);
	}

	@Override
	public void checkInTable(Table table, boolean possiblyTarnished){
		pool.checkIn(table, possiblyTarnished);
	}

	@Override
	public HBaseTablePool getHTablePool(){
		return pool;
	}

	@Override
	public ExecutorService getExecutorService(){
		return executorService;
	}

	@Override
	public Class<? extends PrimaryKey<?>> getPrimaryKeyClass(String tableName){
		return primaryKeyClassByName.get(tableName);
	}

	public Configuration getHBaseConfiguration(){
		return hbaseConfiguration;
	}

	@Override
	public void shutdown(){
		logger.warn("shutting down client:"+getName());
		FutureTool.finishAndShutdown(executorService, 5L, TimeUnit.SECONDS);
		pool.shutdown();
	}

	@Override
	public Future<Optional<String>> notifyNodeRegistration(PhysicalNode<?,?> node){
		if(schemaUpdateEnabled){
			generateSchemaUpdate(node);
		}
		return CompletableFuture.completedFuture(Optional.empty());
	}

	private void generateSchemaUpdate(Node<?,?> node){
		DatabeanFieldInfo<?,?,?> fieldInfo = node.getFieldInfo();
		String tableName = fieldInfo.getTableName();
		HTableDescriptor desc;
		try{
			desc = admin.getTableDescriptor(TableName.valueOf(tableName));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		for(HColumnDescriptor column : desc.getColumnFamilies()){
			if(fieldInfo.getTtlMs().isPresent()){
				if(!fieldInfo.getTtlMs().get().equals(column.getTimeToLive())){
					logger.warn("Please alter the TTL of " + node.getName() + " to "
							+ fieldInfo.getTtlMs().get() / 1000 + " seconds");
				}
			}
		}
	}

}
