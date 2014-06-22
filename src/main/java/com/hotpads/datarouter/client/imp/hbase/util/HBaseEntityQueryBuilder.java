package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.Collection;
import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.ColumnRangeFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.Range;

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
	
	
	/******************* keys ****************************/
	
	public Range<EK> getEkRange(Range<PK> pkRange){
		EK start = pkRange.hasStart() ? pkRange.getStart().getEntityKey() : null;
		EK end = pkRange.hasEnd() ? pkRange.getEnd().getEntityKey() : null;
		return Range.create(start, pkRange.getStartInclusive(), end, pkRange.getEndInclusive());
	}
	
	public byte[] getRowBytes(EK entityKey){
		if(entityKey==null){ return new byte[]{}; }
		return FieldTool.getConcatenatedValueBytes(entityKey.getFields(), true, false);
	}
	
	public byte[] getQualifier(PK primaryKey, String fieldName){
		return ByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(), getQualifierPkBytes(primaryKey),
				StringByteTool.getUtf8Bytes(fieldName));
	}
	
	public byte[] getQualifierPrefix(PK primaryKey){
		return ByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(), getQualifierPkBytes(primaryKey));
	}
	
	public byte[] getQualifierPkBytes(PK primaryKey){
		if(primaryKey==null){ return new byte[]{}; }
		return FieldTool.getConcatenatedValueBytes(primaryKey.getPostEntityKeyFields(), true, true);
	}
	
	public Range<ByteRange> getRowRange(Range<PK> pkRange){
		ByteRange startBytes = null;
		if(pkRange.hasStart()){
			startBytes = new ByteRange(getRowBytes(pkRange.getStart().getEntityKey()));
		}
		ByteRange endBytes = null;
		if(pkRange.hasEnd()){
			endBytes = new ByteRange(getRowBytes(pkRange.getEnd().getEntityKey()));
		}
		return Range.create(startBytes, pkRange.getStartInclusive(), endBytes, pkRange.getEndInclusive());
	}
	
	public ColumnRangeFilter getColumnRangeFilter(Range<PK> pkRange){
		byte[] start = getQualifierPrefix(pkRange.getStart());
		byte[] end = getQualifierPrefix(pkRange.getEnd());
		return new ColumnRangeFilter(start, pkRange.getStartInclusive(), end, pkRange.getEndInclusive());
	}
	
	
	/*********************** Get ****************************/
	
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
	
	public Get getSingleRowRange(EK ek, Range<PK> pkRange, boolean keysOnly){
		Get get = new Get(getRowBytes(ek));
		ColumnRangeFilter columnRangeFilter = getColumnRangeFilter(pkRange);
		if(keysOnly){
			FilterList filterList = new FilterList();
			filterList.addFilter(new KeyOnlyFilter());
			filterList.addFilter(columnRangeFilter);
			get.setFilter(filterList);
		}else{
			get.setFilter(columnRangeFilter);
		}
		return get;
	}
	
	
	/***************** Scan *******************************/
}
