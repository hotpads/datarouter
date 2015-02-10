package com.hotpads.datarouter.serialize.fielder;


import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract  class BaseLatin1Fielder <PK extends PrimaryKey<PK>,
D extends Databean<PK,D>>
extends BaseDatabeanFielder<PK,D>
{
	
	@Override
	public MySqlCollation getCollation(D databean){
		return MySqlCollation.latin1_swedish_ci;
	}
	
	@Override
	public MySqlCharacterSet getCharacterSet(D databean){
		return MySqlCharacterSet.latin1;
	}
	
}
