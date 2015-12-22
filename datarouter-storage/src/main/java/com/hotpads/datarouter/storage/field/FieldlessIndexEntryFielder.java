package com.hotpads.datarouter.storage.field;

import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.FieldlessIndexEntry;
import com.hotpads.datarouter.storage.key.FieldlessIndexEntryPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class FieldlessIndexEntryFielder<
		IK extends FieldlessIndexEntryPrimaryKey<IK,PK,D>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseDatabeanFielder<IK,FieldlessIndexEntry<IK,PK,D>>{

	public FieldlessIndexEntryFielder(Class<IK> keyClass){
		super(keyClass);
	}

	@Override
	public List<Field<?>> getNonKeyFields(FieldlessIndexEntry<IK,PK,D> databean){
		return Collections.emptyList();
	}

}
