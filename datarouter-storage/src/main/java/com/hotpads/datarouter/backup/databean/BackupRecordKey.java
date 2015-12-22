package com.hotpads.datarouter.backup.databean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.array.ByteArrayField;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class BackupRecordKey extends BasePrimaryKey<BackupRecordKey>{

	/***************************** columns ******************************/
	private String clientName;
	private String tableName;
	private String subEntityPrefix;
	protected byte[] startKey;
	protected byte[] endKey;
	protected Date created;

	public static final String
		COL_clientName = "clientName",
		COL_tableName = "tableName",
		COL_subEntityPrefix = "subEntityPrefix",
		COL_startKey = "startKey",
		COL_endKey = "endKey",
		COL_created = "created";


	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(COL_clientName, clientName, MySqlColumnType.LENGTH_50),
				new StringField(COL_tableName, tableName, MySqlColumnType.LENGTH_50),
				new StringField(COL_subEntityPrefix, subEntityPrefix, MySqlColumnType.LENGTH_50),
				new ByteArrayField(COL_startKey, startKey, MySqlColumnType.MAX_LENGTH_VARBINARY),
				new ByteArrayField(COL_endKey, endKey, MySqlColumnType.MAX_LENGTH_VARBINARY),
				new LongDateField(COL_created, created));
	}

	/***************************** constructor **************************************/

	public BackupRecordKey(){
	}

	public BackupRecordKey(String clientName, String tableName, String subEntityPrefix, List<Field<?>> startKey,
			List<Field<?>> endKey){
		this.clientName = clientName;
		this.tableName = tableName;
		this.subEntityPrefix = subEntityPrefix;
		this.startKey = FieldTool.getConcatenatedValueBytes(startKey, false, false);
		this.endKey = FieldTool.getConcatenatedValueBytes(endKey, false, false);
		this.created = new Date();
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

	public String getClientName(){
		return clientName;
	}

	public void setClientName(String clientName){
		this.clientName = clientName;
	}

	public String getTableName(){
		return tableName;
	}

	public void setTableName(String tableName){
		this.tableName = tableName;
	}

	public String getSubEntityPrefix(){
		return subEntityPrefix;
	}

	public void setSubEntityPrefix(String subEntityPrefix){
		this.subEntityPrefix = subEntityPrefix;
	}
}