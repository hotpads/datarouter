package com.hotpads.datarouter.test.node.basic;
import java.io.IOException;

import com.google.inject.Singleton;
import com.hotpads.datarouter.node.HibernateNodeFactory;
import com.hotpads.datarouter.node.op.IndexedSortedStorageNode;
import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBeanKey;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;


@Singleton
public class HibernateBasicNodeTestRouter
extends BaseDataRouter implements BasicNodeTestRouter{

	public static final String name = "basicNodeTest";
	
	public HibernateBasicNodeTestRouter() throws IOException{
		super(name);
		activate();//do after field inits
	}

	/********************************** config **********************************/
	
	@Override
	public String getConfigLocation(){
		return DRTestConstants.CONFIG_PATH;
	}

	/********************************** client names **********************************/
	
	public static final String 
		client_drTest0 = DRTestConstants.CLIENT_drTest0;

	
	/********************************** nodes **********************************/
	
	private MapStorageNode<ManyFieldTypeBeanKey,ManyFieldTypeBean> manyFieldTypeBean = register(
			HibernateNodeFactory.newHibernate(client_drTest0, ManyFieldTypeBean.class, this));

	private IndexedSortedStorageNode<SortedBeanKey,SortedBean> sortedBean = register(
			HibernateNodeFactory.newHibernate(client_drTest0, SortedBean.class, this));
	
	
	
	
	/*************************** get/set ***********************************/


	public MapStorageNode<ManyFieldTypeBeanKey,ManyFieldTypeBean> manyFieldTypeBean() {
		return manyFieldTypeBean;
	}

	public IndexedSortedStorageNode<SortedBeanKey,SortedBean> sortedBean(){
		return sortedBean;
	}
	

}





