package com.hotpads.datarouter.serialize.fielder;

import java.util.ArrayList;
import java.util.List;

import com.hotpads.datarouter.storage.databean.VersionedDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class BaseVersionedDatabeanFielder<PK extends PrimaryKey<PK>,D extends VersionedDatabean<PK,D>>
extends BaseDatabeanFielder<PK,D>{

	public static class FieldKeys{
		public static final LongFieldKey version = new LongFieldKey("version");
	}

	public BaseVersionedDatabeanFielder(Class<? extends Fielder<PK>> primaryKeyFielderClass){
		super(primaryKeyFielderClass);
	}

	@Override
	public final List<Field<?>> getNonKeyFields(D databean){
		List<Field<?>> fields = new ArrayList<>();
		fields.addAll(getVersionedNonKeyFields(databean));
		fields.add(new LongField(FieldKeys.version, databean.getVersion()));
		return fields;
	}

	public abstract List<Field<?>> getVersionedNonKeyFields(D databean);

	@Override
	public boolean isVersioned(){
		return true;
	}

}
