package com.hotpads.datarouter.client.imp.jdbc.op.read;

import java.util.Optional;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.index.UniqueIndexReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.util.core.collections.Range;

public class SortedStorageCountingHelper{

	private static final int BATCH_SIZE = 10000;

	public static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends UniqueIndexEntry<IK,IE,PK,D>>
	Long count(UniqueIndexReader<PK,D,IK,IE> node, Range<IK> range){
		range = Range.nullSafe(range);
		IK startKey = null;
		long count = 0;
		for(IK key : node.scanKeys(range, new Config().setIterateBatchSize(BATCH_SIZE).setLimit(BATCH_SIZE))){
			startKey = key;
			count++;
		}
		if(count < BATCH_SIZE){
			return count;
		}
		Config batchConfig = new Config().setLimit(1).setOffset(BATCH_SIZE);
		Optional<IK> currentKey;
		do{
			Range<IK> batchRange = new Range<>(startKey, true, range.getEnd(), range.getEndInclusive());
			currentKey = node.streamKeys(batchRange, batchConfig).findFirst();
			if(currentKey.isPresent()){
				count += BATCH_SIZE;
				startKey = currentKey.get();
			}
		}while(currentKey.isPresent());
		return count += node.streamKeys(new Range<>(startKey, false, range.getEnd(), range.getEndInclusive()),
				new Config().setIterateBatchSize(BATCH_SIZE)).count();
	}

}
