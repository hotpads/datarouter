package com.hotpads.datarouter.test.client.insert;

import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;

public class PutOpTestBean extends BaseDatabean<PutOpTestBeanKey, PutOpTestBean>{

	private PutOpTestBeanKey key;
	private String strC;

	PutOpTestBean(){
		key = new PutOpTestBeanKey();
	}

	public PutOpTestBean(String strA, String strB, String strC){
		key = new PutOpTestBeanKey(strA, strB);
		this.strC = strC;
	}

	public static class PutOpTestBeanFielder extends BaseDatabeanFielder<PutOpTestBeanKey,PutOpTestBean>{

		@Override
		public Class<? extends Fielder<PutOpTestBeanKey>> getKeyFielderClass(){
			return PutOpTestBeanKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(PutOpTestBean databean){
			return FieldTool.createList(new StringField("strC", databean.strC, 100));
		}

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
		return strC;
	}

	public void setC(String strC){
		this.strC = strC;
	}
}
