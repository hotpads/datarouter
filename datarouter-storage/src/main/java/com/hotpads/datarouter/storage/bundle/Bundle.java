package com.hotpads.datarouter.storage.bundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldBean;
import com.hotpads.datarouter.util.core.DrCollectionTool;


public class Bundle{

	protected Map<String,SingleTypeBundle<? extends Databean<?,?>>> bundleByType = new HashMap<>();

	protected <D extends Databean<?,?>> Bundle add(D databean){
		if(databean == null){
			return this;
		}
		this.ensureSingleTypeBundleExists(databean);
		@SuppressWarnings("unchecked")
		SingleTypeBundle<D> singleTypeBundle = (SingleTypeBundle<D>)this.bundleByType.get(databean.getClass()
				.getName());
		singleTypeBundle.add(databean);
		return this;
	}

	protected <D extends Databean<?,?>> Bundle add(Collection<D> databeans){
		for(D databean : DrCollectionTool.nullSafe(databeans)){
			this.add(databean);
		}
		return this;
	}

	protected <D extends Databean<?,?>> D getFirst(Class<D> clazz){
		if(clazz == null){
			return null;
		}
		if(this.bundleByType.get(clazz.getName()) == null){
			return null;
		}
		@SuppressWarnings("unchecked")
		SingleTypeBundle<D> singleTypeBundle = (SingleTypeBundle<D>)this.bundleByType.get(clazz.getName());
		return singleTypeBundle.getFirst();
	}

	protected <D extends Databean<?,?>> SortedSet<D> getAllSet(Class<D> clazz){
		if(clazz == null){
			return null;
		}
		ensureSingleTypeBundleExists(clazz);
		@SuppressWarnings("unchecked")
		SingleTypeBundle<D> singleTypeBundle = (SingleTypeBundle<D>)this.bundleByType.get(clazz.getName());
		return singleTypeBundle.getDatabeans();
	}

	protected <D extends Databean<?,?>> List<D> getAllList(Class<D> clazz){
		return new ArrayList<>(getAllSet(clazz));
	}

	protected <D extends Databean<?,?>> void ensureSingleTypeBundleExists(D databean){
		ensureSingleTypeBundleExists(databean.getClass());
	}

	private <D extends Databean<?,?>> void ensureSingleTypeBundleExists(Class<D> clazz){
		if(this.bundleByType.get(clazz.getName()) == null){
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

	public static class BundleTests{
		@Test
		public void testModifyCollection(){
			Bundle bundle = new Bundle();
			bundle.add(new ManyFieldBean(1L));
			Set<ManyFieldBean> bs = bundle.getAllSet(ManyFieldBean.class);
			Assert.assertEquals(1, bs.size());

			//modify collection outside of bundle
			bs.add(new ManyFieldBean(2L));
			Assert.assertEquals(2, bundle.getAllSet(ManyFieldBean.class).size());

			//modify empty collection outside bundle
			bundle = new Bundle();
			bs = bundle.getAllSet(ManyFieldBean.class);
			Assert.assertEquals(0, bundle.getAllSet(ManyFieldBean.class).size());
			Assert.assertEquals(0, bs.size());
			bs.add(new ManyFieldBean(3L));
			Assert.assertEquals(1, bundle.getAllSet(ManyFieldBean.class).size());

		}
	}

}
