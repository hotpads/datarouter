package com.hotpads.datarouter.client.imp.hbase.scan;

import org.apache.hadoop.hbase.client.Result;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchBackedScanner;

@Deprecated
public abstract class BaseHBaseScanner<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		T extends Comparable<? super T>>//T should be either PK or D
extends BaseBatchBackedScanner<T,Result>{
	
	//inputs
	protected HBaseReaderNode<PK,D,?> node;
	protected DatabeanFieldInfo<PK,D,?> fieldInfo;
	protected byte[] startInclusive;
	protected byte[] endExclusive;
	protected Config config;
	
	
	public BaseHBaseScanner(HBaseReaderNode<PK,D,?> node, DatabeanFieldInfo<PK,D,?> fieldInfo, 
			byte[] startInclusive, byte[] endExclusive, Config pConfig){
		this.node = node;
		this.fieldInfo = node.getFieldInfo();
		this.startInclusive = startInclusive;
		this.endExclusive = endExclusive;
		this.config = Config.nullSafe(pConfig);
		this.config.setIterateBatchSizeIfNull(Config.DEFAULT_ITERATE_BATCH_SIZE);//why is this necessary?
		this.noMoreBatches = false;
	}
	
	@Override
	protected void loadNextBatch(){
		currentBatchIndex = 0;
		byte[] lastRowOfPreviousBatch = startInclusive;
		boolean isStartInclusive = true;//only on the first load
		if(currentBatch != null){
			Result endOfLastBatch = DrCollectionTool.getLast(currentBatch);
			if(endOfLastBatch==null){
				currentBatch = null;
				return;
			}
			lastRowOfPreviousBatch = endOfLastBatch.getRow();
			isStartInclusive = false;
		}
//		currentBatch = node.getResultsInSubRange(lastRowOfPreviousBatch, isStartInclusive, endExclusive, false, config);
		Range<ByteRange> range = Range.create(new ByteRange(lastRowOfPreviousBatch), isStartInclusive, new ByteRange(
				endExclusive), false);
		currentBatch = node.getResultsInSubRange(range, false, config);
		if(DrCollectionTool.size(currentBatch) < config.getIterateBatchSize()){
			noMoreBatches = true;//tell the advance() method not to call this method again
		}
	}
	
}
