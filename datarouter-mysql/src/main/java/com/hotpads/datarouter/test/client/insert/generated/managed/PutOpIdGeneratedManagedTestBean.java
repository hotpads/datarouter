package com.hotpads.datarouter.test.client.insert.generated.managed;

import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.test.client.insert.generated.PutOpGeneratedTestBean;

public class PutOpIdGeneratedManagedTestBean
extends BaseDatabean<PutOpIdGeneratedManagedTestBeanKey, PutOpIdGeneratedManagedTestBean>
implements PutOpGeneratedTestBean<PutOpIdGeneratedManagedTestBeanKey, PutOpIdGeneratedManagedTestBean>{

	private PutOpIdGeneratedManagedTestBeanKey key;
	private String foo;

	public static class PutOpIdGeneratedManagedTestBeanFielder
	extends BaseDatabeanFielder<PutOpIdGeneratedManagedTestBeanKey,PutOpIdGeneratedManagedTestBean>{

		@Override
		public Class<? extends Fielder<PutOpIdGeneratedManagedTestBeanKey>> getKeyFielderClass(){
			return PutOpIdGeneratedManagedTestBeanKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(PutOpIdGeneratedManagedTestBean bean){
			return FieldTool.createList(new StringField("foo", bean.foo, 10));
		}

	}

	public PutOpIdGeneratedManagedTestBean(){
		this.key = new PutOpIdGeneratedManagedTestBeanKey();
	}

	public PutOpIdGeneratedManagedTestBean(String str){
		this.key = new PutOpIdGeneratedManagedTestBeanKey();
		this.foo = str;
	}

	@Override
	public Class<PutOpIdGeneratedManagedTestBeanKey> getKeyClass(){
		return PutOpIdGeneratedManagedTestBeanKey.class;
	}

	@Override
	public PutOpIdGeneratedManagedTestBeanKey getKey(){
		return key;
	}

}