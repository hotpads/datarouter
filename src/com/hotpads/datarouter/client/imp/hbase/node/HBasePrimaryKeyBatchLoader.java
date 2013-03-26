package com.hotpads.datarouter.client.imp.hbase.node;

import java.util.List;

import org.apache.hadoop.hbase.client.Result;
import org.apache.log4j.Logger;

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
import com.hotpads.util.core.profile.PhaseTimer;


public class HBasePrimaryKeyBatchLoader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseBatchLoader<PK>{
	private static Logger logger = Logger.getLogger(HBasePrimaryKeyBatchLoader.class);
	
	private final HBaseReaderNode<PK,D,F> node;
	private final List<Field<?>> scatteringPrefix;//will be passed along between scanners tracking this partition
	private final byte[] scatteringPrefixBytes;//acts as a cache for the comparison of each result
	private final Range<PK> range;
	private final Config pConfig;
//	private final boolean beforeFirstBatch;
	
	public HBasePrimaryKeyBatchLoader(final HBaseReaderNode<PK,D,F> node, final List<Field<?>> scatteringPrefix,
			final Range<PK> range, final Config pConfig){
		super();
		this.node = node;
		this.scatteringPrefix = scatteringPrefix;
		this.scatteringPrefixBytes = FieldSetTool.getConcatenatedValueBytes(scatteringPrefix, false, false);
		this.range = range;
//		this.beforeFirstBatch = beforeFirstBatch;
		this.pConfig = pConfig;
	}

	@Override
	public HBasePrimaryKeyBatchLoader<PK,D,F> call(){
		PhaseTimer timer = new PhaseTimer();
		//these should handle null scattering prefixes and null pks
		ByteRange startBytes = new ByteRange(node.getKeyBytesWithScatteringPrefix(scatteringPrefix, range.getStart()));
		ByteRange endBytes = null;
		if(range.getEnd() != null){//if no end bytes, then the differentScatteringPrefix(row) below will stop the scanner
			endBytes = new ByteRange(node.getKeyBytesWithScatteringPrefix(scatteringPrefix, range.getEnd()));
		}
		
		//we only care about the scattering prefix part of the range here, not the actual startKey
		Range<ByteRange> byteRange = Range.create(startBytes, shouldIssueStartInclusive(), endBytes, range.getEndInclusive());
		List<Result> hBaseRows = node.getResultsInSubRange(byteRange, true, pConfig);
		List<PK> results = ListTool.createArrayListWithSize(hBaseRows);
		for(Result row : hBaseRows){
			if(row==null || row.isEmpty()){ continue; }
			if(differentScatteringPrefix(row)){ break; }//we ran into the next scattering prefix partition
			PK result = HBaseResultTool.getPrimaryKey(row.getRow(), node.getFieldInfo());
			results.add(result);
		}
		List<PK> pks = results;
		setBatch(pks);
		batchHasBeenLoaded = true;//thread safe by lack of other writers
		int numItems = CollectionTool.size(pks);
		timer.add("loaded "+numItems);
//		logger.warn(timer+" from "+node.getName()+" @"+timer.getItemsPerSecond(numItems)+"/s");
		return this;
	}
	
	@Override
	public boolean isLastBatch(){
		return batchHasBeenLoaded && isBatchSmallerThan(pConfig.getIterateBatchSize());
	}

	@Override
	public BatchLoader<PK> getNextLoader(){
		PK lastPkFromPreviousBatch = CollectionTool.getLast(batch);
		Range<PK> nextRange = Range.create(lastPkFromPreviousBatch, shouldIssueStartInclusive(), range.getEnd(), true);
		return new HBasePrimaryKeyBatchLoader<PK,D,F>(node, scatteringPrefix, nextRange, pConfig);					
	}
	
	private boolean differentScatteringPrefix(Result row){
		if(scatteringPrefixBytes==null || row==null){ return false; }
		return ! ByteTool.equals(scatteringPrefixBytes, 0, scatteringPrefixBytes.length, 
				row.getRow(), 0, scatteringPrefixBytes.length);
	}
}