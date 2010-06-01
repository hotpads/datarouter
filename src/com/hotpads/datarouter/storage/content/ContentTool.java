package com.hotpads.datarouter.storage.content;

import java.util.Collection;
import java.util.Map;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ClassTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.ObjectTool;

public class ContentTool {
	
	public static <D extends Databean<PK>,
			PK extends PrimaryKey<PK>,
			C extends ContentHolder<D,PK>> 
	Map<PK,C> getByKey(Collection<C> in){
		Map<PK,C> out = MapTool.createHashMap();
		for(C contentHolder : CollectionTool.nullSafe(in)){
			out.put(contentHolder.getKey(), contentHolder);
		}
		return out;
	}

	public static <D extends Databean<PK>,
			PK extends PrimaryKey<PK>,
			C extends ContentHolder<D,PK>> 
	boolean equalsContent(C a, C b){
		if(ClassTool.differentClass(a, b)){ return false; }
		if(ObjectTool.bothNull(a, b)){ return true; }
		return a.equalsContent(b);
	}
	
	public static <D extends Databean<PK>,
			PK extends PrimaryKey<PK>,
			C extends ContentHolder<D,PK>> 
	boolean equalsContent(Collection<? extends C> as, Collection<? extends C> bs){
		if(CollectionTool.differentSize(as, bs)){ return false; }
		if(ObjectTool.bothNull(as, bs)){ return true; }
		Map<PK,? extends C> asContentByKey = ContentTool.getByKey(as);
		if(CollectionTool.differentSize(asContentByKey.keySet(), as)){ return false; }//means there were duplicates
		for(C b : bs){
			C a = asContentByKey.get(b.getKey());
			if(a==null ||  ! b.equalsContent(a)){
				return false;
			}
		}
		return true;
	}
}
