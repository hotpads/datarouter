package com.hotpads.datarouter.node.type.index;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;

public interface ManagedNode{

	String getName();
	List<Field<?>> getFields();

}
