package com.hotpads.datarouter.test.client.insert.generated;

import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;

public class PutOpIdGeneratedTestBean extends BaseDatabean<PutOpIdGeneratedTestBeanKey, PutOpIdGeneratedTestBean>{

	private PutOpIdGeneratedTestBeanKey key;
	private String a;
	
	public static class PutOpIdGeneratedTestBeanFielder extends BaseDatabeanFielder<PutOpIdGeneratedTestBeanKey, PutOpIdGeneratedTestBean>{

		@Override
		public Class<? extends Fielder<PutOpIdGeneratedTestBeanKey>> getKeyFielderClass(){
			return PutOpIdGeneratedTestBeanKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(PutOpIdGeneratedTestBean d){
			return FieldTool.createList(new StringField("a", d.a, 10));
		}
		
	}
	
	public PutOpIdGeneratedTestBean(){
		this.key = new PutOpIdGeneratedTestBeanKey();
	}
	
	public PutOpIdGeneratedTestBean(String a){
		this.key = new PutOpIdGeneratedTestBeanKey();
		this.a = a;
	}

	@Override
	public Class<PutOpIdGeneratedTestBeanKey> getKeyClass(){
		return PutOpIdGeneratedTestBeanKey.class;
	}

	@Override
	public PutOpIdGeneratedTestBeanKey getKey(){
		return key;
	}

}
