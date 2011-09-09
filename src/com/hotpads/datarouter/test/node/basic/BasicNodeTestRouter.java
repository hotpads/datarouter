package com.hotpads.datarouter.test.node.basic;
import java.io.IOException;
import java.util.Random;

import com.google.inject.Singleton;
import com.hotpads.datarouter.backup.databean.BackupRecord;
import com.hotpads.datarouter.backup.databean.BackupRecord.BackupRecordFielder;
import com.hotpads.datarouter.backup.databean.BackupRecordKey;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.IndexedStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.backup.BackupBean;
import com.hotpads.datarouter.test.node.basic.backup.BackupBean.BackupBeanFielder;
import com.hotpads.datarouter.test.node.basic.backup.BackupBeanKey;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean.ManyFieldTypeBeanFielder;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBeanKey;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;
import com.hotpads.util.core.ListTool;


@Singleton
public class BasicNodeTestRouter
extends BaseDataRouter{

	public static final String name = "basicNodeTest";
	
	public BasicNodeTestRouter(String client, boolean sorted) throws IOException{
		super(null, name, ListTool.create(new ClientId(client, true)));
		
		manyFieldTypeBeanNode = register(NodeFactory.create(client, ManyFieldTypeBean.class, ManyFieldTypeBeanFielder.class, 
				new Random().nextInt(), this));
		if(sorted){
			sortedBeanNode = register(NodeFactory.create(client, SortedBean.class, SortedBeanFielder.class, this));
			backupBeanNode = register(NodeFactory.create(client, BackupBean.class, BackupBeanFielder.class, this));
			backupRecordNode = register(NodeFactory.create(client, BackupRecord.class, BackupRecordFielder.class, this));
		}
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
	protected Node<BackupBeanKey,BackupBean> backupBeanNode;
	protected Node<BackupRecordKey,BackupRecord> backupRecordNode;
	
	
	/*************************** get/set ***********************************/

	public MapStorage<ManyFieldTypeBeanKey,ManyFieldTypeBean> manyFieldTypeBean() {
		return cast(manyFieldTypeBeanNode);
	}

	public MapStorage<SortedBeanKey,SortedBean> sortedBean(){
		return cast(sortedBeanNode);
	}

	public SortedMapStorageNode<BackupBeanKey,BackupBean> backupBeanNode(){
		return cast(backupBeanNode);
	}

	public SortedMapStorageNode<BackupRecordKey,BackupRecord> backupRecordNode(){
		return cast(backupRecordNode);
	}
	
	
	/************************ sorted and indexed versions of this router *****************/
	
	public static class SortedBasicNodeTestRouter extends BasicNodeTestRouter{
		public SortedBasicNodeTestRouter(String client, boolean sorted) throws IOException{
			super(client, sorted);
		}
		public SortedStorage<SortedBeanKey,SortedBean> sortedBeanSorted(){
			return cast(sortedBeanNode);
		}
	}
	
	public static class IndexedBasicNodeTestRouter extends BasicNodeTestRouter{
		public IndexedBasicNodeTestRouter(String client, boolean sorted) throws IOException{
			super(client, sorted);
		}
		public IndexedStorage<SortedBeanKey,SortedBean> sortedBeanIndexed(){
			return cast(sortedBeanNode);
		}
	}

	

	

}





