package com.hotpads.datarouter.storage.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.key.FieldlessIndexEntryPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.util.core.java.ReflectionTool;

public class FieldlessIndexEntry<
		IK extends FieldlessIndexEntryPrimaryKey<IK,PK,D>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseDatabean<IK,FieldlessIndexEntry<IK,PK,D>>
implements UniqueIndexEntry<IK,FieldlessIndexEntry<IK,PK,D>,PK,D>{

	private final Class<IK> keyClass;
	private final IK key;

	public FieldlessIndexEntry(Class<IK> keyClass){
		this(keyClass, ReflectionTool.create(keyClass));
	}

	public FieldlessIndexEntry(Class<IK> keyClass, IK key){
		this.keyClass = keyClass;
		this.key = key;
	}

	@Override
	public Class<IK> getKeyClass(){
		return keyClass;
	}

	@Override
	public IK getKey(){
		return key;
	}

	@Override
	public PK getTargetKey(){
		return key.getTargetKey();
	}

	@Override
	public List<FieldlessIndexEntry<IK,PK,D>> createFromDatabean(D target){
		return Arrays.asList(key.createFromDatabean(target));
	}

}
