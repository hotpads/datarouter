package com.hotpads.datarouter.backup.databean;

import java.util.Date;
import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
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
	
	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
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
				new StringField(COL_node, node, DEFAULT_STRING_LENGTH),
				new ByteArrayField(COL_startKey, startKey),
				new ByteArrayField(COL_endKey, endKey),
				new LongDateField(COL_created, created));
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