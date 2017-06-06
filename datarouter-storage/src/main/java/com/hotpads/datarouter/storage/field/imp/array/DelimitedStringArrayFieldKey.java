package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.google.common.reflect.TypeToken;
import com.hotpads.datarouter.storage.field.ListFieldKey;

public class DelimitedStringArrayFieldKey extends ListFieldKey<String,List<String>>{

	public final String separator;

	public DelimitedStringArrayFieldKey(String name, String separator){
		super(name, new TypeToken<List<String>>(){});
		this.separator = separator;
	}

	public DelimitedStringArrayFieldKey(String name){
		this(name, ",");
	}

	@Override
	public DelimitedStringArrayField createValueField(final List<String> value){
		return new DelimitedStringArrayField(this, value);
	}

	//hmm, why doesn't this override isFixedLength => false
}
