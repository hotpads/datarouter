package com.hotpads.datarouter.storage.key;

import java.io.Serializable;
import java.util.List;

import com.hotpads.datarouter.serialize.Jsonable;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanAware;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.Fields;


public interface Key<D extends Databean>
extends Fields, DatabeanAware<D>, Comparable<Key<D>>, Serializable, Jsonable{
	
	
	String getPersistentString();  //fuse multi-column field into one string, usually with "_" characters
	String getTypedPersistentString();  //usually getDatabeanName()+"."+getPersistentString()
	
	
}
