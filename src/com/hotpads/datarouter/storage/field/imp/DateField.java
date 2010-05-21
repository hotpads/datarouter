package com.hotpads.datarouter.storage.field.imp;

import java.util.Date;

import com.hotpads.datarouter.storage.field.PrimitiveField;

public class DateField extends PrimitiveField<Date>{

	public DateField(String name, Date value){
		super(name, value);
	}

	public DateField(String prefix, String name, Date value){
		super(prefix, name, value);
	}

}
