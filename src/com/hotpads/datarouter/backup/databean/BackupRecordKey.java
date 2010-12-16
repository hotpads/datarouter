package com.hotpads.datarouter.backup.databean;

import java.util.Date;
import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.array.ByteArrayField;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

/********************************* indexes ***********************************/

@SuppressWarnings("serial")
@Embeddable
public class BackupRecordKey extends BasePrimaryKey<BackupRecordKey>{
	
	protected String node;
	protected byte[] startKey;
	protected byte[] endKey;
	protected Date created;
	
	BackupRecordKey(){
	}
	
	public BackupRecordKey(String node, byte[] startKey, byte[] endKey){
		this.node = node;
		this.startKey = startKey;
		this.endKey = endKey;
		this.created = new Date();
	}

	public static final String
		COL_node = "node",
		COL_startKey = "startKey",
		COL_endKey = "endKey",
		COL_created = "created";


	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringField(BackupRecord.KEY_NAME, COL_node, node),
				new ByteArrayField(BackupRecord.KEY_NAME, COL_startKey, startKey),
				new ByteArrayField(BackupRecord.KEY_NAME, COL_endKey, endKey),
				new LongDateField(BackupRecord.KEY_NAME, COL_created, created));
	}


	
	/***************************** get/set *******************************/
	

	public byte[] getStartKey(){
		return startKey;
	}

	public void setStartKey(byte[] startKey){
		this.startKey = startKey;
	}

	public byte[] getEndKey(){
		return endKey;
	}

	public void setEndKey(byte[] endKey){
		this.endKey = endKey;
	}

	public Date getCreated(){
		return created;
	}

	public void setCreated(Date created){
		this.created = created;
	}

	public String getNode(){
		return node;
	}

	public void setNode(String node){
		this.node = node;
	}

	
}