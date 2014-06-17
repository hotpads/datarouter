package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.Collection;
import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.Filter;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.ListTool;

public class HBaseEntityQueryBuilder<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
{
	
	private DatabeanFieldInfo<PK,D,F> fieldInfo;
	
	public HBaseEntityQueryBuilder(DatabeanFieldInfo<PK,D,F> fieldInfo){
		this.fieldInfo = fieldInfo;
	}
	
	public List<Get> getPrefixQueries(Collection<PK> prefixes, Config config){
		List<Get> gets = ListTool.createArrayList();
		for(PK prefix : prefixes){
			gets.add(getPrefixQuery(prefix, config));
		}
		return gets;
	}
	
	public Get getPrefixQuery(PK pkPrefix, Config config){
		byte[] rowBytes = FieldTool.getConcatenatedValueBytes(pkPrefix.getEntityKeyFields(), false, false);//don't allow nulls in EK
		byte[] pkQualifierBytes = FieldTool.getConcatenatedValueBytes(pkPrefix.getPostEntityKeyFields(), true, false);
		byte[] qualifierPrefix = ByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(), pkQualifierBytes);
		Get get = new Get(rowBytes);
		get.setFilter(new ColumnPrefixFilter(qualifierPrefix));
		//TODO obey config.getLimit()
		return get;
	}
	
	
	
}
