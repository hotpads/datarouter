package com.hotpads.datarouter.node.op.index;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface MultiIndexReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>>{

	List<D> lookupMulti(IK indexKey, boolean wildcardLastField, Config config);
	List<D> lookupMultiMulti(Collection<IK> indexKeys, boolean wildcardLastField, Config config);

	//should probably take an IE as the start key, and maybe for the prefix too
//	List<D> getPrefixedRange(
//			final IK prefix, final boolean wildcardLastField, 
//			final IK start, final boolean startInclusive, 
//			final Config config);
	
}
