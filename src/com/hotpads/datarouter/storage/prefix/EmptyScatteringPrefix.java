package com.hotpads.datarouter.storage.prefix;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ListTool;

public class EmptyScatteringPrefix<PK extends PrimaryKey<PK>>
extends BaseScatteringPrefix<PK>{

	public static final EmptyScatteringPrefix<?> REUSABLE_PREFIX = new EmptyScatteringPrefix();
	public static final List<Field<?>> EMPTY_LIST = ListTool.createArrayList();
	public static final List<List<Field<?>>> EMPTY_LIST_LIST = ListTool.createArrayList();
	
	public EmptyScatteringPrefix(){
	}
	
	@Override
	public List<Field<?>> getScatteringPrefixFields(PK primaryKey){
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
