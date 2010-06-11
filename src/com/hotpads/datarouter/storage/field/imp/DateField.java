package com.hotpads.datarouter.storage.field.imp;

import java.util.Date;

import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.bytes.LongByteTool;

public class DateField extends PrimitiveField<Date>{

	public DateField(String name, Date value){
		super(name, value);
	}

	public DateField(String prefix, String name, Date value){
		super(prefix, name, value);
	}

	@Override
	public Date parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Date)obj;
	}
	
	@Override
	public byte[] getBytes(){
		return LongByteTool.getUInt63Bytes(this.value.getTime());
	}
}
