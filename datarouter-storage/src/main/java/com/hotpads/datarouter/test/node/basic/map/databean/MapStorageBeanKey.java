package com.hotpads.datarouter.test.node.basic.map.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.base.BaseEntityPrimaryKey;

public class MapStorageBeanKey extends BaseEntityPrimaryKey<MapStorageBeanEntityKey,MapStorageBeanKey>{

	private final MapStorageBeanEntityKey entityKey;
	private final Long id;

	/** fields ***************************************************************/

	public static class FieldKeys{
		public static final LongFieldKey id = new LongFieldKey("id");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new LongField(FieldKeys.id, id));
	}

	/** constructor **********************************************************/

	public MapStorageBeanKey(){
		this(new MapStorageBeanEntityKey(UInt63Field.nextPositiveRandom()), UInt63Field.nextPositiveRandom());
	}

	public MapStorageBeanKey(MapStorageBeanEntityKey entityKey, Long id){
		this.entityKey = entityKey;
		this.id = id;
	}

	/** entity ***************************************************************/

	@Override
	public MapStorageBeanKey prefixFromEntityKey(MapStorageBeanEntityKey entityKey){
		return new MapStorageBeanKey(entityKey, null);
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return Arrays.asList(new LongField(FieldKeys.id, id));
	}

	/** get ******************************************************************/

	@Override
	public MapStorageBeanEntityKey getEntityKey(){
		return entityKey;
	}

	public Long getId(){
		return id;
	}
}