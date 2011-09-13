package com.hotpads.datarouter.storage.prefix;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface ScatteringPrefix<PK extends PrimaryKey<PK>>
//extends FieldSet<PK>
{

	void setKey(PK pk);

	List<Field<?>> getScatteringPrefixFields(PK primaryKey);
	List<List<Field<?>>> getAllPossibleScatteringPrefixes();
	Integer getNumPrefixBytes();
	
}
