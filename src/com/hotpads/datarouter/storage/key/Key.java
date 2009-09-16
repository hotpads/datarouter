package com.hotpads.datarouter.storage.key;

import java.io.Serializable;
import java.util.List;

import com.hotpads.datarouter.serialize.Jsonable;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanAware;
import com.hotpads.datarouter.storage.field.Field;


public interface Key<D extends Databean>
extends DatabeanAware<D>, Comparable<Key<D>>, Serializable, Jsonable{
	
	List<Field> getFields();
	List<String> getFieldNames();
	List<Comparable<?>> getFieldValues();

	Comparable<?> getFieldValue(String fieldName);
	
	String getPersistentString();  //fuse multi-column field into one string, usually with "_" characters
	String getTypedPersistentString();  //usually getDatabeanName()+"."+getPersistentString()
	
	List<String> getSqlValuesEscaped();
	List<String> getSqlNameValuePairsEscaped();
	String getSqlNameValuePairsEscapedConjunction();
	
	
}
