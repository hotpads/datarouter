package com.hotpads.datarouter.serialize.fielder;

import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.prefix.EmptyScatteringPrefix;
import com.hotpads.datarouter.storage.prefix.ScatteringPrefix;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseDatabeanFielder<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
//extends BaseFielder<D>
implements DatabeanFielder<PK,D>{
	
	protected BaseDatabeanFielder(){
		this.scatteringPrefix = ReflectionTool.create(getScatteringPrefixClass());
		this.primaryKeyFielder = ReflectionTool.create(getKeyFielderClass());
	}

//	protected PKF primaryKeyFielder;
	protected ScatteringPrefix scatteringPrefix;
	protected Fielder<PK> primaryKeyFielder;
	
	@Override
	public Class<? extends ScatteringPrefix> getScatteringPrefixClass() {
		return EmptyScatteringPrefix.class;
	}
	
	@Override
	public Fielder<PK> getKeyFielder(){
		return primaryKeyFielder;
	}
	
	@Override
	public List<Field<?>> getKeyFields(D databean){
		return FieldTool.prependPrefixes(databean.getKeyFieldName(), 
				primaryKeyFielder.getFields(databean.getKey()));
	}
	
	@Override
	public List<Field<?>> getFields(D databean){
		List<Field<?>> allFields = getKeyFields(databean); //getKeyFields already prepends prefixes
		ListTool.nullSafeArrayAddAll(allFields, getNonKeyFields(databean));
		return allFields;
	}
	
	@Override
	public Map<String,List<Field<?>>> getIndexes(D databean){
		return MapTool.createTreeMap();
	}
	
	@Override
	public MySqlCollation getCollation(D databean){
		return MySqlCollation.utf8_bin;
	}
	
	@Override
	public MySqlCharacterSet getCharacterSet(D databean){
		return MySqlCharacterSet.utf8;
	}
	
}
