package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.List;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.iterable.scanner.BaseSortedScanner;

//copy/pasted from HBaseManualPrimaryKeyScanner.  should probably be abstracted
public class HBaseManualDatabeanScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseSortedScanner<D>{
	
	protected HBaseReaderNode<PK,D,?> node;
	protected DatabeanFieldInfo<PK,D,?> fieldInfo;
	protected HTable hTable;
	protected byte[] startInclusive;
	protected byte[] endExclusive;
	protected Config config;
	protected int rowsPerBatch;
	
	protected byte[] lastRow;
	protected List<Result> currentBatch;
	protected int currentBatchIndex = 0;
	protected D current;
	boolean foundEndOfData;
	
	
	public HBaseManualDatabeanScanner(HBaseReaderNode<PK,D,?> node, 
			DatabeanFieldInfo<PK,D,?> fieldInfo, HTable hTable, 
			byte[] startInclusive, byte[] endExclusive, Config pConfig){
		this.node = node;
		this.fieldInfo = node.getFieldInfo();
		this.hTable = hTable;
		this.startInclusive = startInclusive;
		this.endExclusive = endExclusive;
		this.config = Config.nullSafe(pConfig);
		this.config.setIterateBatchSizeIfNull(HBaseReaderNode.DEFAULT_ITERATE_BATCH_SIZE); 
		foundEndOfData = false;
//			loadNextBatch();
	}
	
	@Override
	public D getCurrent() {
		return current;
	}
	
	@Override
	public boolean advance() {
//		if(foundEndOfData){ return false; }
		if(currentBatchIndex == CollectionTool.size(currentBatch)){
			loadNextBatch();
		}
		if(CollectionTool.isEmpty(currentBatch)){ return false; }
		Result currentResult = currentBatch.get(currentBatchIndex);
		++currentBatchIndex;
		current = HBaseResultTool.getDatabean(currentResult, fieldInfo);
//		System.out.println(current);
		return true;
	}
	
	protected void loadNextBatch(){
		currentBatchIndex = 0;
		if(foundEndOfData){ 
			currentBatch = null;
			return; 
		}
		byte[] lastRowOfPreviousBatch = startInclusive;
		boolean isStartInclusive = true;//only on the first load
		if(currentBatch != null){
			Result endOfLastBatch = CollectionTool.getLast(currentBatch);
			if(endOfLastBatch==null){
				currentBatch = null;
				return;
			}
			lastRowOfPreviousBatch = endOfLastBatch.getRow();
			isStartInclusive = false;
		}
		currentBatch = node.getResultsInSubRange(lastRowOfPreviousBatch, isStartInclusive, endExclusive, false, config);
//		if(true){
//			if(CollectionTool.notEmpty(currentBatch)){
//				PK debug = HBaseResultTool.getPrimaryKey(CollectionTool.getLast(currentBatch).getRow(), fieldInfo);
//				System.out.println("got "+CollectionTool.size(currentBatch)+" "+debug);
//			}
//		}
		if(CollectionTool.size(currentBatch) < config.getIterateBatchSize()){
			foundEndOfData = true;
		}
	}
	
}
