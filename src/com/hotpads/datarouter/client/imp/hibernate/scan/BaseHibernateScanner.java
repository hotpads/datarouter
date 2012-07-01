package com.hotpads.datarouter.client.imp.hibernate.scan;

import org.apache.hadoop.hbase.client.Result;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.iterable.scanner.BaseBatchingSortedScanner;

public abstract class BaseHibernateScanner<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		T extends Comparable<? super T>>//T should be either PK or D
extends BaseBatchingSortedScanner<T,Result>{
	
	//inputs
	protected HibernateReaderNode<PK,D,?> node;
	protected DatabeanFieldInfo<PK,D,?> fieldInfo;
	protected PK startInclusive;
	protected PK endExclusive;
	protected Config config;
	
	
	public BaseHibernateScanner(HibernateReaderNode<PK,D,?> node, DatabeanFieldInfo<PK,D,?> fieldInfo, 
			PK startInclusive, PK endExclusive, Config pConfig){
		this.node = node;
		this.fieldInfo = node.getFieldInfo();
		this.startInclusive = startInclusive;
		this.endExclusive = endExclusive;
		this.config = Config.nullSafe(pConfig);
		this.config.setIterateBatchSizeIfNull(HBaseReaderNode.DEFAULT_ITERATE_BATCH_SIZE);//why is this necessary?
		this.noMoreBatches = false;
	}
	
	@Override
	protected void loadNextBatch(){
//		currentBatchIndex = 0;
//		PK lastRowOfPreviousBatch = startInclusive;
//		boolean isStartInclusive = true;//only on the first load
//		if(currentBatch != null){
//			PK endOfLastBatch = CollectionTool.getLast(currentBatch);
//			if(endOfLastBatch==null){
//				currentBatch = null;
//				return;
//			}
//			lastRowOfPreviousBatch = endOfLastBatch.getRow();
//			isStartInclusive = false;
//		}
//		currentBatch = node.getResultsInSubRange(lastRowOfPreviousBatch, isStartInclusive, endExclusive, false, config);
//		if(CollectionTool.size(currentBatch) < config.getIterateBatchSize()){
//			noMoreBatches = true;//tell the advance() method not to call this method again
//		}
	}
	
}
