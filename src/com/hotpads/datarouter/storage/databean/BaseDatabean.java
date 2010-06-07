package com.hotpads.datarouter.storage.databean;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.storage.field.BaseFieldSet;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ClassTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;


@SuppressWarnings("serial")
public abstract class BaseDatabean<PK extends PrimaryKey<PK>>
extends BaseFieldSet
implements Databean<PK> {

	/********************** databean *********************************/
	
	@Override
	public String getDatabeanName() {
		return this.getClass().getSimpleName();
	}
	
	
	/*************************** fields ****************************/
	
	@Override
	public List<Field<?>> getKeyFields(){
		return this.getKey().getFields();
	}

	@Override
	public List<Field<?>> getNonKeyFields(){
		return new LinkedList<Field<?>>();
	}

	@Override
	public List<Field<?>> getFields(){
		List<Field<?>> allFields = ListTool.createLinkedList();
		allFields.addAll(getKeyFields());
		allFields.addAll(getNonKeyFields());
		return allFields;
	}

	@Override
	public List<String> getFieldNames(){
		return this.getKey().getFieldNames();
	}

	@Override
	public List<Comparable<?>> getFieldValues(){
		return this.getKey().getFieldValues();
	}

	@Override
	public Comparable<?> getFieldValue(String fieldName){
		return this.getKey().getFieldValue(fieldName);
	}

	@Override
	public String getPersistentString(){  //fuse multi-column field into one string, usually with "_" characters
		return this.getKey().getPersistentString();
	}
	
	@Override
	public String getTypedPersistentString(){  //usually getDatabeanName()+"."+getPersistentString()
		return this.getClass().getSimpleName()+"."+this.getPersistentString();
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> Map<PK,D> 
	getByKey(Iterable<D> databeans){
		Map<PK,D> map = MapTool.createHashMap();
		for(D databean : IterableTool.nullSafe(databeans)){
			map.put(databean.getKey(), databean);
		}
		return map;
	}
	
	/************************ standard java *************************/
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj){
		if(ClassTool.differentClass(this, obj)){ return false; }
		Databean<PK> that = (Databean<PK>)obj;
		return this.getKey().equals(that.getKey());
	}

//	@Override
//	public int compareTo(Databean<PK> obj) {
//		return this.getKey().compareTo(obj.getKey());
//	}
	
	
	@Override
	public int hashCode(){
		return this.getKey().hashCode();
	}

	@Override
	public String toString(){
		return this.getClass().getSimpleName()+"."+this.getKey().getPersistentString();
	}
}
