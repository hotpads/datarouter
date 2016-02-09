package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.positive.UInt31Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt31FieldKey;
import com.hotpads.datarouter.storage.key.primary.base.BaseEntityPrimaryKey;

/********************************* indexes ***********************************/

@SuppressWarnings("serial")
@Embeddable
public class SortedBeanKey extends BaseEntityPrimaryKey<SortedBeanEntityKey,SortedBeanKey>{

	private String a;
	private String b;
	private Integer c;
	private String d;

	public static class FieldKeys{
		public static final UInt31FieldKey c = new UInt31FieldKey("c");
		public static final StringFieldKey d = new StringFieldKey("d");
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return FieldTool.createList(
				new UInt31Field(FieldKeys.c, c),
				new StringField(FieldKeys.d, d));
	}

	@Override
	public SortedBeanEntityKey getEntityKey(){
		return new SortedBeanEntityKey(a, b);
	}

	@Override
	public String getEntityKeyName() {
		return null;
	}

	@Override
	public SortedBeanKey prefixFromEntityKey(SortedBeanEntityKey ek){
		return new SortedBeanKey(ek.getA(), ek.getB(), null, null);
	}


	/************************ construct ********************************/

	SortedBeanKey(){
	}

	public SortedBeanKey(String a, String b, Integer c, String d){
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}


	/***************************** get/set *******************************/

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

	public Integer getC(){
		return c;
	}

	public void setC(Integer c){
		this.c = c;
	}

	public String getD(){
		return d;
	}

	public void setD(String d){
		this.d = d;
	}


}