package com.hotpads.datarouter.test.node.basic;
import java.io.IOException;

import com.google.inject.Singleton;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeFactory;
import com.hotpads.datarouter.node.op.IndexedStorageNode;
import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.node.op.SortedStorageNode;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBeanKey;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;
import com.hotpads.util.core.ListTool;


@Singleton
public class BasicNodeTestRouter
extends BaseDataRouter{

	public static final String name = "basicNodeTest";
	
	public BasicNodeTestRouter(String client) throws IOException{
		super(name, ListTool.create(new ClientId(client, true)));
		
		manyFieldTypeBeanNode = register(NodeFactory.create(client, ManyFieldTypeBean.class, this));
		sortedBeanNode = register(NodeFactory.create(client, SortedBean.class, this));
		
		activate();//do after field inits
	}

	/********************************** config **********************************/
	
	@Override
	public String getConfigLocation(){
		return DRTestConstants.CONFIG_PATH;
	}


	
	/********************************** nodes **********************************/
	
	protected Node<ManyFieldTypeBeanKey,ManyFieldTypeBean> manyFieldTypeBeanNode;
	protected Node<SortedBeanKey,SortedBean> sortedBeanNode;
	
	
	
	
	/*************************** get/set ***********************************/


	public MapStorageNode<ManyFieldTypeBeanKey,ManyFieldTypeBean> manyFieldTypeBean() {
		return cast(manyFieldTypeBeanNode);
	}

	public MapStorageNode<SortedBeanKey,SortedBean> sortedBean(){
		return cast(sortedBeanNode);
	}
	
	public static class SortedBasicNodeTestRouter extends BasicNodeTestRouter{
		public SortedBasicNodeTestRouter(String client) throws IOException{
			super(client);
		}
		public SortedStorageNode<SortedBeanKey,SortedBean> sortedBeanSorted(){
			return cast(sortedBeanNode);
		}
	}
	
	public static class IndexedBasicNodeTestRouter extends BasicNodeTestRouter{
		public IndexedBasicNodeTestRouter(String client) throws IOException{
			super(client);
		}
		public IndexedStorageNode<SortedBeanKey,SortedBean> sortedBeanIndexed(){
			return cast(sortedBeanNode);
		}
	}

	

	

}





