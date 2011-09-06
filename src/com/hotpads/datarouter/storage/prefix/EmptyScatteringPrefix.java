package com.hotpads.datarouter.storage.prefix;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ListTool;

public class EmptyScatteringPrefix<PK extends PrimaryKey<PK>>
extends BaseScatteringPrefix<PK>{

	@Override
	public List<Field<?>> getScatteringPrefixFields(PK primaryKey){
		return ListTool.create();
	}
	
	@Override
	public List<List<Field<?>>> getAllPossibleScatteringPrefixes() {
		return ListTool.createLinkedList();
	}

	@Override
	public Integer getNumPrefixBytes() {
		return 0;
	}
	
}
