package com.hotpads.datarouter.test.node.basic.sorted;

import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

@SuppressWarnings("serial")
@Entity()
@AccessType("field")
public class IndexedSortedBean extends SortedBean{
	
	@Override
	public Class<IndexedSortedKey> getKeyClass(){
		return IndexedSortedKey.class;
	}
	
}
