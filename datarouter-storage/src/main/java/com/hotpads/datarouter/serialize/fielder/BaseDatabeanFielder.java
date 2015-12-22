package com.hotpads.datarouter.serialize.fielder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.serialize.StringDatabeanCodec;
import com.hotpads.datarouter.serialize.codec.JsonDatabeanCodec;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.prefix.EmptyScatteringPrefix;
import com.hotpads.datarouter.storage.prefix.ScatteringPrefix;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseDatabeanFielder<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
implements DatabeanFielder<PK,D>{

	private final Class<? extends Fielder<PK>> primaryKeyFielderClass;
	private final Fielder<PK> primaryKeyFielder;
	private final StringDatabeanCodec stringDatabeanCodec;

	protected BaseDatabeanFielder(Class<? extends Fielder<PK>> primaryKeyFielderClass){
		this.primaryKeyFielder = ReflectionTool.create(primaryKeyFielderClass);
		this.primaryKeyFielderClass = primaryKeyFielderClass;
		this.stringDatabeanCodec = ReflectionTool.create(getStringDatabeanCodecClass());
	}

	/**
	 * @deprecated use {@link #BaseDatabeanFielder(Class)}, you do not need to implement {@link #getKeyFielderClass()}
	 */
	@Deprecated
	protected BaseDatabeanFielder(){
		this.primaryKeyFielder = ReflectionTool.create(getKeyFielderClass());
		this.primaryKeyFielderClass = getKeyFielderClass();
		this.stringDatabeanCodec = ReflectionTool.create(getStringDatabeanCodecClass());
	}

	@Override
	public Class<? extends Fielder<PK>> getKeyFielderClass(){
		return primaryKeyFielderClass;
	}

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
		return FieldTool.prependPrefixes(databean.getKeyFieldName(), primaryKeyFielder.getFields(databean.getKey()));
	}

	@Override
	public List<Field<?>> getFields(D databean){
		List<Field<?>> allFields = new ArrayList<>();
		allFields.addAll(getKeyFields(databean)); //getKeyFields already prepends prefixes
		allFields.addAll(getNonKeyFields(databean));
		return allFields;
	}

	@Override
	public Map<String,List<Field<?>>> getIndexes(D databean){
		return new TreeMap<>();
	}

	@Override
	public MySqlCollation getCollation(){
		return MySqlCollation.utf8_bin;
	}

	@Override
	public MySqlCharacterSet getCharacterSet(){
		return MySqlCharacterSet.utf8;
	}

	@Override
	public Class<? extends StringDatabeanCodec> getStringDatabeanCodecClass(){
		return JsonDatabeanCodec.class;
	}

	@Override
	public final StringDatabeanCodec getStringDatabeanCodec(){
		return stringDatabeanCodec;
	}

}
