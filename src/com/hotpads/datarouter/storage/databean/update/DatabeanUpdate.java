package com.hotpads.datarouter.storage.databean.update;

import java.util.Collection;
import java.util.SortedMap;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Configs;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.storage.content.ContentHolder;
import com.hotpads.datarouter.storage.content.ContentTool;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.MapTool;

public abstract class DatabeanUpdate <PK extends PrimaryKey<PK>, D extends Databean<PK,D> & ContentHolder<PK,D>>{

	private MapStorage<PK,D> storage;
	protected <Storage extends MapStorage<PK,D>> DatabeanUpdate(Storage storage){
		this.storage = storage;
	}
	
	public void update(Collection<D> oldBeans, Collection<D> newBeans){
		SortedMap<PK,D> newBeansByKey = KeyTool.getByKeySorted(newBeans);
		for(D oldBean : CollectionTool.nullSafe(oldBeans)){
			D newBean = newBeansByKey.get(oldBean.getKey());
			if(newBean!=null){
				update(oldBean,newBean);
			}else{
				storage.delete(oldBean.getKey(), null);
			}
			newBeansByKey.remove(oldBean.getKey());
		}
		storage.putMulti(MapTool.nullSafe(newBeansByKey).values(), Configs.INSERT_OR_BUST);
	}
	
	public void update(D oldBean, D newBean){
		if(ContentTool.equalsContent(oldBean,newBean)){
			return;
		}
		if(oldBean==null && newBean!=null){
			storage.put(newBean, Configs.INSERT_OR_BUST);
		}else if(oldBean!=null && newBean==null){
			storage.delete(oldBean.getKey(),null);
		}else{
			Config putConfig = Configs.MERGE;
			if(replaceInsteadOfMerge(oldBean, newBean)){
				putConfig = Configs.INSERT_OR_BUST;
				storage.delete(oldBean.getKey(), null);
			}
			storage.put(newBean, putConfig);	
		}
	}
	
	/**
	 * Comparing all values is not required since this method is called after the content has already been compared and 
	 * determined to be different. This method should determine whether to delete the existing row and save the new 
	 * value (replace) or merge the new values into the existing row.  Usually replacement is desired if the key of the 
	 * row has changed (but the row still holds data about the same object), such as a case change in a case-insensitive 
	 * database.
	 * 
	 * @param oldBean
	 * @param newBean
	 * @return
	 */
	protected abstract boolean replaceInsteadOfMerge(D oldBean, D newBean);
}
