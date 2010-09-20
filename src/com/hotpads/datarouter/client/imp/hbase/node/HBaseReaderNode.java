package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.HBaseTask;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseQueryBuilder;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseResultTool;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.physical.BasePhysicalNode;
import com.hotpads.datarouter.node.scanner.Scanner;
import com.hotpads.datarouter.node.type.physical.PhysicalMapStorageReaderNode;
import com.hotpads.datarouter.node.type.physical.PhysicalSortedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.iterable.PeekableIterable;

public class HBaseReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK>> 
extends BasePhysicalNode<PK,D>
implements HBasePhysicalNode<PK,D>
			,PhysicalMapStorageReaderNode<PK,D>
			,PhysicalSortedStorageReaderNode<PK,D>
{
	protected Logger logger = Logger.getLogger(getClass());
	
	public static final int DEFAULT_ITERATE_BATCH_SIZE = 1000;
	
	/******************************* constructors ************************************/

	public HBaseReaderNode(Class<D> databeanClass, 
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HBaseReaderNode(Class<D> databeanClass,
			DataRouter router, String clientName) {
		super(databeanClass, router, clientName);
	}

	public HBaseReaderNode(Class<? super D> baseDatabeanClass, Class<D> databeanClass, 
			DataRouter router, String clientName){
		super(baseDatabeanClass, databeanClass, router, clientName);
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public HBaseClient getClient(){
		return (HBaseClient)this.router.getClient(getClientName());
	}
	
	@Override
	public void clearThreadSpecificState(){
	}

	public HTable checkOutHTable(){
		return this.getClient().checkOutHTable(this.getTableName());
	}
	
	public void checkInHTable(HTable hTable){
		this.getClient().checkInHTable(hTable);
	}
	
	/*
	 * need to handle
	 * 
	 * 2010-09-15 11:07:13,050  WARN org.apache.hadoop.hbase.zookeeper.ZooKeeperWrapper:418 - Failed to create /hbase -- check quorum servers, currently=hammer:2181
org.apache.zookeeper.KeeperException$ConnectionLossException: KeeperErrorCode = ConnectionLoss for /hbase
	at org.apache.zookeeper.KeeperException.create(KeeperException.java:90)
	at org.apache.zookeeper.KeeperException.create(KeeperException.java:42)
	at org.apache.zookeeper.ZooKeeper.exists(ZooKeeper.java:809)
	at org.apache.zookeeper.ZooKeeper.exists(ZooKeeper.java:837)
	at org.apache.hadoop.hbase.zookeeper.ZooKeeperWrapper.ensureExists(ZooKeeperWrapper.java:405)
	at org.apache.hadoop.hbase.zookeeper.ZooKeeperWrapper.ensureParentExists(ZooKeeperWrapper.java:432)
	at org.apache.hadoop.hbase.zookeeper.ZooKeeperWrapper.checkOutOfSafeMode(ZooKeeperWrapper.java:545)
	at org.apache.hadoop.hbase.client.HConnectionManager$TableServers.locateRootRegion(HConnectionManager.java:956)
	at org.apache.hadoop.hbase.client.HConnectionManager$TableServers.locateRegion(HConnectionManager.java:625)
	at org.apache.hadoop.hbase.client.HConnectionManager$TableServers.relocateRegion(HConnectionManager.java:607)
	at org.apache.hadoop.hbase.client.HConnectionManager$TableServers.locateRegionInMeta(HConnectionManager.java:758)
	at org.apache.hadoop.hbase.client.HConnectionManager$TableServers.locateRegion(HConnectionManager.java:630)
	at org.apache.hadoop.hbase.client.HConnectionManager$TableServers.locateRegion(HConnectionManager.java:601)
	at org.apache.hadoop.hbase.client.HConnectionManager$TableServers.locateRegionInMeta(HConnectionManager.java:670)
	at org.apache.hadoop.hbase.client.HConnectionManager$TableServers.locateRegion(HConnectionManager.java:634)
	at org.apache.hadoop.hbase.client.HConnectionManager$TableServers.relocateRegion(HConnectionManager.java:607)
	at org.apache.hadoop.hbase.client.HConnectionManager$TableServers.getRegionLocation(HConnectionManager.java:428)
	at org.apache.hadoop.hbase.client.HConnectionManager$TableServers.getRegionLocationForRowWithRetries(HConnectionManager.java:1071)
	at org.apache.hadoop.hbase.client.HConnectionManager$TableServers.access$200(HConnectionManager.java:240)
	at org.apache.hadoop.hbase.client.HConnectionManager$TableServers$Batch.getRegionName(HConnectionManager.java:1183)
	at org.apache.hadoop.hbase.client.HConnectionManager$TableServers$Batch.process(HConnectionManager.java:1128)
	at org.apache.hadoop.hbase.client.HConnectionManager$TableServers.processBatchOfRows(HConnectionManager.java:1230)
	at org.apache.hadoop.hbase.client.HTable.flushCommits(HTable.java:666)
	at org.apache.hadoop.hbase.client.HTable.put(HTable.java:535)
	at com.hotpads.datarouter.client.imp.hbase.HBaseNode.putMulti(HBaseNode.java:104)
	at com.hotpads.datarouter.node.type.partitioned.PartitionedSortedStorageNode.putMulti(PartitionedSortedStorageNode.java:86)
	at com.hotpads.profile.count.collection.archive.imp.DatabeanCountArchive.saveCounts(DatabeanCountArchive.java:91)
	at com.hotpads.profile.count.collection.archive.CountArchiveFlusher$CountArchiveFlushAttempt.run(CountArchiveFlusher.java:100)
	at java.util.concurrent.Executors$RunnableAdapter.call(Unknown Source)
	at java.util.concurrent.FutureTask$Sync.innerRun(Unknown Source)
	at java.util.concurrent.FutureTask.run(Unknown Source)
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$301(Unknown Source)
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(Unknown Source)
	at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(Unknown Source)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)
	at java.lang.Thread.run(Unknown Source)
	 */
	
	/************************************ MapStorageReader methods ****************************/
	
	@Override
	public boolean exists(PK key, Config config) {
		return this.get(key, config) != null;
	}

	
	@Override
	public D get(final PK key, final Config pConfig){
		if(key==null){ return null; }
		return new HBaseTask<D>("get", this){
				public D wrappedCall() throws Exception{
					Result row = hTable.get(new Get(key.getBytes(false)));
					if(row.isEmpty()){ return null; }
					D result = HBaseResultTool.getDatabean(row, databeanClass, primaryKeyFields, fieldByMicroName);
					return result;
				}
			}.call();
	}
	
	
	@Override
	public List<D> getAll(final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		return new HBaseTask<List<D>>("getAll", this){
				public List<D> wrappedCall() throws Exception{
					List<D> results = ListTool.createArrayList();
					Scan scan = new Scan();
					scan.setCaching(HBaseQueryBuilder.getIterateBatchSize(config));
					ResultScanner scanner = hTable.getScanner(scan);
					for(Result row : scanner){
						if(row.isEmpty()){ continue; }
						D result = HBaseResultTool.getDatabean(row, databeanClass, primaryKeyFields, fieldByMicroName);
						results.add(result);
						if(config.getLimit()!=null && results.size()>=config.getLimit()){ break; }
					}
					scanner.close();
					return results;
				}
			}.call();
	}

	
	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config pConfig){	
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<D>(); }
		return new HBaseTask<List<D>>("getMulti", this){
				public List<D> wrappedCall() throws Exception{
					List<D> results = ListTool.createArrayListWithSize(keys);
					for(PK key : keys){
						Result row = hTable.get(new Get(key.getBytes(false)));
						if(row.isEmpty()){ continue; }
						D result = HBaseResultTool.getDatabean(row, databeanClass, primaryKeyFields, fieldByMicroName);
						results.add(result);
					}
					return results;
				}
			}.call();
	}
	
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config pConfig) {	
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<PK>(); }
		return new HBaseTask<List<PK>>("getKeys", this){
				public List<PK> wrappedCall() throws Exception{
					List<PK> results = ListTool.createArrayListWithSize(keys);
					for(PK key : keys){
						Get get = new Get(key.getBytes(false));
						get.setFilter(new FirstKeyOnlyFilter());//make sure first column in row is not something big
						Result row = hTable.get(get);
						if(row.isEmpty()){ continue; }
						PK result = HBaseResultTool.getPrimaryKey(row.getRow(), primaryKeyClass, primaryKeyFields);
						results.add(result);
					}
					return results;
				}
			}.call();
	}

	
	/******************************* Sorted *************************************/
	
	@Override
	public PK getFirstKey(Config config){
		Config nsConfig = Config.nullSafe(config).setLimit(1);
		return CollectionTool.getFirst(
				getKeysInRange(null, true, null, true, nsConfig));
	}

	
	@Override
	public D getFirst(Config config){
		Config nsConfig = Config.nullSafe(config).setLimit(1);
		return CollectionTool.getFirst(
				getRange(null, true, null, true, nsConfig));
	}
	
	
	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		return getWithPrefixes(ListTool.wrap(prefix), wildcardLastField, config);
	}

	
	@Override
	public List<D> getWithPrefixes(final Collection<? extends PK> prefixes, 
			final boolean wildcardLastField, final Config pConfig){
		if(CollectionTool.isEmpty(prefixes)){ return new LinkedList<D>(); }
		final Config config = Config.nullSafe(pConfig);
		return new HBaseTask<List<D>>("getWithPrefixes", this){
				public List<D> wrappedCall() throws Exception{
					List<D> results = ListTool.createArrayList();
					for(PK prefix : prefixes){
						Scan scan = HBaseQueryBuilder.getPrefixScanner(prefix, wildcardLastField, config);
						ResultScanner scanner = hTable.getScanner(scan);
						for(Result row : scanner){
							if(row.isEmpty()){ continue; }
							D result = HBaseResultTool.getDatabean(row, databeanClass, primaryKeyFields, fieldByMicroName);
							results.add(result);
							if(config.getLimit()!=null && results.size()>=config.getLimit()){ break; }
						}
						scanner.close();
					}
					return results;
				}
			}.call();
	}
	

	@Override
	public List<PK> getKeysInRange(final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		return new HBaseTask<List<PK>>("getKeysInRange", this){
				public List<PK> wrappedCall() throws Exception{
					Scan scan = HBaseQueryBuilder.getRangeScanner(start, startInclusive, end, endInclusive, config);
					scan.setFilter(new FirstKeyOnlyFilter());
					List<PK> results = ListTool.createArrayList(scan.getCaching());
					ResultScanner scanner = hTable.getScanner(scan);
					for(Result row : scanner){
						if(row.isEmpty()){ continue; }
						PK result = HBaseResultTool.getPrimaryKey(row.getRow(), primaryKeyClass, primaryKeyFields);
						results.add(result);
						if(config.getLimit()!=null && results.size()>=config.getLimit()){ break; }
					}
					scanner.close();
					return results;
				}
			}.call();
	}
	

	//TODO could probably call getPrefixedRange with no prefix
	@Override
	public List<D> getRange(final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		return new HBaseTask<List<D>>("getRange", this){
				public List<D> wrappedCall() throws Exception{
					Scan scan = HBaseQueryBuilder.getRangeScanner(start, startInclusive, end, endInclusive, config);
					List<D> results = ListTool.createArrayList(scan.getCaching());
					ResultScanner scanner = hTable.getScanner(scan);
					for(Result row : scanner){
						if(row.isEmpty()){ continue; }
						D result = HBaseResultTool.getDatabean(row, databeanClass, primaryKeyFields, fieldByMicroName);
						results.add(result);
						if(config.getLimit()!=null && results.size()>=config.getLimit()){ break; }
					}
					scanner.close();
					return results;
				}
			}.call();
	}
	
	
	@Override
	public List<D> getPrefixedRange(
			final PK prefix, final boolean wildcardLastField, 
			final PK start, final boolean startInclusive, 
			final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		return new HBaseTask<List<D>>("getPrefixedRange", this){
				public List<D> wrappedCall() throws Exception{
					Scan scan = HBaseQueryBuilder.getPrefixedRangeScanner(
							prefix, wildcardLastField, 
							start, startInclusive, 
							null, true, 
							config);
					List<D> results = ListTool.createArrayList(scan.getCaching());
					ResultScanner scanner = hTable.getScanner(scan);
					for(Result row : scanner){
						if(row.isEmpty()){ continue; }
						D result = HBaseResultTool.getDatabean(row, databeanClass, primaryKeyFields, fieldByMicroName);
						results.add(result);
						if(config.getLimit()!=null && results.size()>=config.getLimit()){ break; }
					}
					scanner.close();
					return results;
				}
			}.call();
	}

	
	@Override
	public PeekableIterable<D> scan(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config config){
		return new Scanner<PK,D>(this, start, startInclusive, end, endInclusive, 
				config, DEFAULT_ITERATE_BATCH_SIZE);
	}
		
	
	
}
