package com.hotpads.datarouter.client.imp.hbase.batching;

import java.util.List;

import org.apache.hadoop.hbase.client.Result;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchLoader;

public abstract class BaseHBaseBatchLoader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		T> //T will be either PK or D, but not going to express that (or think about how to)
extends BaseBatchLoader<T>{
	private static Logger logger = Logger.getLogger(BaseBatchLoader.class);
	
	protected final HBaseReaderNode<PK,D,F> node;
	protected final List<Field<?>> scatteringPrefix;//will be passed along between scanners tracking this partition
	protected final byte[] scatteringPrefixBytes;//acts as a cache for the comparison of each result
	protected final Range<PK> range;
	protected final Config pConfig;
	protected Long batchChainCounter;
	
	public BaseHBaseBatchLoader(final HBaseReaderNode<PK,D,F> node, final List<Field<?>> scatteringPrefix,
			final Range<PK> range, final Config pConfig, Long batchChainCounter){
		this.node = node;
		this.scatteringPrefix = scatteringPrefix;
		this.scatteringPrefixBytes = FieldSetTool.getConcatenatedValueBytes(scatteringPrefix, false, false);
		this.range = range;
		this.pConfig = pConfig;
		this.batchChainCounter = batchChainCounter;
	}

	protected abstract boolean isKeysOnly();
	protected abstract T parseHBaseResult(Result result);
	protected abstract PK getLastPrimaryKeyFromBatch();
	

	@Override
	public BaseHBaseBatchLoader<PK,D,F,T> call(){
		logger.warn("dispatching call "+batchChainCounter+" for scatteringPrefix="+ArrayTool.toCsvString(scatteringPrefixBytes));
		
		//these should handle null scattering prefixes and null pks
		ByteRange startBytes = new ByteRange(node.getKeyBytesWithScatteringPrefix(scatteringPrefix, range.getStart()));
		ByteRange endBytes = null;
		if(range.getEnd() != null){//if no end bytes, then the differentScatteringPrefix(row) below will stop the scanner
			endBytes = new ByteRange(node.getKeyBytesWithScatteringPrefix(scatteringPrefix, range.getEnd()));
		}
		//we only care about the scattering prefix part of the range here, not the actual startKey
		Range<ByteRange> byteRange = Range.create(startBytes, range.getStartInclusive(), endBytes, 
				range.getEndInclusive());
		
		//do the RPC
		List<Result> hBaseRows = node.getResultsInSubRange(byteRange, isKeysOnly(), pConfig);
		
		List<T> outs = ListTool.createArrayListWithSize(hBaseRows);
		for(Result row : hBaseRows){
			if(row==null || row.isEmpty()){ continue; }
			if(differentScatteringPrefix(row)){ break; }//we ran into the next scattering prefix partition
			T result = parseHBaseResult(row);
			outs.add(result);
		}
		updateBatch(outs);

		logger.warn("completed call "+batchChainCounter+" for scatteringPrefix="+ArrayTool.toCsvString(scatteringPrefixBytes));
		return this;
	}
	
	protected Range<PK> getNextRange(){
		PK lastPkFromPreviousBatch = getLastPrimaryKeyFromBatch();
		Range<PK> nextRange = Range.create(lastPkFromPreviousBatch, false, range.getEnd(), true);
		return nextRange;
	}
	
	@Override
	public boolean isLastBatch(){
		return isBatchHasBeenLoaded() && isBatchSmallerThan(pConfig.getIterateBatchSize());
	}

	
	//TODO same as PrimaryKeyBatchLoader.differentScatteringPrefix
	protected boolean differentScatteringPrefix(Result row){
		if(scatteringPrefixBytes==null || row==null){ return false; }
		return ! ByteTool.equals(scatteringPrefixBytes, 0, scatteringPrefixBytes.length, 
				row.getRow(), 0, scatteringPrefixBytes.length);
	}
}
