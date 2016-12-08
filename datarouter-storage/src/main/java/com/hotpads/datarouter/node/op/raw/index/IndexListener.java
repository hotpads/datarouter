package com.hotpads.datarouter.node.op.raw.index;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexListener<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>{

	void onPut(D databean, Config config);
	void onPutMulti(Collection<D> databeans, Config config);

	void onDelete(PK key, Config config);
	void onDeleteDatabean(D databean, Config config);
	void onDeleteMulti(Collection<PK> keys, Config config);
	void onDeleteMultiDatabeans(Collection<D> databeans, Config config);
	void onDeleteAll(Config config);

}
