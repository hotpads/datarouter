package com.hotpads.datarouter.storage.field;

import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.FieldlessIndexEntry;
import com.hotpads.datarouter.storage.key.FieldlessIndexEntryPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class FieldlessIndexEntryFielder<
		IK extends FieldlessIndexEntryPrimaryKey<IK,PK,D>,
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseDatabeanFielder<IK,FieldlessIndexEntry<IK,PK,D>>{

	private MySqlCharacterSet characterSet = DEFAULT_CHARACTER_SET;
	private MySqlCollation collation = DEFAULT_COLLATION;

	public FieldlessIndexEntryFielder(Class<IK> keyClass){
		super(keyClass);
	}

	public FieldlessIndexEntryFielder(Class<IK> keyClass, MySqlCharacterSet characterSet, MySqlCollation collation){
		this(keyClass);
		if(characterSet != null){
			this.characterSet = characterSet;
		}
		if(collation != null){
			this.collation = collation;
		}
	}

	@Override
	public List<Field<?>> getNonKeyFields(FieldlessIndexEntry<IK,PK,D> databean){
		return Collections.emptyList();
	}

	@Override
	public MySqlCharacterSet getCharacterSet(){
		return characterSet;
	}

	@Override
	public MySqlCollation getCollation(){
		return collation;
	}
}
