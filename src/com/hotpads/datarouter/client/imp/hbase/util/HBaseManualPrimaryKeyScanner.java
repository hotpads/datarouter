package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.List;

import junit.framework.Assert;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.iterable.PeekableIterable;
import com.hotpads.util.core.iterable.PeekableIterator;

public class HBaseManualPrimaryKeyScanner<PK extends PrimaryKey<PK>>
implements PeekableIterable<PK>{
	
	protected HBaseReaderNode<PK,?,?> node;
	protected DatabeanFieldInfo<PK,?,?> fieldInfo;
	protected HTable hTable;
	protected byte[] startInclusive;
	protected byte[] endExclusive;
	protected Config config;
	
	public HBaseManualPrimaryKeyScanner(HBaseReaderNode<PK,?,?> node,
			DatabeanFieldInfo<PK,?,?> fieldInfo, HTable hTable, 
			byte[] startInclusive, byte[] endExclusive,	final Config pConfig){
		this.node = node;
		this.fieldInfo = fieldInfo;
		this.hTable = hTable;
		this.startInclusive = startInclusive;
		this.endExclusive = endExclusive;
		this.config = Config.nullSafe(pConfig);
	}
	
	@Override
	public PeekableIterator<PK> iterator() {
		return new HBaseManualPrimaryKeyIterator<PK>(node, fieldInfo, hTable, 
				startInclusive, endExclusive, config);
	}
	
	
	
	public static class HBaseManualPrimaryKeyIterator<PK extends PrimaryKey<PK>>
	implements PeekableIterator<PK>{
		
		protected HBaseReaderNode<PK,?,?> node;
		protected DatabeanFieldInfo<PK,?,?> fieldInfo;
		protected HTable hTable;
		protected byte[] startInclusive;
		protected byte[] endExclusive;
		protected Config config;
		protected int rowsPerBatch;
		
		protected byte[] lastRow;
		protected List<Result> currentBatch;
		protected int nextBatchIndex = 0;
		protected int numBatchesLoaded = 0;
		boolean foundEndOfData;
		
		protected PK peeked;
		
		
		public HBaseManualPrimaryKeyIterator(HBaseReaderNode<PK,?,?> node, 
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
		public PK peek(){
			if(peeked!=null){
				return peeked;
			}
			if(hasNext()){
				peeked = next();
			}
			return peeked;
		}
		
		@Override
		public boolean hasNext() {
			if(peeked!=null){ return true; }
			Assert.assertFalse(nextBatchIndex > CollectionTool.size(currentBatch));
			if(nextBatchIndex == CollectionTool.size(currentBatch)){
				loadNextBatch();
			}
			return nextBatchIndex < CollectionTool.size(currentBatch);
		}
		
		@Override
		public PK next() {
			if(peeked!=null){
				PK next = peeked;
				peeked = null;
				return next;
			}
			if(!hasNext()){//triggers loadNextBatch
				return null;
			}
			Result nextResult = currentBatch.get(nextBatchIndex);
			++nextBatchIndex;
			PK pk = HBaseResultTool.getPrimaryKey(nextResult.getRow(), fieldInfo);
			return pk;
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
			System.out.println("got "+CollectionTool.size(currentBatch));
			nextBatchIndex = 0;
			peeked = null;
			if(CollectionTool.size(currentBatch) < config.getIterateBatchSize()){
				foundEndOfData = true;
			}
		}
		
		@Override
		public void remove() {
			throw new RuntimeException("can't remove from this iterator");
		}
	}
	
}
