package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientTableNodeNames;
import com.hotpads.datarouter.client.imp.hbase.client.HBaseClient;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseMultiAttemptTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseSubEntityQueryBuilder;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseSubEntityResultParser;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.entity.SubEntitySortedMapStorageReaderNode;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.serialize.fieldcache.EntityFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BatchingSortedScanner;
import com.hotpads.util.core.iterable.scanner.collate.Collator;
import com.hotpads.util.core.iterable.scanner.collate.PriorityQueueCollator;
import com.hotpads.util.core.iterable.scanner.imp.ListBackedSortedScanner;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class HBaseSubEntityReaderNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BasePhysicalNode<PK,D,F>
implements HBasePhysicalNode<PK,D>,
		SubEntitySortedMapStorageReaderNode<EK,PK,D,F>
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final int DEFAULT_ITERATE_BATCH_SIZE = Config.DEFAULT_ITERATE_BATCH_SIZE;

	private ClientTableNodeNames clientTableNodeNames;
	protected EntityFieldInfo<EK,E> entityFieldInfo;
	
	protected HBaseSubEntityQueryBuilder<EK,E,PK,D,F> queryBuilder;
	protected HBaseSubEntityResultParser<EK,E,PK,D,F> resultParser;
	
	/******************************* constructors ************************************/
	
	public HBaseSubEntityReaderNode(EntityNodeParams<EK,E> entityNodeParams, NodeParams<PK,D,F> params){
		super(params);
		this.clientTableNodeNames = new ClientTableNodeNames(getClientName(), getTableName(), getName());
		this.entityFieldInfo = new EntityFieldInfo<>(entityNodeParams);
		this.queryBuilder = new HBaseSubEntityQueryBuilder<EK,E,PK,D,F>(entityFieldInfo, fieldInfo);
		this.resultParser = new HBaseSubEntityResultParser<EK,E,PK,D,F>(entityFieldInfo, fieldInfo);
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public HBaseClient getClient(){
		return (HBaseClient)getRouter().getClient(getClientName());
	}
	
	@Override
	public String getEntityNodePrefix(){
		return fieldInfo.getEntityNodePrefix();
	}
	
	/************************************ MapStorageReader methods ****************************/
	
	@Override
	public boolean exists(PK key, Config config) {
		//should probably make a getKey method
		return get(key, config) != null;
	}

	
	@Override
	public D get(final PK key, final Config pConfig){
		if(key==null){ return null; }
		final Config config = Config.nullSafe(pConfig);
		return DrCollectionTool.getFirst(getMulti(DrListTool.wrap(key), config));
	}
	
	
	@Override
	public List<D> getMulti(final Collection<PK> pks, final Config pConfig){	
		if(DrCollectionTool.isEmpty(pks)){ return new LinkedList<D>(); }
		final Config config = Config.nullSafe(pConfig);
		return new HBaseMultiAttemptTask<List<D>>(new HBaseTask<List<D>>(getDatarouterContext(), getClientTableNodeNames(), 
				"getMulti", config){
				public List<D> hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
					DRCounters.incClientNodeCustom(client.getType(), "getMulti requested", getClientName(), getNodeName(), 
							DrCollectionTool.size(pks));
					List<Get> gets = queryBuilder.getGets(pks, false);
					Result[] hBaseResults = hTable.get(gets);
					List<D> databeans = resultParser.getDatabeansWithMatchingQualifierPrefix(hBaseResults);
					DRCounters.incClientNodeCustom(client.getType(), "getMulti found", getClientName(), getNodeName(), 
							DrCollectionTool.size(pks));
					return databeans;
				}
			}).call();
	}
	
	
	@Override
	public List<PK> getKeys(final Collection<PK> pks, final Config pConfig) {	
		if(DrCollectionTool.isEmpty(pks)){ return new LinkedList<PK>(); }
		final Config config = Config.nullSafe(pConfig);
		return new HBaseMultiAttemptTask<List<PK>>(new HBaseTask<List<PK>>(getDatarouterContext(), getClientTableNodeNames(), 
				"getKeys", config){
				public List<PK> hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
					DRCounters.incClientNodeCustom(client.getType(), "getKeys requested", getClientName(), getNodeName(), 
							DrCollectionTool.size(pks));
					List<Get> gets = queryBuilder.getGets(pks, true);
					Result[] hBaseResults = hTable.get(gets);
					List<PK> pks = resultParser.getPrimaryKeysWithMatchingQualifierPrefix(hBaseResults);
					DRCounters.incClientNodeCustom(client.getType(), "getKeys found", getClientName(), getNodeName(), 
							DrCollectionTool.size(pks));
					return pks;
				}
			}).call();
	}

	
	/************************* sorted **********************************/
	
	@Override
	public PK getFirstKey(Config pConfig){
		Config config = Config.nullSafe(pConfig).setIterateBatchSize(1);
		return DrIterableTool.first(scanKeys(null, config));
	}

	
	@Override
	public D getFirst(Config pConfig){
		Config config = Config.nullSafe(pConfig).setIterateBatchSize(1);
		return DrIterableTool.first(scan(null, config));
	}
	
	
	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		return getWithPrefixes(DrListTool.wrap(prefix), wildcardLastField, config);
	}

	
	@Override
	public List<D> getWithPrefixes(final Collection<PK> prefixes, final boolean wildcardLastField, 
			final Config pConfig){
		if(DrCollectionTool.isEmpty(prefixes)){ return new LinkedList<D>(); }
		final Config config = Config.nullSafe(pConfig);

		//segment prefixes into single vs multi-row queries
		final List<PK> singleEntityPrefixes = new ArrayList<>();
		final List<PK> multiEntityPrefixes = new ArrayList<>();
		for(PK prefix : prefixes){
			if(queryBuilder.isSingleEkPrefixQuery(prefix, wildcardLastField)){
				singleEntityPrefixes.add(prefix);
			}else{
				multiEntityPrefixes.add(prefix);
			}
		}
		
		//execute the single-row queries in a big multi-Get
		List<D> singleEntityResults = new HBaseMultiAttemptTask<List<D>>(new HBaseTask<List<D>>(
				getDatarouterContext(), getClientTableNodeNames(), "getWithPrefixes", config){
				public List<D> hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
					List<Get> gets = queryBuilder.getPrefixGets(singleEntityPrefixes, wildcardLastField, config);
					Result[] hbaseRows = hTable.get(gets);
					return resultParser.getDatabeansWithMatchingQualifierPrefix(hbaseRows);
				}
			}).call();

		//execute the multi-row queries in individual Scans
		//TODO parallelize
		List<D> multiEntityResults = new ArrayList<>();
		for(final PK pkPrefix : multiEntityPrefixes){
			EK ekPrefix = pkPrefix.getEntityKey();//we already determined prefix is confied to the EK
			final List<Scan> allPartitionScans = queryBuilder.getPrefixScans(ekPrefix, wildcardLastField, config);
			for(final Scan singlePartitionScan : allPartitionScans){
			List<D> singleScanResults = new HBaseMultiAttemptTask<List<D>>(new HBaseTask<List<D>>(
					getDatarouterContext(), getClientTableNodeNames(), "getWithPrefixes", config){
					public List<D> hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
						List<D> results = new ArrayList<>();
						managedResultScanner = hTable.getScanner(singlePartitionScan);
						for(Result row : managedResultScanner){
							if(row.isEmpty()){ continue; }
							List<D> singleRowResults = resultParser.getDatabeansWithMatchingQualifierPrefix(row);
							results.addAll(singleRowResults);
							if(config.getLimit()!=null && results.size()>=config.getLimit()){ break; }
						}
						return results;
					}
				}).call();
				multiEntityResults.addAll(singleScanResults);
			}
		}
		
		List<D> allResults = DrListTool.concatenate(singleEntityResults, multiEntityResults);
		Collections.sort(allResults);
		return allResults;
	}
	
	@Override
	public SortedScannerIterable<PK> scanKeys(final Range<PK> pRange, final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		final Range<PK> range = Range.nullSafe(pRange);
		if(queryBuilder.isSingleEntity(range)){//single row.  use Get.  gets all pks in entity.  no way to limit rows
			List<PK> pks = new HBaseMultiAttemptTask<List<PK>>(new HBaseTask<List<PK>>(getDatarouterContext(), 
					getClientTableNodeNames(), "scanPksInEntity", config){
				public List<PK> hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
					Get get = queryBuilder.getSingleRowRange(range.getStart().getEntityKey(), range, true);
					Result result = hTable.get(get);
					return DrListTool.createArrayList(resultParser.getPrimaryKeysWithMatchingQualifierPrefix(result));	
				}}).call();
			return new SortedScannerIterable<PK>(new ListBackedSortedScanner<PK>(pks));
		}
		List<BatchingSortedScanner<PK>> scanners = queryBuilder.getPkScanners(this, range, pConfig);
		Collator<PK> collator = new PriorityQueueCollator<PK>(scanners);
		return new SortedScannerIterable<PK>(collator);
	}
	
	@Override
	public SortedScannerIterable<D> scan(final Range<PK> pRange, final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		final Range<PK> range = Range.nullSafe(pRange);
		if(queryBuilder.isSingleEntity(range)){//single row.  use Get.  gets all databeans in entity.  no way to limit rows
			List<D> databeans = new HBaseMultiAttemptTask<List<D>>(new HBaseTask<List<D>>(getDatarouterContext(), 
					getClientTableNodeNames(), "scanInEntity", config){
				public List<D> hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
					Get get = queryBuilder.getSingleRowRange(range.getStart().getEntityKey(), range, false);
					Result result = hTable.get(get);
					return resultParser.getDatabeansWithMatchingQualifierPrefix(result);	
				}}).call();
			return new SortedScannerIterable<D>(new ListBackedSortedScanner<D>(databeans));
		}
		List<BatchingSortedScanner<D>> scanners = queryBuilder.getDatabeanScanners(this, range, pConfig);
		Collator<D> collator = new PriorityQueueCollator<D>(scanners);
		return new SortedScannerIterable<D>(collator);
	}
		
	
	/***************************** helper methods **********************************/
	
	/*
	 * internal method to fetch a single batch of hbase rows/keys.  only public so that iterators in other packages
	 * can use it
	 * 
	 * warning: we cannot currently limit the number of databeans/pks, only hbase rows.  be aware that it will probably
	 * return more databeans/pks than iterateBatchSize
	 */
	public List<Result> getResultsInSubRange(final int partition, final Range<PK> rowRange, final boolean keysOnly, 
			final Config pConfig){
		final Config config = Config.nullSafe(pConfig);
		final String scanKeysVsRowsNumBatches = "scan " + (keysOnly ? "pk" : "databean") + " numBatches";
		final String scanKeysVsRowsNumRows = "scan " + (keysOnly ? "pk" : "databean") + " numRows";
//		final String scanKeysVsRowsNumCells = "scan " + (keysOnly ? "key" : "row") + " numCells";//need a clean way to get cell count
		return new HBaseMultiAttemptTask<List<Result>>(new HBaseTask<List<Result>>(getDatarouterContext(), 
				getClientTableNodeNames(), scanKeysVsRowsNumBatches, config){
				public List<Result> hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
					Scan scan = queryBuilder.getScanForSubrange(partition, rowRange, pConfig, keysOnly);
					managedResultScanner = hTable.getScanner(scan);
					List<Result> results = new ArrayList<>();
					for(Result row : managedResultScanner){
						if(row.isEmpty()){ continue; }
						results.add(row);
						if(config.getIterateBatchSize()!=null && results.size()>=config.getIterateBatchSize()){ break; }
						if(config.getLimit()!=null && results.size()>=config.getLimit()){ break; }
					}
					managedResultScanner.close();
					DRCounters.incClientNodeCustom(client.getType(), scanKeysVsRowsNumRows, getClientName(), getNodeName(),  
							DrCollectionTool.size(results));
					return results;
				}
			}).call();
	}
	
	
	/********************* get/set *******************************/

	public HBaseSubEntityResultParser<EK,E,PK,D,F> getResultParser(){
		return resultParser;
	}

	public ClientTableNodeNames getClientTableNodeNames(){
		return clientTableNodeNames;
	}

	public EntityFieldInfo<EK,E> getEntityFieldInfo(){
		return entityFieldInfo;
	}
	
	
}