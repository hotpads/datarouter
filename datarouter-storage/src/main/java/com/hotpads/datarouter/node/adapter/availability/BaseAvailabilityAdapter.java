package com.hotpads.datarouter.node.adapter.availability;

import com.hotpads.datarouter.exception.UnavailableException;
import com.hotpads.datarouter.node.adapter.BaseAdapter;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class BaseAvailabilityAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalNode<PK,D>>
extends BaseAdapter<PK,D,N>{

	public BaseAvailabilityAdapter(N backingNode){
		super(backingNode);
	}

	public N getBackingNode(){
		return backingNode;
	}

	@Override
	protected String getToStringPrefix(){
		return "AvailabilityAdapter";
	}

	public UnavailableException makeUnavailableException(){
		return new UnavailableException("Client " + getBackingNode().getClient().getName() + " is not available.");
	}

}
