package com.hotpads.datarouter.test.client.insert.generated;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class PutOpIdGeneratedTestBeanKey extends BasePrimaryKey<PutOpIdGeneratedTestBeanKey>{

	private Long id;
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(new UInt63Field("id", id).setAutoGenerated(true));
	}

	public Long getId(){
		return id;
	}

}