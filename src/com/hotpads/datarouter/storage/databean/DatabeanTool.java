package com.hotpads.datarouter.storage.databean;

import java.lang.reflect.Constructor;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


public class DatabeanTool {	

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
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
	
}
