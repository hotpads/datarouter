package com.hotpads.datarouter.storage.prefix;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ListTool;

public class EmptyScatteringPrefix<PK extends PrimaryKey<PK>>
extends BaseScatteringPrefix<PK>{

<<<<<<< HEAD
	public static final EmptyScatteringPrefix<?> REUSABLE_PREFIX = new EmptyScatteringPrefix();
	public static final List<Field<?>> EMPTY_LIST = ListTool.createArrayList();
	public static final List<List<Field<?>>> EMPTY_LIST_LIST = ListTool.createArrayList();
	
	public EmptyScatteringPrefix(){
	}
	
	@Override
	public List<Field<?>> getScatteringPrefixFields(PK primaryKey){
		return EMPTY_LIST;
=======
	@Override
	public List<Field<?>> getScatteringPrefixFields(PK primaryKey){
		return ListTool.create();
>>>>>>> fc2b9c3030ac2efe79047bfab0efa753cb2fbd3d
	}
	
	@Override
	public List<List<Field<?>>> getAllPossibleScatteringPrefixes() {
<<<<<<< HEAD
		return EMPTY_LIST_LIST;
=======
		return ListTool.createLinkedList();
>>>>>>> fc2b9c3030ac2efe79047bfab0efa753cb2fbd3d
	}

	@Override
	public Integer getNumPrefixBytes() {
		return 0;
	}
	
}
