package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.NavigableSet;

import com.hotpads.datarouter.storage.entity.BaseEntity;

public class SortedBeanEntity extends BaseEntity<SortedBeanEntityKey>{

	public static final String
			TABLE_SortedBean = SortedBean.class.getSimpleName();//
	
	private SortedBeanEntity(){//required no-arg
		super(null);
	}
	
	public SortedBeanEntity(SortedBeanEntityKey key){
		super(key);
	}
	
	
	/********************* get databeans ************************/
	
	public NavigableSet<SortedBean> getSortedBeans(){
		return getDatabeansForTableName(SortedBean.class, TABLE_SortedBean);
	}

}
