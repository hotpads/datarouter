package com.hotpads.datarouter.test.client.insert.generated.random;

import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.test.client.insert.generated.PutOpGeneratedTestBean;

public class PutOpIdGeneratedRandomTestBean
extends BaseDatabean<PutOpIdGeneratedRandomTestBeanKey, PutOpIdGeneratedRandomTestBean>
implements PutOpGeneratedTestBean<PutOpIdGeneratedRandomTestBeanKey, PutOpIdGeneratedRandomTestBean>{

	private PutOpIdGeneratedRandomTestBeanKey key;
	private String stringA;

	public static class PutOpIdGeneratedRandomTestBeanFielder
	extends BaseDatabeanFielder<PutOpIdGeneratedRandomTestBeanKey, PutOpIdGeneratedRandomTestBean>{

		@Override
		public Class<? extends Fielder<PutOpIdGeneratedRandomTestBeanKey>> getKeyFielderClass(){
			return PutOpIdGeneratedRandomTestBeanKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(PutOpIdGeneratedRandomTestBean val){
			return FieldTool.createList(new StringField("a", val.stringA, 10));
		}

	}

	public PutOpIdGeneratedRandomTestBean(){
		this.key = new PutOpIdGeneratedRandomTestBeanKey();
	}

	public PutOpIdGeneratedRandomTestBean(String val){
		this.key = new PutOpIdGeneratedRandomTestBeanKey();
		this.stringA = val;
	}

	@Override
	public Class<PutOpIdGeneratedRandomTestBeanKey> getKeyClass(){
		return PutOpIdGeneratedRandomTestBeanKey.class;
	}

	@Override
	public PutOpIdGeneratedRandomTestBeanKey getKey(){
		return key;
	}

}
