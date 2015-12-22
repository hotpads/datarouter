package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.hotpads.datarouter.storage.field.ListFieldKey;

public class DelimitedStringArrayFieldKey extends ListFieldKey<String,List<String>>{

	public final String separator;

	public DelimitedStringArrayFieldKey(String name, String separator){
		super(name);
		this.separator = separator;
	}

}
