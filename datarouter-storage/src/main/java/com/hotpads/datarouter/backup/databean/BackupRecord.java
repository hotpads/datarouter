package com.hotpads.datarouter.backup.databean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;


public class BackupRecord extends BaseDatabean<BackupRecordKey,BackupRecord>{

	private final BackupRecordKey key;

	private String bucketName;
	private Long rawBytes;
	private Long compressedBytes;
	private Long numRecords;

	public static class Fields{
		public static final String
			KEY_NAME = "key",
			bucketName = "bucketName",
			rawBytes = "rawBytes",
			compressedBytes = "compressedBytes",
			numRecords = "numRecords";
	}


	public static class BackupRecordFielder extends BaseDatabeanFielder<BackupRecordKey,BackupRecord>{

		@Override
		public Class<BackupRecordKey> getKeyFielderClass(){
			return BackupRecordKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(BackupRecord databean){
			return Arrays.asList(
					new StringField(Fields.bucketName, databean.bucketName, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new LongField(Fields.rawBytes, databean.rawBytes),
					new LongField(Fields.compressedBytes, databean.compressedBytes),
					new LongField(Fields.numRecords, databean.numRecords));
		}
	}


	/***************************** constructor **************************************/

	BackupRecord() {
		this.key = new BackupRecordKey(null, null, null, null, null);
	}

	public BackupRecord(String clientName, String tableName, String subEntityPrefix, List<Field<?>> startKey,
			List<Field<?>> endKey, String bucketName, Long rawBytes, Long compressedBytes, Long numRecords){
		this.key = new BackupRecordKey(clientName, tableName, subEntityPrefix, startKey, endKey);
		this.bucketName = bucketName;
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

	public String getClientName(){
		return key.getClientName();
	}

	public String getTableName(){
		return key.getTableName();
	}

	public String getSubEntityPrefix(){
		return key.getSubEntityPrefix();
	}

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

	public String getBucketName(){
		return bucketName;
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
