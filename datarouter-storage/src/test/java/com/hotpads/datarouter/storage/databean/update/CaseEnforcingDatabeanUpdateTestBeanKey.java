package com.hotpads.datarouter.storage.databean.update;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt31Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

/********************************* indexes ***********************************/

@SuppressWarnings("serial")
public class CaseEnforcingDatabeanUpdateTestBeanKey extends BasePrimaryKey<CaseEnforcingDatabeanUpdateTestBeanKey>{

	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;

	protected String a;
	protected String b;
	protected Integer c;
	protected String d;

	CaseEnforcingDatabeanUpdateTestBeanKey(){
	}


	public CaseEnforcingDatabeanUpdateTestBeanKey(String a, String b, Integer c, String d){
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	public static final String
		COL_a = "a",
		COL_b = "b",
		COL_c = "c",
		COL_d = "d";


	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(COL_a, a, DEFAULT_STRING_LENGTH),
				new StringField(COL_b, b, DEFAULT_STRING_LENGTH),
				new UInt31Field(COL_c, c),
				new StringField(COL_d, d, DEFAULT_STRING_LENGTH));
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