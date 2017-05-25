package com.hotpads.datarouter.test.client.insert;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class PutOpTestBeanKey extends BasePrimaryKey<PutOpTestBeanKey>{

	private String first;
	private String second;

	PutOpTestBeanKey(){
	}

	public PutOpTestBeanKey(String first, String second){
		this.first = first;
		this.second = second;
	}

	public static class FieldKeys{
		public static final StringFieldKey first = new StringFieldKey("first");
		public static final StringFieldKey second = new StringFieldKey("second");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(FieldKeys.first, first), new StringField(FieldKeys.second, second));
	}

	public String getA(){
		return first;
	}

	public void setA(String first){
		this.first = first;
	}

	public String getB(){
		return second;
	}

	public void setB(String second){
		this.second = second;
	}
}
