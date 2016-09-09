package com.hotpads.datarouter.test.node.basic.map.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.key.entity.base.BaseEntityKey;
import com.hotpads.datarouter.storage.key.entity.base.BaseEntityPartitioner;
import com.hotpads.datarouter.util.core.DrHashMethods;

@SuppressWarnings("serial")
public class MapStorageBeanEntityKey extends BaseEntityKey<MapStorageBeanEntityKey>{

	private static int NUM_PARTITIONS = 4;

	/** fields ***************************************************************/

	private Long entityId;

	public static class FieldKeys{
		public static final LongFieldKey entityId = new LongFieldKey("entityId");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new LongField(FieldKeys.entityId, entityId));
	}

	/** partitioner **********************************************************/

	public static class MapStorageBeanEntityPartitioner extends BaseEntityPartitioner<MapStorageBeanEntityKey>{

		@Override
		public int getNumPartitions(){
			return NUM_PARTITIONS;
		}

		@Override
		public int getPartition(MapStorageBeanEntityKey entityKey){
			String hashInput = String.valueOf(entityKey.entityId);
			long hash = DrHashMethods.longDJBHash(hashInput) % getNumPartitions();
			return (int)(hash % getNumPartitions());
		}
	}

	/** Constructor **********************************************************/

	// for reflection
	@SuppressWarnings("unused")
	private MapStorageBeanEntityKey(){
	}

	public MapStorageBeanEntityKey(Long entityId){
		this.entityId = entityId;
	}

	/** get/set **************************************************************/

	public Long getEntityId(){
		return entityId;
	}
}