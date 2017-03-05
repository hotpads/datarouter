package com.hotpads.datarouter.serialize.codec;

import java.util.function.Supplier;

import com.hotpads.datarouter.serialize.JsonDatabeanTool;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class FlatKeyJsonDatabeanCodec extends JsonDatabeanCodec{

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	String toString(D databean, F fielder){
		return JsonDatabeanTool.databeanToJsonString(databean, fielder, true);
	}

	@Override
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> D fromString(
			String string, F fielder, Supplier<D> databeanSupplier){
		return JsonDatabeanTool.databeanFromJson(databeanSupplier, fielder, string, true);
	}

}
