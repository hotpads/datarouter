package com.hotpads.datarouter.backup;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.backup.databean.BackupRecord;
import com.hotpads.datarouter.backup.databean.BackupRecordKey;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.NumberFormatter;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.number.VarLong;

public abstract class BackupRegion<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{
	protected static Logger logger = Logger.getLogger(BackupRegion.class);

	public static final int GZIP_BUFFER_BYTES = 256<<10;
	
	protected DataRouter router;
	protected SortedStorageReaderNode<PK,D> node;
	protected PK startKeyInclusive;
	protected PK endKeyExclusive;
	protected MapStorage<BackupRecordKey,BackupRecord> backupRecordNode;
	
	protected OutputStream os;

	protected Long rawBytes = 0L;
	protected Long compressedBytes = 0L;
	protected Long numRecords = 0L;
	
	public BackupRegion(DataRouter router, SortedStorageReaderNode<PK,D> node, 
			PK startKeyInclusive, PK endKeyExclusive,
			MapStorage<BackupRecordKey,BackupRecord> backupRecordNode){
		this.router = router;
		this.node = node;
		this.startKeyInclusive = startKeyInclusive;
		this.endKeyExclusive = endKeyExclusive;
		this.backupRecordNode = backupRecordNode;
	}
	
	public abstract void execute() throws IOException;
	
	protected void exportWithoutClosingOutputStream() throws IOException{
		Iterable<D> iterable = node.scan(Range.create(startKeyInclusive, true, endKeyExclusive, false), 
				new Config().setIterateBatchSize(1000).setNumAttempts(30).setTimeout(10, TimeUnit.SECONDS));
		if( ! node.getFieldInfo().getFieldAware()){ throw new IllegalArgumentException("databeans must be field aware"); }
		for(D databean : IterableTool.nullSafe(iterable)){
			//include zero-length fields in key bytes
			byte[] bytes = DatabeanTool.getBytes(databean, node.getFieldInfo().getSampleFielder());
			VarLong length = new VarLong(ArrayTool.length(bytes));
			os.write(length.getBytes());
			os.write(bytes);
			++numRecords;
			rawBytes += length.getValue() + length.getNumBytes();
			if(numRecords % 10000 == 0){
				String numRecordsString = NumberFormatter.addCommas(numRecords);
				String numBytesString = NumberFormatter.addCommas(rawBytes)+"b";
				logger.warn("exported "+numRecordsString+", "+numBytesString+" from "+node.getName());
			}
		}
	}
	
	protected void recordMeta(){
//		byte[] startBytes = startKeyInclusive==null?null:startKeyInclusive.getBytes(true);
//		byte[] endBytes = endKeyExclusive==null?null:endKeyExclusive.getBytes(true);
//		BackupRecord record = new BackupRecord(node.getName(), startBytes, endBytes, rawBytes, compressedBytes, numRecords);
//		backupRecordNode.put(record, null);
	}

	public Long getRawBytes(){
		return rawBytes;
	}

	public Long getCompressedBytes(){
		return compressedBytes;
	}

	public Long getNumRecords(){
		return numRecords;
	}
	
	
}





