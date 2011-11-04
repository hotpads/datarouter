package com.hotpads.datarouter.client.imp.hbase.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.iterable.PeekableIterable;
import com.hotpads.util.core.iterable.PeekableIterator;

public class HBaseManualPrimaryKeyScanner<PK extends PrimaryKey<PK>>
implements PeekableIterable<PK>{
	
	protected SortedStorageReader<PK,?> node;
	protected DatabeanFieldInfo<PK,?,?> fieldInfo;
	protected HTable hTable;
	protected byte[] startInclusive;
	protected byte[] endExclusive;
	protected Config config;
	
	public HBaseManualPrimaryKeyScanner(SortedStorageReader<PK,?> node,
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
		
		protected SortedStorageReader<PK,?> node;
		protected DatabeanFieldInfo<PK,?,?> fieldInfo;
		protected HTable hTable;
		protected byte[] startInclusive;
		protected byte[] endExclusive;
		protected Config config;
		protected int rowsPerBatch;
		
		protected byte[] lastRow;
		protected ArrayList<Result> currentBatch;
		protected int nextBatchIndex = 0;
		protected int numBatchesLoaded = 0;
		boolean foundEndOfData;
		
		protected PK peeked;
		
		public HBaseManualPrimaryKeyIterator(SortedStorageReader<PK,?> node, 
				DatabeanFieldInfo<PK,?,?> fieldInfo, HTable hTable, 
				byte[] startInclusive, byte[] endExclusive, Config pConfig){
			this.node = node;
			this.fieldInfo = fieldInfo;
			this.hTable = hTable;
			this.startInclusive = startInclusive;
			this.endExclusive = endExclusive;
			this.config = Config.nullSafe(config);
			if(this.config.getIterateBatchSize()==null){ 
				this.config.setIterateBatchSize(HBaseReaderNode.DEFAULT_ITERATE_BATCH_SIZE); 
			}
			foundEndOfData = false;
			loadNextBatch();
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
			if(nextBatchIndex > CollectionTool.size(currentBatch)){
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
			if(!hasNext()){
				return null;
			}
			Result nextResult = currentBatch.get(nextBatchIndex);
			++nextBatchIndex;
			PK pk = HBaseResultTool.getPrimaryKey(nextResult.getRow(), fieldInfo);
			return pk;
		}
		
		protected void loadNextBatch(){
			byte[] lastRowOfPreviousBatch = startInclusive;
			if(batch != null){
				Result endOfLastBatch = CollectionTool.getLast(batch);
				if(endOfLastBatch==null){
					batch = null;
					return;
				}
				lastRowOfPreviousBatch = endOfLastBatch.getRow();
			}
			List<PK> currentBatch = node.getBatchOfKeysInRange(lastRowOfPreviousBatch, endExclusive, config)
			scan.setFilter(new FirstKeyOnlyFilter());
			try{
				ResultScanner scanner = hTable.getScanner(scan);
				Result result;
				while((result = scanner.next()) != null){
					batch.add(result);
				}
			}catch(IOException ioe){
				throw new DataAccessException(ioe);
			}
			startInclusive = false;//always false after first batch
		}

		protected void loadNextBatch(){
			if(foundEndOfData){ return; }
			config.setLimit(config.getIterateBatchSize());
			List<PK> currentBatch = node.getBatchOfKeysInRange(lastKey, startInclusive, end, endInclusive, config);
			currentBatchIterator = currentBatch.iterator();
			++numBatchesLoaded;
			if(CollectionTool.size(currentBatch) < config.getLimit()){
				foundEndOfData = true;
				return; 
			}
			lastKey = CollectionTool.getLast(currentBatch);
			startInclusive = false;//always false after first batch
		}
		
		@Override
		public void remove() {
			throw new RuntimeException("can't remove from this iterator");
		}
	}
	
}
