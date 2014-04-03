package com.hotpads.datarouter.storage.databean;

import java.lang.reflect.Constructor;
import java.util.List;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ArrayTool;


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
		byte[] allBytes = ArrayTool.concatenate(keyBytes, nonKeyBytes);
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
	
}
