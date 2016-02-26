package com.hotpads.datarouter.test.client.txn;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;


@SuppressWarnings("serial")
@Embeddable
public class TxnBeanKey extends BasePrimaryKey<TxnBeanKey>{


	/********************************* fields ***********************************/

	@Column(length=MySqlColumnType.MAX_KEY_LENGTH_UTF8MB4)
	protected String id;

	public static final String
		COL_id = "id";

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(COL_id, id, MySqlColumnType.MAX_KEY_LENGTH_UTF8MB4));
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