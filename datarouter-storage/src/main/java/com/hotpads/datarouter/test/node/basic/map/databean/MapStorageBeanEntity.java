package com.hotpads.datarouter.test.node.basic.map.databean;

import java.util.Collection;

import com.hotpads.datarouter.storage.entity.BaseEntity;

public class MapStorageBeanEntity extends BaseEntity<MapStorageBeanEntityKey>{

	public static final String QUALIFIER_PREFIX_MapStorageBean = "MSB";

	/** constructor **********************************************************/

	private MapStorageBeanEntity(){
		super(null);
	}

	public MapStorageBeanEntity(MapStorageBeanEntityKey key){
		super(key);
	}
}
