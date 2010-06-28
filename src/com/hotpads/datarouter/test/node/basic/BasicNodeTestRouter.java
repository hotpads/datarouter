package com.hotpads.datarouter.test.node.basic;

import com.google.inject.ImplementedBy;
import com.hotpads.datarouter.node.op.IndexedSortedStorageNode;
import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBeanKey;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;

@ImplementedBy(HibernateBasicNodeTestRouter.class)
public interface BasicNodeTestRouter extends DataRouter{

	MapStorageNode<ManyFieldTypeBeanKey,ManyFieldTypeBean> manyFieldTypeBean();
	
	IndexedSortedStorageNode<SortedBeanKey,SortedBean> sortedBean();

	
	
	
	
}