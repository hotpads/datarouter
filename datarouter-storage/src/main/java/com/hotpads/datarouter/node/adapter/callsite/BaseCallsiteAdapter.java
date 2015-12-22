package com.hotpads.datarouter.node.adapter.callsite;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.BaseAdapter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.callsite.CallsiteRecorder;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.profile.callsite.LineOfCode;
import com.hotpads.util.core.cache.Cached;

public abstract class BaseCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D>>
extends BaseAdapter<PK,D,N> implements CallsiteAdapter{

	private final Cached<Boolean> recordCallsites;

	public BaseCallsiteAdapter(NodeParams<PK,D,F> params, N backingNode){
		super(backingNode);
		this.recordCallsites = params.getRecordCallsites();
	}

	@Override
	public String getToStringPrefix(){
		return "CallsiteAdapter";
	}

	@Override
	public LineOfCode getCallsite(){
		LineOfCode callsite = new LineOfCode(2);//adjust for this method and adapter method
		return callsite;
	}

	@Override
	public void recordCollectionCallsite(Config config, long startTimeNs, Collection<?> items){
		recordCallsite(config, startTimeNs, DrCollectionTool.size(items));
	}

	@Override
	public void recordCallsite(Config config, long startNs, int numItems){
		if(recordCallsites == null || DrBooleanTool.isFalseOrNull(recordCallsites.get())){
			return;
		}
		LineOfCode datarouterMethod = new LineOfCode(2);
		long durationNs = System.nanoTime() - startNs;
		CallsiteRecorder.record(backingNode.getName(), datarouterMethod.getMethodName(), config.getCallsite(),
				numItems, durationNs);
	}

	public N getBackingNode(){
		return backingNode;
	}

}
