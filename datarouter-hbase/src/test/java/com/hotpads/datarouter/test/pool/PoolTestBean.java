package com.hotpads.datarouter.test.pool;

import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;


public class PoolTestBean extends BaseDatabean<PoolTestBeanKey,PoolTestBean>{

	private PoolTestBeanKey key;

	/***************************** columns ******************************/

	public static class PoolTestBeanFielder extends BaseDatabeanFielder<PoolTestBeanKey,PoolTestBean>{
		public PoolTestBeanFielder(){
		}
		@Override
		public Class<PoolTestBeanKey> getKeyFielderClass(){
			return PoolTestBeanKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(PoolTestBean d){
			return FieldTool.createList();
		}
	}


	/***************************** constructor **************************************/

	PoolTestBean(){
		this.key = new PoolTestBeanKey(null);
	}

	public PoolTestBean(Long id) {
		this.key = new PoolTestBeanKey(id);
	}


	/***************************** method ************************************/

	@Override
	public Class<PoolTestBeanKey> getKeyClass() {
		return PoolTestBeanKey.class;
	}

	@Override
	public PoolTestBeanKey getKey() {
		return key;
	}


	/***************************** get/set **************************************/

	public void setKey(PoolTestBeanKey key){
		this.key = key;
	}

	public Long getId(){
		return key.getId();
	}

	public void setId(Long id){
		key.setId(id);
	}

}
