package com.hotpads.datarouter.client.imp.hbase.test.pool;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;


@SuppressWarnings("serial")
@Embeddable
public class PoolTestBeanKey extends BasePrimaryKey<PoolTestBeanKey>{

	/********************************* fields ***********************************/

	protected Long id;

	public static class F{
		public static final String
			id = "id";
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new UInt63Field(F.id, id));
	}


	/****************************** constructors *******************************/

	PoolTestBeanKey(){
	}

	public PoolTestBeanKey(Long id) {
		this.id = id;
	}


	/******************************* get/set **************************************/

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}




}