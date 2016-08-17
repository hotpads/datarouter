package com.hotpads.datarouter.test.node.basic.manyfield;

import java.util.List;


import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class ManyFieldBeanKey extends BasePrimaryKey<ManyFieldBeanKey>{

	/****************************** fields *******************************************/

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


	/***************************** constructors *************************************/

	public ManyFieldBeanKey(){//no-arg and public
		this.id = UInt63Field.nextPositiveRandom();
	}

	public ManyFieldBeanKey(Long id) {
		this.id = id;
	}


	/******************************** get/set *******************************************/

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}


}