package com.hotpads.datarouter.test.client.insert.generated.random;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.datarouter.test.client.insert.generated.PutOpGeneratedTestBeanKey;

@SuppressWarnings("serial")
public class PutOpIdGeneratedRandomTestBeanKey
extends BasePrimaryKey<PutOpIdGeneratedRandomTestBeanKey>
implements PutOpGeneratedTestBeanKey<PutOpIdGeneratedRandomTestBeanKey>{

	private Long id;
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(new UInt63Field(null, "id", false, FieldGeneratorType.RANDOM, id));
	}

	public Long getId(){
		return id;
	}
	
	public void setId(Long id){
		this.id = id;
	}

}
