package com.hotpads.datarouter.storage.databean;

import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface VersionedDatabeanFielder<PK extends PrimaryKey<PK>,D extends VersionedDatabean<PK,D>>
extends DatabeanFielder<PK,D>{

	LongField getPreviousVersionField(VersionedDatabean<?,?> databean);

}
