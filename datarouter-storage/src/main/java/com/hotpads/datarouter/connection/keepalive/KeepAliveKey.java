package com.hotpads.datarouter.connection.keepalive;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;


public class KeepAliveKey extends BasePrimaryKey<KeepAliveKey>{


	/********************************* fields ***********************************/

	protected String id;

	public static final String
		COL_id = "id";

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringField(COL_id, id, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}


	/****************************** constructors *******************************/

	KeepAliveKey(){
	}

	public KeepAliveKey(String id){
		this.id = id;
	}


	/******************************* get/set **************************************/

	public String getId(){
		return id;
	}

	public void setId(String id){
		this.id = id;
	}

}