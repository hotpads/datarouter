package com.hotpads.datarouter.backup.databean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;


public class BackupRecord extends BaseDatabean<BackupRecordKey,BackupRecord>{

	/***************************** columns ******************************/
	protected BackupRecordKey key;

	protected Long rawBytes;
	protected Long compressedBytes;
	protected Long numRecords;

	public static final String
		KEY_NAME = "key",
		COL_rawBytes = "rawBytes",
		COL_compressedBytes = "compressedBytes",
		COL_numRecords = "numRecords";


	public static class BackupRecordFielder extends BaseDatabeanFielder<BackupRecordKey,BackupRecord>{

		@Override
		public Class<BackupRecordKey> getKeyFielderClass(){
			return BackupRecordKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(BackupRecord d){
			return Arrays.asList(
					new LongField(COL_rawBytes, d.rawBytes),
					new LongField(COL_compressedBytes, d.compressedBytes),
					new LongField(COL_numRecords, d.numRecords));
		}
	}


	/***************************** constructor **************************************/

	BackupRecord() {
		this.key = new BackupRecordKey();
	}

	public BackupRecord(String clientName, String tableName, String subEntityPrefix, List<Field<?>> startKey,
			List<Field<?>> endKey, Long rawBytes, Long compressedBytes, Long numRecords){
		this.key = new BackupRecordKey(clientName, tableName, subEntityPrefix, startKey, endKey);
		this.rawBytes = rawBytes;
		this.compressedBytes = compressedBytes;
		this.numRecords = numRecords;
	}

	/************************** databean *******************************************/

	@Override
	public Class<BackupRecordKey> getKeyClass() {
		return BackupRecordKey.class;
	}

	@Override
	public BackupRecordKey getKey() {
		return key;
	}


	/***************************** get/set **************************************/

	public Long getRawBytes(){
		return rawBytes;
	}

	public void setRawBytes(Long rawBytes){
		this.rawBytes = rawBytes;
	}

	public Long getCompressedBytes(){
		return compressedBytes;
	}

	public void setCompressedBytes(Long compressedBytes){
		this.compressedBytes = compressedBytes;
	}

	public Long getNumRecords(){
		return numRecords;
	}

	public void setNumRecords(Long numRecords){
		this.numRecords = numRecords;
	}

	public Date getCreated(){
		return key.getCreated();
	}

	public byte[] getEndKey(){
		return key.getEndKey();
	}

	public byte[] getStartKey(){
		return key.getStartKey();
	}

	public void setCreated(Date created){
		key.setCreated(created);
	}

	public void setEndKey(byte[] endKey){
		key.setEndKey(endKey);
	}

	public void setStartKey(byte[] startKey){
		key.setStartKey(startKey);
	}

}
