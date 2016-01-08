package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientTableNodeNames;
import com.hotpads.datarouter.client.imp.hbase.client.HBaseClient;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseMultiAttemptTask;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseQueryBuilder;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseResultTool;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseScatteringPrefixQueryBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.AsyncBatchLoaderScanner;
import com.hotpads.util.core.iterable.scanner.collate.PriorityQueueCollator;
import com.hotpads.util.core.iterable.scanner.iterable.SingleUseScannerIterable;
import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;

public class HBaseReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements HBasePhysicalNode<PK,D>,
		MapStorageReader<PK,D>,
		SortedStorageReader<PK,D>{
	private static final Logger logger = LoggerFactory.getLogger(HBaseReaderNode.class);

	private ClientTableNodeNames clientTableNodeNames;

	/******************************* constructors ************************************/

	public HBaseReaderNode(NodeParams<PK,D,F> params){
		super(params);
		this.clientTableNodeNames = new ClientTableNodeNames(getClientId().getName(), getTableName(), getName());
	}

	/***************************** plumbing methods ***********************************/

	@Override
	public HBaseClient getClient(){
		return (HBaseClient)getRouter().getClient(getClientId().getName());
	}

	/************************************ MapStorageReader methods ****************************/

	@Override
	public boolean exists(PK key, Config config) {
		//should probably make a getKey method
		return get(key, config) != null;
	}


	@Override
	public D get(final PK key, Config config){
		if(key==null){
			return null;
		}
		config = Config.nullSafe(config);
		return new HBaseMultiAttemptTask<>(new HBaseTask<D>(getDatarouter(), getClientTableNodeNames(), "get",
				config){
			@Override
			public D hbaseCall(HTable table, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
				byte[] rowBytes = getKeyBytesWithScatteringPrefix(null, key);
				Result row = table.get(new Get(rowBytes));
				if (row.isEmpty()){
					return null;
				}
				if( ! Bytes.equals(rowBytes, row.getRow())){//bug in hbase 0.94.2?
					logger.warn("hbase returned row that doesn't match our key");
					return null;
				}
				D result = HBaseResultTool.getDatabean(row, fieldInfo);
				return result;
			}
		}).call();
	}


	@Override
	public List<D> getMulti(final Collection<PK> keys, Config config){
		if(DrCollectionTool.isEmpty(keys)){
			return new LinkedList<>();
		}
		config = Config.nullSafe(config);
		return new HBaseMultiAttemptTask<>(new HBaseTask<List<D>>(getDatarouter(), getClientTableNodeNames(),
				"getMulti", config){
			@Override
			public List<D> hbaseCall(HTable table, HBaseClient client, ResultScanner managedResultScanner)
					throws Exception{
				List<Get> gets = DrListTool.createArrayListWithSize(keys);
					for(PK key : keys){
						byte[] rowBytes = getKeyBytesWithScatteringPrefix(null, key);
						gets.add(new Get(rowBytes));
					}
					Result[] resultArray = table.get(gets);
					return HBaseResultTool.getDatabeans(Arrays.asList(resultArray), fieldInfo);
				}
			}).call();
	}


	@Override
	public List<PK> getKeys(final Collection<PK> keys, Config config) {
		if(DrCollectionTool.isEmpty(keys)){
			return new LinkedList<>();
		}
		config = Config.nullSafe(config);
		return new HBaseMultiAttemptTask<>(new HBaseTask<List<PK>>(getDatarouter(), getClientTableNodeNames(),
				"getKeys", config){
			@Override
			public List<PK> hbaseCall(HTable table, HBaseClient client, ResultScanner managedResultScanner)
					throws Exception{
				List<Get> gets = DrListTool.createArrayListWithSize(keys);
					for(PK key : keys){
						byte[] rowBytes = getKeyBytesWithScatteringPrefix(null, key);
						Get get = new Get(rowBytes);
						//FirstKeyOnlyFilter returns value too, so it's better if value in each row is not large
						get.setFilter(new FirstKeyOnlyFilter());
						gets.add(get);
					}
					Result[] resultArray = table.get(gets);
					return HBaseResultTool.getPrimaryKeys(Arrays.asList(resultArray), fieldInfo);
				}
			}).call();
	}


	/******************************* Sorted *************************************/

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		return getWithPrefixes(DrListTool.wrap(prefix), wildcardLastField, config);
	}


	@Override
	public List<D> getWithPrefixes(final Collection<PK> prefixes, final boolean wildcardLastField, Config config){
		if(DrCollectionTool.isEmpty(prefixes)){
			return new LinkedList<>();
		}
		config = Config.nullSafe(config);
		final List<D> results = new ArrayList<>();
		List<Scan> scanForEachScatteringPartition = HBaseScatteringPrefixQueryBuilder.getPrefixScanners(fieldInfo,
				prefixes, wildcardLastField, config);
		for(final Scan scan : scanForEachScatteringPartition){
			new HBaseMultiAttemptTask<>(new HBaseTask<Void>(getDatarouter(), getClientTableNodeNames(),
					"getWithPrefixes", config){
				@Override
				public Void hbaseCall(HTable table, HBaseClient client, ResultScanner managedResultScanner)
						throws Exception{
					managedResultScanner = table.getScanner(scan);
						for(Result row : managedResultScanner){
							if(row.isEmpty()){
								continue;
							}
							D result = HBaseResultTool.getDatabean(row, fieldInfo);
							results.add(result);//add results directly to the parent result list
							//TODO terribly inneficient limiting.  fetches full limit for every scattering partition
							if(config.getLimit()!=null && results.size()>=config.getLimit()){
								break;
							}
						}
						return null;
					}
				}).call();
		}
		sortIfScatteringPrefixExists(results);
		return results;
	}

	@Override
	public SingleUseScannerIterable<PK> scanKeys(Range<PK> range, final Config config){
		range = Range.nullSafe(range);
		Config nullSafeConfig = Config.nullSafe(config);
		if(nullSafeConfig.getLimit() != null && nullSafeConfig.getOffset() != null){
			nullSafeConfig.setLimit(nullSafeConfig.getLimit() + nullSafeConfig.getOffset());
		}
		List<AsyncBatchLoaderScanner<PK>> scanners = HBaseScatteringPrefixQueryBuilder
				.getBatchingPrimaryKeyScannerForEachPrefix(getClient().getExecutorService(), this, fieldInfo, range,
						nullSafeConfig);
		SortedScanner<PK> collator = new PriorityQueueCollator<>(scanners);
		collator.advanceBy(nullSafeConfig.getOffset());
		return new SingleUseScannerIterable<>(collator);
	}

	@Override
	public SingleUseScannerIterable<D> scan(Range<PK> range, final Config config){
		range = Range.nullSafe(range);
		Config nullSafeConfig = Config.nullSafe(config);
		if(nullSafeConfig.getLimit() != null && nullSafeConfig.getOffset() != null){
			nullSafeConfig.setLimit(nullSafeConfig.getLimit() + nullSafeConfig.getOffset());
		}
		List<AsyncBatchLoaderScanner<D>> scanners = HBaseScatteringPrefixQueryBuilder
				.getBatchingDatabeanScannerForEachPrefix(getClient().getExecutorService(), this, fieldInfo, range,
						nullSafeConfig);
		SortedScanner<D> collator = new PriorityQueueCollator<>(scanners);
		collator.advanceBy(nullSafeConfig.getOffset());
		return new SingleUseScannerIterable<>(collator);
	}


	/***************************** helper methods **********************************/

	/*
	 * internal method to fetch a single batch of hbase rows/keys.  only public so that iterators in other packages
	 * can use it
	 */
	public List<Result> getResultsInSubRange(final Range<ByteRange> range, final boolean keysOnly, Config config){
		config = Config.nullSafe(config);
		final String scanKeysVsRowsNumBatches = "scan " + (keysOnly ? "key" : "row") + " numBatches";
		final String scanKeysVsRowsNumRows = "scan " + (keysOnly ? "key" : "row") + " numRows";
		//need a clean way to get cell count
//		final String scanKeysVsRowsNumCells = "scan " + (keysOnly ? "key" : "row") + " numCells";
		return new HBaseMultiAttemptTask<>(new HBaseTask<List<Result>>(getDatarouter(),
				getClientTableNodeNames(), scanKeysVsRowsNumBatches, config){
			@Override
			public List<Result> hbaseCall(HTable table, HBaseClient client, ResultScanner managedResultScanner)
					throws Exception{
				ByteRange start = range.getStart();
				//careful: this may have already been set by scatteringPrefix logic
				if(start!=null && !range.getStartInclusive()){
					start = new ByteRange(DrByteTool.unsignedIncrement(start.toArray()));
				}
				ByteRange end = range.getEnd();

				//startInclusive already adjusted for
				Range<ByteRange> scanRange = Range.create(start, true, end, range.getEndInclusive());
				Scan scan = HBaseQueryBuilder.getScanForRange(scanRange, config);
				if(keysOnly){
					scan.setFilter(new FirstKeyOnlyFilter());
				}
				managedResultScanner = table.getScanner(scan);
				List<Result> results = new ArrayList<>();
				for(Result row : managedResultScanner){
					if(row.isEmpty()){
						continue;
					}
					results.add(row);
					if(config.getIterateBatchSize()!=null && results.size()>=config.getIterateBatchSize()){
						break;
					}
					if(config.getLimit()!=null && results.size()>=config.getLimit()){
						break;
					}
				}
				managedResultScanner.close();
				DRCounters.incClientNodeCustom(client.getType(), scanKeysVsRowsNumRows, getClientName(), getNodeName(),
						DrCollectionTool.size(results));
				return results;
			}
		}).call();
	}

	//this method is in the node because it deals with the messy primaryKeyHasUnnecessaryTrailingSeparatorByte
	public byte[] getKeyBytesWithScatteringPrefix(List<Field<?>> overrideScatteringPrefixFields, PK key){
		//return only scatteringPrefix bytes
		if(key==null){
			if(DrCollectionTool.isEmpty(overrideScatteringPrefixFields)){
				return new byte[]{};
			}
			return FieldTool.getConcatenatedValueBytes(overrideScatteringPrefixFields, false, false);
		}

		//else return scatteringPrefix bytes + keyBytes + (maybe) trailing separator
		List<Field<?>> scatteringPrefixFields = new LinkedList<>();
		if(DrCollectionTool.notEmpty(overrideScatteringPrefixFields)){
			scatteringPrefixFields.addAll(overrideScatteringPrefixFields);
		}else{
			//maybe Assert the override fields match those returned for the key
			scatteringPrefixFields.addAll(fieldInfo.getSampleScatteringPrefix().getScatteringPrefixFields(key));
		}
		byte[] scatteringPrefixBytes = FieldTool.getConcatenatedValueBytes(scatteringPrefixFields, true, false);
		byte[] keyBytes = FieldTool.getConcatenatedValueBytes(key.getFields(), true, false);
		return DrByteTool.concatenate(scatteringPrefixBytes, keyBytes);
	}

	private <T extends Comparable<? super T>> void sortIfScatteringPrefixExists(List<T> ins){
		if(fieldInfo.getSampleScatteringPrefix().getNumPrefixBytes() > 0){
			Collections.sort(ins);
		}
	}

	public ClientTableNodeNames getClientTableNodeNames(){
		return clientTableNodeNames;
	}


}
