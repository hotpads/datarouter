package com.hotpads.datarouter.serialize;

import java.util.List;
import java.util.function.Supplier;

import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface StringDatabeanCodec{

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	String toString(D databean, F fielder);

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	D fromString(String string, F fielder, Supplier<D> databeanSupplier);

	public String getCollectionSeparator();
	public String getCollectionPrefix();
	public String getCollectionSuffix();

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	List<D> fromStringMulti(String string, F fielder, Supplier<D> databeanSupplier);
}
