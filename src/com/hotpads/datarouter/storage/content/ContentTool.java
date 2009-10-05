package com.hotpads.datarouter.storage.content;

import java.util.Collection;
import java.util.Map;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.ClassTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.ObjectTool;

public class ContentTool {
	
	public static <D extends Databean,C extends ContentHolder<D>> Map<Key<D>,C> getByKey(Collection<C> in){
		Map<Key<D>,C> out = MapTool.createHashMap();
		for(C contentHolder : CollectionTool.nullSafe(in)){
			out.put(contentHolder.getKey(), contentHolder);
		}
		return out;
	}

	public static <D extends Databean,H extends ContentHolder<D>> boolean equalsContent(H a, H b){
		if(ClassTool.differentClass(a, b)){ return false; }
		if(ObjectTool.bothNull(a, b)){ return true; }
		if(ObjectTool.isOneNullButNotTheOther(a, b)){ return false; }
		return a.equalsContent(b);
	}
	
	public static <D extends Databean,H extends ContentHolder<D>,C extends Collection<H>> 
	boolean equalsContent(C as, C bs){
		if(CollectionTool.differentSize(as, bs)){ return false; }
		if(CollectionTool.isEmpty(as)){ return true; }
		Map<Key<D>,H> asContentByKey = ContentTool.getByKey(as);
		if(CollectionTool.differentSize(asContentByKey.keySet(), as)){ return false; }//means there were duplicates
		for(H b : bs){
			H a = asContentByKey.get(b.getKey());
			if(a==null ||  ! b.equalsContent(a)){
				return false;
			}
		}
		return true;
	}
}
