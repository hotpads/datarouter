package com.hotpads.datarouter.storage.bundle;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.test.client.pool.PoolTestBean;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;


public class Bundle{

	protected Map<String,SingleTypeBundle<? extends Databean<?,?>>> bundleByType 
		= MapTool.createHashMap();
	
	protected <D extends Databean<?,?>> Bundle add(D databean){
		if(databean==null){ return this; }
		this.ensureSingleTypeBundleExists(databean);
		@SuppressWarnings("unchecked") 
		SingleTypeBundle<D> singleTypeBundle = (SingleTypeBundle<D>)this.bundleByType.get(databean.getClass()
				.getName());
		singleTypeBundle.add(databean);
		return this;
	}
	
	protected <D extends Databean<?,?>> Bundle add(Collection<D> databeans){
		for(D databean : CollectionTool.nullSafe(databeans)){
			this.add(databean);
		}
		return this;
	}
	
	protected <D extends Databean<?,?>> D getFirst(Class<D> clazz){
		if(clazz==null){ return null; }
		if(this.bundleByType.get(clazz.getName())==null){ return null; }
		@SuppressWarnings("unchecked")
		SingleTypeBundle<D> singleTypeBundle = (SingleTypeBundle<D>)this.bundleByType.get(clazz.getName());
		return singleTypeBundle.getFirst();
	}
	
	protected <D extends Databean<?,?>> SortedSet<D> getAllSet(Class<D> clazz){
		if(clazz==null){ return null; }
		ensureSingleTypeBundleExists(clazz);
		@SuppressWarnings("unchecked")
		SingleTypeBundle<D> singleTypeBundle = (SingleTypeBundle<D>)this.bundleByType.get(clazz.getName());
		return singleTypeBundle.getDatabeans();
	}
	
	protected <D extends Databean<?,?>> List<D> getAllList(Class<D> clazz){
		return ListTool.createArrayList(getAllSet(clazz));
	}
	
	protected <D extends Databean<?,?>> void ensureSingleTypeBundleExists(D databean){
		ensureSingleTypeBundleExists(databean.getClass());
		if(this.bundleByType.get(databean.getClass().getName())==null){//FIXME useless because of the previous line ? @Clement
			this.bundleByType.put(databean.getClass().getName(), new SingleTypeBundle<D>());
		}
	}
	
	private <D extends Databean<?,?>> void ensureSingleTypeBundleExists(Class<D> clazz){
		if(this.bundleByType.get(clazz.getName())==null){
			this.bundleByType.put(clazz.getName(), new SingleTypeBundle<D>());
		}
	}
	
	protected <D extends Databean<?,?>> void removeAll(Class<D> clazz){
		this.bundleByType.put(clazz.getName(), new SingleTypeBundle<D>());
	}
	
	/**
	 * @param <D>
	 * @param clazz needed to allow setting empty
	 * @param databeans
	 */
	protected <D extends Databean<?,?>> void set(
			Class<D> clazz, Collection<D> databeans){
		this.removeAll(clazz);
		this.add(databeans);
	}
	
	protected <D extends Databean<?,?>> void set(D databean){
		this.removeAll(databean.getClass());
		this.add(databean);
	}

	/** tests ****************************************************************/
	public static class BundleTests {
		@Test public void testModifyCollection(){
			Bundle b = new Bundle();
			b.add(new PoolTestBean(1L));
			Set<PoolTestBean> bs = b.getAllSet(PoolTestBean.class);
			Assert.assertEquals(1, bs.size());
			
			//modify collection outside of bundle
			bs.add(new PoolTestBean(2L));
			Assert.assertEquals(2, b.getAllSet(PoolTestBean.class).size());
			
			//modify empty collection outside bundle
			b = new Bundle();
			bs = b.getAllSet(PoolTestBean.class);
			Assert.assertEquals(0, b.getAllSet(PoolTestBean.class).size());
			Assert.assertEquals(0, bs.size());
			bs.add(new PoolTestBean(3L));
			Assert.assertEquals(1, b.getAllSet(PoolTestBean.class).size());
			
		}
	}
	
}
