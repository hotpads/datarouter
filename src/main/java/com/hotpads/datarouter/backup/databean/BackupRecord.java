package com.hotpads.datarouter.backup.databean;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;


@SuppressWarnings("serial")
@Entity()
@AccessType("field")
public class BackupRecord extends BaseDatabean<BackupRecordKey,BackupRecord>{
	
	@Id
	protected BackupRecordKey key;
	
	protected Long rawBytes;
	protected Long compressedBytes;
	protected Long numRecords;
	
	/***************************** columns ******************************/
	
	public static final String
		KEY_NAME = "key",
		COL_rawBytes = "rawBytes",
		COL_compressedBytes = "compressedBytes",
		COL_numRecords = "numRecords";
	
	
	public static class BackupRecordFielder extends BaseDatabeanFielder<BackupRecordKey,BackupRecord>{
		public BackupRecordFielder(){}
		@Override
		public Class<BackupRecordKey> getKeyFielderClass(){
			return BackupRecordKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(BackupRecord d){
			return FieldTool.createList(
					new LongField(COL_rawBytes, d.rawBytes),
					new LongField(COL_compressedBytes, d.compressedBytes),
					new LongField(COL_numRecords, d.numRecords));
		}
	}
	

	/***************************** constructor **************************************/
		
	BackupRecord() {
		this.key = new BackupRecordKey();
	}
	
	public BackupRecord(String node, byte[] startKey, byte[] endKey, Long rawBytes,
			Long compressedBytes, Long numRecords){
		this.key = new BackupRecordKey(node, startKey, endKey);
		this.rawBytes = rawBytes;
		this.compressedBytes = compressedBytes;
		this.numRecords = numRecords;
	}

	/************************** databean *******************************************/

	@Override
	public Class<BackupRecordKey> getKeyClass() {
		return BackupRecordKey.class;
	};
	
	@Override
	public BackupRecordKey getKey() {
		return key;
	}
	
	
	/***************************** index *************************************/
	

	
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

	public String getNode(){
		return key.getNode();
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

	public void setNode(String node){
		key.setNode(node);
	}

	public void setStartKey(byte[] startKey){
		key.setStartKey(startKey);
	}

	
	
}
