package com.hotpads.datarouter.storage.databean;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface Databean<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends FieldSet<D>
//,DatabeanFielder<PK,D>
{

	String getDatabeanName();
	
	Class<PK> getKeyClass();
	String getKeyFieldName();
	PK getKey();  
	
	boolean isFieldAware();
	
	List<Field<?>> getKeyFields();
	@Deprecated//always use a Fielder
	List<Field<?>> getNonKeyFields();
	MySqlCollation getCollation();
	MySqlCharacterSet getCharacterSet();
	
}
