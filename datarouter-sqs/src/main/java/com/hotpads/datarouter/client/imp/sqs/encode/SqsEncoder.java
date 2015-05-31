package com.hotpads.datarouter.client.imp.sqs.encode;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface SqsEncoder{

	<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> String encode(D databean);
	
	<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> D decode(String string, Class<D> databeanClass);
	
}
