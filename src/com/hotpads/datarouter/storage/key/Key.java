package com.hotpads.datarouter.storage.key;

import java.io.Serializable;

import com.hotpads.datarouter.serialize.Jsonable;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanAware;
import com.hotpads.datarouter.storage.field.FieldSet;


public interface Key<D extends Databean>
extends FieldSet, DatabeanAware<D>, Comparable<Key<D>>, Serializable, Jsonable{
	
	
	String getPersistentString();  //fuse multi-column field into one string, usually with "_" characters
	String getTypedPersistentString();  //usually getDatabeanName()+"."+getPersistentString()
	
	
}
