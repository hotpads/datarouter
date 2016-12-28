package com.hotpads.datarouter.storage.content;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.util.core.lang.ClassTool;

public class ContentTool{

	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					C extends ContentHolder<PK,D>>
	Map<PK,C> getByKey(Collection<C> in){
		Map<PK,C> out = new HashMap<>();
		for(C contentHolder : DrCollectionTool.nullSafe(in)){
			out.put(contentHolder.getKey(), contentHolder);
		}
		return out;
	}


	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					C extends ContentHolder<PK,D>>
	boolean equalsContent(C first, C second){
		if(ClassTool.differentClass(first, second)){
			return false;
		}
		if(DrObjectTool.bothNull(first, second)){
			return true;
		}
		return first.equalsContent(second);
	}


	public static <PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					C extends ContentHolder<PK,D>>
	boolean equalsContent(Collection<C> as, Collection<C> bs){
		if(DrCollectionTool.differentSize(as, bs)){
			return false;
		}
		if(DrObjectTool.bothNull(as, bs)){
			return true;
		}
		Map<PK,C> asContentByKey = ContentTool.getByKey(as);
		if(DrCollectionTool.differentSize(asContentByKey.keySet(), as)){
			return false;//means there were duplicates
		}
		for(C b : bs){
			C contentA = asContentByKey.get(b.getKey());
			if(contentA == null || !b.equalsContent(contentA)){
				return false;
			}
		}
		return true;
	}
}
