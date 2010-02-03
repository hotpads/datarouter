package com.hotpads.datarouter.storage.databean;

public interface DatabeanAware<D extends Databean> {

	String getDatabeanName();
	Class<D> getDatabeanClass();
	String getPhysicalNodeName();
}
