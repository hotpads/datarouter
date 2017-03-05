package com.hotpads.datarouter.test.node.basic.manyfield;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63FieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class ManyFieldBeanKey extends BasePrimaryKey<ManyFieldBeanKey>{

	/****************************** fields *******************************************/

	protected Long id;

	public static class FieldKeys{
		public static final UInt63FieldKey id = new UInt63FieldKey("id");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new UInt63Field(FieldKeys.id, id));
	}

	/***************************** constructors *************************************/

	public ManyFieldBeanKey(){// no-arg and public
		this.id = UInt63Field.nextPositiveRandom();
	}

	public ManyFieldBeanKey(Long id){
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