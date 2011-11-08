package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.List;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.iterable.scanner.BaseSortedScanner;

public class HBaseManualPrimaryKeyScanner<PK extends PrimaryKey<PK>>
extends BaseSortedScanner<PK>{
	
	protected HBaseReaderNode<PK,?,?> node;
	protected DatabeanFieldInfo<PK,?,?> fieldInfo;
	protected HTable hTable;
	protected byte[] startInclusive;
	protected byte[] endExclusive;
	protected Config config;
	protected int rowsPerBatch;
	
	protected byte[] lastRow;
	protected List<Result> currentBatch;
	protected int currentBatchIndex = 0;
	protected PK current;
	boolean foundEndOfData;
	
	
	public HBaseManualPrimaryKeyScanner(HBaseReaderNode<PK,?,?> node, 
			DatabeanFieldInfo<PK,?,?> fieldInfo, HTable hTable, 
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
	public PK getCurrent() {
		return current;
	}
	
	@Override
	public boolean advance() {
		if(currentBatchIndex == CollectionTool.size(currentBatch)){
			loadNextBatch();
		}
		if(CollectionTool.isEmpty(currentBatch)){ return false; }
		Result currentResult = currentBatch.get(currentBatchIndex);
		++currentBatchIndex;
		current = HBaseResultTool.getPrimaryKey(currentResult.getRow(), fieldInfo);
		return true;
	}
	
	protected void loadNextBatch(){
		if(foundEndOfData){ return; }
		byte[] lastRowOfPreviousBatch = startInclusive;
		if(currentBatch != null){
			Result endOfLastBatch = CollectionTool.getLast(currentBatch);
			if(endOfLastBatch==null){
				currentBatch = null;
				return;
			}
			lastRowOfPreviousBatch = endOfLastBatch.getRow();
		}
		currentBatch = node.getKeysInSubRange(lastRowOfPreviousBatch, endExclusive, config);
		if(true){
			current = HBaseResultTool.getPrimaryKey(currentBatch.get(0).getRow(), fieldInfo);
			System.out.println("got "+CollectionTool.size(currentBatch)+" "+current.getPersistentString());
		}
		currentBatchIndex = 0;
		if(CollectionTool.size(currentBatch) < config.getIterateBatchSize()){
			foundEndOfData = true;
		}
	}
	
}
