package com.hotpads.datarouter.storage.field.enums;

import java.util.Date;

import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;

public class ColumnEnumTool {
	public static String getFieldName(ColumnEnum c){
		if(c.getPrefix()==null) return c.getColumnName();
		return c.getPrefix()+"."+c.getColumnName();
	}
	public static StringField field(ColumnEnum c, String value){
		return new StringField(c.getPrefix(),c.getColumnName(),value,255);			
	}
	public static IntegerField field(ColumnEnum c, Integer value){
		return new IntegerField(c.getPrefix(),c.getColumnName(),value);			
	}
	public static DateField field(ColumnEnum c, Date value){
		return new DateField(c.getPrefix(),c.getColumnName(),value);			
	}
	public static LongField field(ColumnEnum c, Long value){
		return new LongField(c.getPrefix(),c.getColumnName(),value);			
	}
	//add more field types here if/as needed
}
