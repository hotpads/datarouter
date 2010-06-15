package com.hotpads.datarouter.storage.content;

import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface ContentHolder<PK extends PrimaryKey<PK>,D extends Databean<PK>> {

	List<BaseField<?>> getKeyFields();
//	List<Field<?>> getDataFields();
	List<BaseField<?>> getMetaFields();
	List<BaseField<?>> getContentFields();
	
	boolean equalsContent(ContentHolder<?,?> other);
	
	PK getKey();
	
//	void copyContent(ContentHolder<D> other);
}
