package com.hotpads.datarouter.storage.databean;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.ConverterIterator;
import com.hotpads.util.core.stream.StreamTool;


public class DatabeanTool {

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	D create(Class<D> databeanClass){
		try{
			//use getDeclaredConstructor to access non-public constructors
			Constructor<D> constructor = databeanClass.getDeclaredConstructor();
			constructor.setAccessible(true);
			D databeanInstance = constructor.newInstance();
			return databeanInstance;
		}catch(Exception e){
			throw new DataAccessException(e.getClass().getSimpleName()+" on "+databeanClass.getSimpleName()
					+".  Is there a no-arg constructor?");
		}
	}

//	@Deprecated//should specify fielder using below method
//	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> byte[] getBytes(D databean){
//		//always include zero-length fields in key bytes
//		byte[] keyBytes = FieldSetTool.getSerializedKeyValues(databean.getKeyFields(), true, false);
//		byte[] nonKeyBytes = FieldSetTool.getSerializedKeyValues(databean.getNonKeyFields(), true, true);
//		byte[] allBytes = ArrayTool.concatenate(keyBytes, nonKeyBytes);
//		return allBytes;
//	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> byte[] getBytes(D databean,
			DatabeanFielder<PK,D> fielder){
		return getBytes(fielder.getKeyFields(databean), fielder.getNonKeyFields(databean));
	}

	protected static byte[] getBytes(List<Field<?>> keyFields, List<Field<?>> nonKeyFields){
		//always include zero-length fields in key bytes
		byte[] keyBytes = FieldSetTool.getSerializedKeyValues(keyFields, true, false);

		//skip zero-length fields in non-key bytes
		//TODO should this distinguish between null and empty Strings?
		byte[] nonKeyBytes = FieldSetTool.getSerializedKeyValues(nonKeyFields, true, true);
		byte[] allBytes = DrArrayTool.concatenate(keyBytes, nonKeyBytes);
		return allBytes;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> String getCsvColumnNames(D databean,
			DatabeanFielder<PK,D> fielder){
		return FieldTool.getCsvColumnNames(fielder.getFields(databean));
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> String getCsvValues(D databean,
			DatabeanFielder<PK,D> fielder){
		return FieldTool.getCsvValues(fielder.getFields(databean));
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	List<PK> getKeys(Iterable<D> databeans){
		List<PK> keys = new LinkedList<>();
		for(D databean : DrIterableTool.nullSafe(databeans)){
			keys.add(databean.getKey());
		}
		return keys;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> Iterator<PK> getKeys(Iterator<D> databeans){
		return new ConverterIterator<>(databeans, (databean) -> databean.getKey());
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	Map<PK,D> getByKey(Iterable<D> databeans){
		Map<PK,D> map = new HashMap<>();
		for(D databean : DrIterableTool.nullSafe(databeans)){
			map.put(databean.getKey(), databean);
		}
		return map;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	SortedMap<PK,D> getByKeySorted(Iterable<D> databeans){
		SortedMap<PK,D> map = new TreeMap<>();
		for(D databean : DrIterableTool.nullSafe(databeans)){
			map.put(databean.getKey(), databean);
		}
		return map;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	Range<PK> getKeyRange(Range<D> databeanRange){
		PK startKey = databeanRange.hasStart() ? databeanRange.getStart().getKey() : null;
		PK endKey = databeanRange.hasEnd() ? databeanRange.getEnd().getKey() : null;
		return new Range<>(startKey, databeanRange.getStartInclusive(), endKey, databeanRange.getEndInclusive());
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> Set<PK> getKeySet(Collection<D> databeans){
		return StreamTool.nullItemSafeStream(databeans).map(Databean::getKey).collect(Collectors.toSet());
	}

}
