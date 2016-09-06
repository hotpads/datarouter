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

	/** fields ***************************************************************/

	private Long id;

	public static class FieldKeys{
		public static final LongFieldKey id = new LongFieldKey("id");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new LongField(FieldKeys.id, id));
	}

	/** constructor **********************************************************/

	public MapStorageBeanKey(){
		this.id = UInt63Field.nextPositiveRandom();
	}

	public Long getId(){
		return id;
	}

	/** entity ***************************************************************/

	@Override
	public MapStorageBeanEntityKey getEntityKey(){
		return new MapStorageBeanEntityKey(UInt63Field.nextPositiveRandom());
	}

	@Override
	public MapStorageBeanKey prefixFromEntityKey(MapStorageBeanEntityKey entityKey){
		return new MapStorageBeanKey();
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return Arrays.asList(new LongField(FieldKeys.id, id));
	}
}