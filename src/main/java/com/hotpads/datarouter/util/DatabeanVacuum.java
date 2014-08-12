package com.hotpads.datarouter.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.NumberFormatter;
import com.hotpads.util.core.Predicate;

public class DatabeanVacuum<PK extends PrimaryKey<PK>, D extends Databean<PK, D>>{

	private static final Logger logger = LoggerFactory.getLogger(DatabeanVacuum.class);

	private SortedMapStorageNode<PK, D> storage;
	private Predicate<D> predicate;
	private int batchSize = 100;

	public DatabeanVacuum(SortedMapStorageNode<PK, D> storage, Predicate<D> predicate) {
		this.storage= storage;
		this.predicate = predicate;
	}

	public DatabeanVacuum<PK,D> setStorage(SortedMapStorageNode<PK,D> storage){
		this.storage = storage;
		return this;
	}

	public DatabeanVacuum<PK, D> setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}
	
	public Void run() {
		int deletionCount = 0;
		int scanCount = 0;
		List<PK> pkBatch = ListTool.create();
		for(D databean : storage.scan(null, null)) {
			scanCount++;
//			if(isInterrupted()) { return; }
			if(predicate.check(databean)) {
				pkBatch.add(databean.getKey());
			}
			if(pkBatch.size() >= batchSize ) {
				storage.deleteMulti(pkBatch, null);
				deletionCount += pkBatch.size();
				logger.warn("DatabeanVacuum deleted " + NumberFormatter.addCommas(deletionCount) + " of "
						+ NumberFormatter.addCommas(scanCount) + " " + storage.getDatabeanType().getSimpleName());
				pkBatch = ListTool.create();
			}
		}
		storage.deleteMulti(pkBatch, null);
		deletionCount += pkBatch.size();
		logger.warn("DatabeanVacuum deleted " + NumberFormatter.addCommas(deletionCount) + " of "
				+ NumberFormatter.addCommas(scanCount) + " " + storage.getDatabeanType().getSimpleName());
		return null;
	}

}
