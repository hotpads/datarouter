package com.hotpads.datarouter.client.imp.hbase.batching.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.client.Result;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.field.compare.FieldSetRangeFilter;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.core.DrIterableTool;
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

	public HBaseEntityDatabeanBatchLoader(final HBaseSubEntityReaderNode<EK,E,PK,D,F> node, int partition,
			final byte[] partitionBytes, final Range<PK> range, final Config config, Long batchChainCounter){
		super(node, partition, partitionBytes, range, config, batchChainCounter);
	}

	@Override
	protected boolean isKeysOnly(){
		return false;
	}

	@Override
	protected List<D> parseHBaseResult(Result result){
		//the first and last entity may include results outside the range
		List<D> unfilteredResults = node.getResultParser().getDatabeansWithMatchingQualifierPrefix(result, null);
		List<D> filteredResults = new ArrayList<>();
		for(D candidate : DrIterableTool.nullSafe(unfilteredResults)){
			if(FieldSetRangeFilter.include(candidate.getKey(), range)){
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
		return new HBaseEntityDatabeanBatchLoader<>(node, partition, partitionBytes, nextRange, config,
				batchChainCounter + 1);
	}
}