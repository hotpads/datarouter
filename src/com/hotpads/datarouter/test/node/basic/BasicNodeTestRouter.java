package com.hotpads.datarouter.test.node.basic;
import java.io.IOException;

import com.google.inject.Singleton;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeFactory;
import com.hotpads.datarouter.node.op.IndexedSortedStorageNode;
import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBeanKey;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;


@Singleton
public class BasicNodeTestRouter
extends BaseDataRouter{

	public static final String name = "basicNodeTest";
	
	public BasicNodeTestRouter(String manyFieldTypeBeanClient, String sortedBeanClient) throws IOException{
		super(name);
		
		manyFieldTypeBeanNode = register(NodeFactory.create(manyFieldTypeBeanClient, ManyFieldTypeBean.class, this));
		manyFieldTypeBean = cast(manyFieldTypeBeanNode);

		sortedBeanNode = register(NodeFactory.create(sortedBeanClient, SortedBean.class, this));
		sortedBean = cast(sortedBeanNode);
		
		activate();//do after field inits
	}

	/********************************** config **********************************/
	
	@Override
	public String getConfigLocation(){
		return DRTestConstants.CONFIG_PATH;
	}


	
	/********************************** nodes **********************************/
	
	protected Node<ManyFieldTypeBeanKey,ManyFieldTypeBean> manyFieldTypeBeanNode;
	protected MapStorageNode<ManyFieldTypeBeanKey,ManyFieldTypeBean> manyFieldTypeBean;

	protected Node<SortedBeanKey,SortedBean> sortedBeanNode;
	protected IndexedSortedStorageNode<SortedBeanKey,SortedBean> sortedBean;
	
	
	
	
	/*************************** get/set ***********************************/


	public MapStorageNode<ManyFieldTypeBeanKey,ManyFieldTypeBean> manyFieldTypeBean() {
		return manyFieldTypeBean;
	}

	public IndexedSortedStorageNode<SortedBeanKey,SortedBean> sortedBean(){
		return sortedBean;
	}
	

}





