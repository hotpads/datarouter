package com.hotpads.datarouter.test.client.insert;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class PutOpTestBeanKey extends BasePrimaryKey<PutOpTestBeanKey>{

	private String a;
	private String b;
	
	PutOpTestBeanKey(){
	}
	
	public PutOpTestBeanKey(String a, String b){
		this.a = a;
		this.b = b;
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(new StringField("a", a, 100), new StringField("b", b, 100));
	}

	public String getA(){
		return a;
	}

	public void setA(String a){
		this.a = a;
	}

	public String getB(){
		return b;
	}

	public void setB(String b){
		this.b = b;
	}
}
