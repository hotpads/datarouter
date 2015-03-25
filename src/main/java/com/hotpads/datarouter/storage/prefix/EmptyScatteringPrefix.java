package com.hotpads.datarouter.storage.prefix;

import java.util.ArrayList;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.util.core.DrListTool;

public class EmptyScatteringPrefix
extends BaseScatteringPrefix{

	public static final EmptyScatteringPrefix REUSABLE_PREFIX = new EmptyScatteringPrefix();
	public static final List<Field<?>> EMPTY_LIST = new ArrayList<>();
	
	//this should have one empty FieldSet representing the normal, non-prefixed table
	public static final List<List<Field<?>>> EMPTY_LIST_LIST = DrListTool.wrap(EMPTY_LIST);
	
	public EmptyScatteringPrefix(){
	}
	
	@Override
	public List<Field<?>> getScatteringPrefixFields(FieldSet<?> primaryKey){
		return EMPTY_LIST;
	}
	
	@Override
	public List<List<Field<?>>> getAllPossibleScatteringPrefixes() {
		return EMPTY_LIST_LIST;
	}

	@Override
	public Integer getNumPrefixBytes() {
		return 0;
	}
	
}
