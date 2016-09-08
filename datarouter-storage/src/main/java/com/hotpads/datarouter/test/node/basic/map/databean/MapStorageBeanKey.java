package com.hotpads.datarouter.test.node.basic.map.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.base.BaseEntityPrimaryKey;

@SuppressWarnings("serial")
public class MapStorageBeanKey extends BaseEntityPrimaryKey<MapStorageBeanEntityKey,MapStorageBeanKey>{

	private MapStorageBeanEntityKey entityKey;
	private Long id;

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
		this(UInt63Field.nextPositiveRandom());
	}

	public MapStorageBeanKey(Long id){
		this.id = id;
		entityKey = new MapStorageBeanEntityKey(id);
	}

	/** get ******************************************************************/

	public Long getId(){
		return id;
	}

	/** entity ***************************************************************/

	@Override
	public MapStorageBeanEntityKey getEntityKey(){
		return entityKey;
	}

	@Override
	public MapStorageBeanKey prefixFromEntityKey(MapStorageBeanEntityKey entityKey){
		return new MapStorageBeanKey(entityKey.getEntityId());
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return Arrays.asList(new LongField(FieldKeys.id, id));
	}
}