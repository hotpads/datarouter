package com.hotpads.datarouter.client.imp.hbase.batching;

import java.util.List;

import org.apache.hadoop.hbase.client.Result;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchLoader;

public abstract class BaseHBaseBatchLoader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		T> //T will be either PK or D, but not going to express that (or think about how to)
extends BaseBatchLoader<T>{

	protected final HBaseReaderNode<PK,D,F> node;
	protected final List<Field<?>> scatteringPrefix;//will be passed along between scanners tracking this partition
	protected final byte[] scatteringPrefixBytes;//acts as a cache for the comparison of each result
	protected final Range<PK> range;
	protected final Config config;
	protected final Integer iterateBatchSize;//break this out of config for safety
	protected Long batchChainCounter;

	public BaseHBaseBatchLoader(final HBaseReaderNode<PK,D,F> node, final List<Field<?>> scatteringPrefix,
			final Range<PK> range, final Config config, Long batchChainCounter){
		this.node = node;
		this.scatteringPrefix = scatteringPrefix;
		this.scatteringPrefixBytes = FieldTool.getConcatenatedValueBytes(scatteringPrefix, false, false);
		this.range = range;
		this.config = Config.nullSafe(config);
		this.iterateBatchSize = this.config.getIterateBatchSize();
		this.config.setIterateBatchSize(iterateBatchSize);
		this.batchChainCounter = batchChainCounter;
	}

	abstract boolean isKeysOnly();
	abstract T parseHBaseResult(Result result);
	abstract PK getLastPrimaryKeyFromBatch();


	@Override
	public BaseHBaseBatchLoader<PK,D,F,T> call(){
		//these should handle null scattering prefixes and null pks
		boolean incrementStartBytes = !range.getStartInclusive();
		ByteRange startBytes = new ByteRange(node.getKeyBytesWithScatteringPrefix(scatteringPrefix, range.getStart(),
				incrementStartBytes));
		ByteRange endBytes = null;
		if(range.getEnd() != null){
			//if no end bytes, then the differentScatteringPrefix(row) below will stop the scanner
			//don't increment endBytes here.  it will be taken care of later.  hard to follow =(
			endBytes = new ByteRange(node.getKeyBytesWithScatteringPrefix(scatteringPrefix, range.getEnd(), false));
			//TODO adjust endKey for scatteringPrefix and endInclusive???
		}else{
			//TODO stop at the next scatteringPrefix.  we may be way overshooting short scans
		}

		//startInclusive=true because we already adjusted for it above
		Range<ByteRange> byteRange = Range.create(startBytes, true, endBytes, range.getEndInclusive());

		//do the RPC
		List<Result> hbaseRows = node.getResultsInSubRange(byteRange, isKeysOnly(), config);

		List<T> outs = DrListTool.createArrayListWithSize(hbaseRows);
		for(Result row : hbaseRows){
			if (row == null || row.isEmpty()){
				continue;
			}
			if (differentScatteringPrefix(row)){
				break;// we ran into the next scattering prefix partition
			}
			T result = parseHBaseResult(row);
			outs.add(result);
		}
		updateBatch(outs);

		return this;
	}

	protected Range<PK> getNextRange(){
		PK lastPkFromPreviousBatch = getLastPrimaryKeyFromBatch();
		Range<PK> nextRange = Range.create(lastPkFromPreviousBatch, false, range.getEnd(), range.getEndInclusive());
		return nextRange;
	}

	@Override
	public boolean isLastBatch(){
		//refer to the dedicated iterateBatchSize field in case someone changed Config down the line
		return isBatchHasBeenLoaded() && isBatchSmallerThan(iterateBatchSize);
	}


	private boolean differentScatteringPrefix(Result row){
		if (scatteringPrefixBytes == null || row == null){
			return false;
		}
		return ! DrByteTool.equals(scatteringPrefixBytes, 0, scatteringPrefixBytes.length,
				row.getRow(), 0, scatteringPrefixBytes.length);
	}
}
