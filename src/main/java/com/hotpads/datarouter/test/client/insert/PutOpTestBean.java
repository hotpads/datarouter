package com.hotpads.datarouter.test.client.insert;

import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;

@SuppressWarnings("serial")
public class PutOpTestBean extends BaseDatabean<PutOpTestBeanKey, PutOpTestBean>{

	private PutOpTestBeanKey key;
	private String c;
	
	PutOpTestBean(){
		key = new PutOpTestBeanKey();
	}
	
	public PutOpTestBean(String a, String b, String c){
		key = new PutOpTestBeanKey(a, b);
		this.c = c;
	}
	
	public static class PutOpTestBeanFielder extends BaseDatabeanFielder<PutOpTestBeanKey, PutOpTestBean> {

		@Override
		public Class<? extends Fielder<PutOpTestBeanKey>> getKeyFielderClass(){
			return PutOpTestBeanKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(PutOpTestBean databean){
			return FieldTool.createList(new StringField("c", databean.c, 100));
		}
		
	}
	
	@Override
	public List<Field<?>> getNonKeyFields(){
		return new PutOpTestBeanFielder().getNonKeyFields(this);
	}

	@Override
	public Class<PutOpTestBeanKey> getKeyClass(){
		return PutOpTestBeanKey.class;
	}

	@Override
	public PutOpTestBeanKey getKey(){
		return key;
	}
	
	public void setKey(PutOpTestBeanKey key){
		this.key = key;
	}

	public String getC(){
		return c;
	}

	public void setC(String c){
		this.c = c;
	}
}
