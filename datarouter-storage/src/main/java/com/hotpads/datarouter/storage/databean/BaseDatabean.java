package com.hotpads.datarouter.storage.databean;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.PrimaryKeyPercentCodec;
import com.hotpads.util.core.lang.ClassTool;

public abstract class BaseDatabean<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
implements Databean<PK,D>{

	public static final String DEFAULT_KEY_FIELD_NAME = "key";

	/********************** databean *********************************/

	@Override
	public String getDatabeanName(){
		return getClass().getSimpleName();
	}

	/*************************** fields ****************************/

	@Override
	public String getKeyFieldName(){
		return DEFAULT_KEY_FIELD_NAME;
	}

	@Override
	public List<Field<?>> getKeyFields(){
		return FieldTool.prependPrefixes(getKeyFieldName(), getKey().getFields());
	}

	/************************ standard java *************************/

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj){
		if(ClassTool.differentClass(this, obj)){
			return false;
		}
		Databean<PK,D> that = (Databean<PK,D>)obj;
		return this.getKey().equals(that.getKey());
	}

	@Override
	public int hashCode(){
		return getKey().hashCode();
	}

	@Override
	public int compareTo(Databean<?,?> that){
		int diff = ClassTool.compareClass(this, that);
		if(diff != 0){
			return diff;
		}
		// must be same class
		return getKey().compareTo(((D)that).getKey());
	}

	@Override
	public String toString(){
		return getClass().getSimpleName() + "." + PrimaryKeyPercentCodec.encode(getKey());
	}

}
