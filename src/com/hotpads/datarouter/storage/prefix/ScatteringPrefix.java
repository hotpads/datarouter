package com.hotpads.datarouter.storage.prefix;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface ScatteringPrefix
//extends FieldSet<PK>
{

//	void setKey(PK pk);

	List<Field<?>> getScatteringPrefixFields(FieldSet<?> primaryKey);
	List<List<Field<?>>> getAllPossibleScatteringPrefixes();
	Integer getNumPrefixBytes();
	
}
