package com.hotpads.datarouter.storage.databean;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.SortedMap;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


public class DatabeanTool {

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	SortedMap<PK,D> getByKeySorted(Collection<D> databeans){
		return KeyTool.getByKeySorted(databeans);
	}
	

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	Class<PK> getPrimaryKeyClass(Class<D> databeanClass){
		try{
			//use getDeclaredConstructor to access non-public constructors
			Constructor<D> constructor = databeanClass.getDeclaredConstructor();
			constructor.setAccessible(true);
			D databeanInstance = constructor.newInstance();
			return databeanInstance.getKeyClass();
		}catch(Exception e){
			throw new DataAccessException(e.getClass().getSimpleName()+" on "+databeanClass.getSimpleName()
					+".  Is there a no-arg constructor?");
		}
	}
}
