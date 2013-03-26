package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.List;

import org.apache.hadoop.hbase.client.Result;

import com.hotpads.datarouter.client.imp.hbase.util.HBaseResultTool;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BaseBatchLoader;
import com.hotpads.util.core.iterable.scanner.batch.BatchLoader;

//TODO merge this with PrimaryKeyBatchLoader.  slightly more complicated than first glance with generics
public class HBaseDatabeanBatchLoader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseBatchLoader<D>{
	
	private final HBaseReaderNode<PK,D,F> node;
	private final List<Field<?>> scatteringPrefix;//will be passed along between scanners tracking this partition
	private final byte[] scatteringPrefixBytes;//acts as a cache for the comparison of each result
	private final Range<PK> range;
	private final Config pConfig;
	private final boolean isFirstBatch;
	
	public HBaseDatabeanBatchLoader(final HBaseReaderNode<PK,D,F> node, final List<Field<?>> scatteringPrefix,
			final Range<PK> range, boolean isFirstBatch, final Config pConfig){
		this.node = node;
		this.scatteringPrefix = scatteringPrefix;
		this.scatteringPrefixBytes = FieldSetTool.getConcatenatedValueBytes(scatteringPrefix, false, false);
		this.range = range;
		this.isFirstBatch = isFirstBatch;
		this.pConfig = pConfig;
	}

	@Override
	public HBaseDatabeanBatchLoader<PK,D,F> call(){
		//these should handle null scattering prefixes and null pks
		ByteRange startBytes = new ByteRange(node.getKeyBytesWithScatteringPrefix(scatteringPrefix, range.getStart()));
		ByteRange endBytes = new ByteRange(node.getKeyBytesWithScatteringPrefix(scatteringPrefix, range.getEnd()));
		
		//we only care about the scattering prefix part of the range here, not the actual startKey
		Range<ByteRange> byteRange = Range.create(startBytes, isFirstBatch, endBytes, range.getEndInclusive());
		List<Result> hBaseRows = node.getResultsInSubRange(byteRange, false, pConfig);
		List<D> databeans = ListTool.createArrayListWithSize(hBaseRows);
		for(Result row : hBaseRows){
			if(row==null || row.isEmpty()){ continue; }
			if(differentScatteringPrefix(row)){ break; }//we ran into the next scattering prefix partition
			D result = HBaseResultTool.getDatabean(row, node.getFieldInfo());
			databeans.add(result);
		}
		setBatch(databeans);
		return this;
	}
	
	@Override
	public boolean isLastBatch(){
		return batchHasBeenLoaded && isBatchSmallerThan(pConfig.getIterateBatchSize());
	}

	@Override
	public BatchLoader<D> getNextLoader(){
		PK lastPkFromPreviousBatch = CollectionTool.isEmpty(batch) ? null : CollectionTool.getLast(batch).getKey();
		Range<PK> nextRange = Range.create(lastPkFromPreviousBatch, isFirstBatch, range.getEnd(), true);
		return new HBaseDatabeanBatchLoader<PK,D,F>(node, scatteringPrefix, nextRange, false, pConfig);					
	}
	
	//TODO same as PrimaryKeyBatchLoader.differentScatteringPrefix
	private boolean differentScatteringPrefix(Result row){
		if(scatteringPrefixBytes==null || row==null){ return false; }
		return ! ByteTool.equals(scatteringPrefixBytes, 0, scatteringPrefixBytes.length, 
				row.getRow(), 0, scatteringPrefixBytes.length);
	}
}