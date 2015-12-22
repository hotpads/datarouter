package com.hotpads.datarouter.op.scan;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.storage.view.index.IndexEntryTool;
import com.hotpads.util.core.iterable.BatchingIterable;
import com.hotpads.util.core.iterable.scanner.sorted.BaseHoldingScanner;

public class ManagedIndexDatabeanScanner<
		PK extends PrimaryKey<PK>, 
		D extends Databean<PK, D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK, IE, PK, D>>
extends BaseHoldingScanner<D>{
	
	private Iterator<List<IE>> indexBatchIterator;
	private MapStorageReader<PK, D> mainNode;

	private Config config;

	private Iterator<IE> indexIterator;
	private Map<PK, D> keyToDatabeans;
	
	public ManagedIndexDatabeanScanner(MapStorageReader<PK, D> mainNode, Iterable<IE> indexIterable, Config config){
		this.mainNode = mainNode;
		this.config = config;
		Integer batchSize = Config.nullSafe(config).getIterateBatchSize();
		BatchingIterable<IE> batchingIndexIterable = new BatchingIterable<>(indexIterable, batchSize);
		this.indexBatchIterator = batchingIndexIterable.iterator();
	}
	
	@Override
	public boolean advance(){
		if(indexIterator == null || !indexIterator.hasNext()){
			if(!doLoad()){
				return false;
			}
		}
		current = keyToDatabeans.get(indexIterator.next().getTargetKey());
		return true;
	}
	
	public boolean doLoad(){
		if(!indexBatchIterator.hasNext()){
			return false;
		}
		List<IE> indexes = indexBatchIterator.next();
		keyToDatabeans = DatabeanTool.getByKey(mainNode.getMulti(IndexEntryTool.getPrimaryKeys(indexes), config));
		indexIterator = indexes.iterator();
		
		return true;
	}
	
}
