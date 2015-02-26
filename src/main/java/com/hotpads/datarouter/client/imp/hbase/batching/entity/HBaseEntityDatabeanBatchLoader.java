package com.hotpads.datarouter.client.imp.hbase.batching.entity;

import java.util.List;

import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.field.compare.EndOfRangeFieldSetComparator;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BatchLoader;

//TODO merge this with PrimaryKeyBatchLoader.  slightly more complicated than first glance with generics
public class HBaseEntityDatabeanBatchLoader<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHBaseEntityBatchLoader<EK,E,PK,D,F,D>{
	private static Logger logger = LoggerFactory.getLogger(HBaseEntityDatabeanBatchLoader.class);
		
	public HBaseEntityDatabeanBatchLoader(final HBaseSubEntityReaderNode<EK,E,PK,D,F> node, int partition,
			final Range<PK> range, final Config pConfig, Long batchChainCounter){
		super(node, partition, range, pConfig, batchChainCounter);
	}
	
	@Override
	protected boolean isKeysOnly(){
		return false;
	}
	
	@Override
	protected List<D> parseHBaseResult(Result result){
		//the first and last entity may include results outside the range
		List<D> unfilteredResults = node.getResultParser().getDatabeansWithMatchingQualifierPrefix(result);
		List<D> filteredResults = DrListTool.createArrayList();
		for(D candidate : DrIterableTool.nullSafe(unfilteredResults)){
			if(EndOfRangeFieldSetComparator.isCandidateIncludedForEndOfRange(candidate.getKey(), range.getEnd(), 
					range.getEndInclusive())){
				filteredResults.add(candidate);
			}
		}
		return filteredResults;
	}
	
	@Override
	protected PK getLastPrimaryKeyFromBatch(){
		PK last = getLast()==null ? null : getLast().getKey();
		return last;
	}

	@Override
	public BatchLoader<D> getNextLoader(){
		Range<PK> nextRange = getNextRange();
		return new HBaseEntityDatabeanBatchLoader<EK,E,PK,D,F>(node, partition, nextRange, config, batchChainCounter + 1);					
	}
}