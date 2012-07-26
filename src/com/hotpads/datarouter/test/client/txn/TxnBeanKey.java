package com.hotpads.datarouter.test.client.txn;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;


@SuppressWarnings("serial")
@Embeddable
public class TxnBeanKey extends BasePrimaryKey<TxnBeanKey>{

	/********************************* fields ***********************************/
	
	protected String id;

	public static final String
		COL_id = "id";
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringField(COL_id, id,255));
	}
	
	
	/****************************** constructors *******************************/
	
	TxnBeanKey(){
	}
	
	public TxnBeanKey(String id) {
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