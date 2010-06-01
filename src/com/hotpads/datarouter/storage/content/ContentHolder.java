package com.hotpads.datarouter.storage.content;

import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface ContentHolder<D extends Databean<PK>,PK extends PrimaryKey<PK>> {

	List<Field<?>> getKeyFields();
//	List<Field<?>> getDataFields();
	List<Field<?>> getMetaFields();
	List<Field<?>> getContentFields();
	
	boolean equalsContent(ContentHolder<D,PK> other);
	
	PK getKey();
	
//	void copyContent(ContentHolder<D> other);
}
