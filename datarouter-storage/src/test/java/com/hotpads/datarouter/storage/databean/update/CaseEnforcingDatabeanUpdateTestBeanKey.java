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

	private String aa;
	private String bb;
	private Integer cc;
	private String dd;

	CaseEnforcingDatabeanUpdateTestBeanKey(){
	}


	public CaseEnforcingDatabeanUpdateTestBeanKey(String aa, String bb, Integer cc, String dd){
		this.aa = aa;
		this.bb = bb;
		this.cc = cc;
		this.dd = dd;
	}

	public static final String
		COL_a = "aa",
		COL_b = "bb",
		COL_c = "cc",
		COL_d = "dd";


	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(COL_a, aa, DEFAULT_STRING_LENGTH),
				new StringField(COL_b, bb, DEFAULT_STRING_LENGTH),
				new UInt31Field(COL_c, cc),
				new StringField(COL_d, dd, DEFAULT_STRING_LENGTH));
	}



	/***************************** get/set *******************************/

	public String getA(){
		return aa;
	}

	public void setA(String aa){
		this.aa = aa;
	}

	public String getB(){
		return bb;
	}

	public void setB(String bb){
		this.bb = bb;
	}

	public Integer getC(){
		return cc;
	}

	public void setC(Integer cc){
		this.cc = cc;
	}

	public String getD(){
		return dd;
	}

	public void setD(String dd){
		this.dd = dd;
	}


}