package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.List;

import com.hotpads.datarouter.storage.entity.BaseEntity;
import com.hotpads.datarouter.storage.key.entity.base.BaseEntityPartitioner;
import com.hotpads.util.core.HashMethods;

public class SortedBeanEntity extends BaseEntity<SortedBeanEntityKey>{

	public static final String
			QUALIFIER_PREFIX_SortedBean = "SB";
	
	public static class SortedBeanEntityPartitioner extends BaseEntityPartitioner<SortedBeanEntityKey>{
		@Override
		public int getNumPartitions(){
			return 4;
		}
		@Override
		public int getPartition(SortedBeanEntityKey ek){
			String hashInput = ek.a + ek.b;
			long hash = HashMethods.longDJBHash(hashInput) % getNumPartitions();
			return (int)(hash % getNumPartitions());
		}
	}
	
	private SortedBeanEntity(){//required no-arg
		super(null);
	}
	
	public SortedBeanEntity(SortedBeanEntityKey key){
		super(key);
	}
	
	
	/********************* get databeans ************************/
	
	public List<SortedBean> getSortedBeans(){
		return getDatabeansForQualifierPrefix(SortedBean.class, QUALIFIER_PREFIX_SortedBean);
	}

}
