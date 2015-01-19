package com.hotpads.datarouter.test.node.basic.backup;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.backup.databean.BackupRecord;
import com.hotpads.datarouter.backup.databean.BackupRecord.BackupRecordFielder;
import com.hotpads.datarouter.backup.databean.BackupRecordKey;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.backup.BackupBean.BackupBeanFielder;
import com.hotpads.util.core.ListTool;

@Singleton
public class BackupTestRouter extends BaseDatarouter{
	
	@Inject
	public BackupTestRouter(DatarouterContext drContext, NodeFactory nodeFactory, String clientName){
		super(drContext, DRTestConstants.CONFIG_PATH, BackupTestRouter.class.getSimpleName());
		
		backupBeanNode = register(nodeFactory.create(clientName, BackupBean.class, BackupBeanFielder.class, this, 
				false));
		backupRecordNode = register(nodeFactory.create(clientName, BackupRecord.class, BackupRecordFielder.class, this,
				false));

		registerWithContext();//do after field inits
	}

	/********************************** config **********************************/

	public static final List<ClientId> CLIENT_IDS = ListTool.create(
			new ClientId(DRTestConstants.CLIENT_drTestJdbc0, true),
			new ClientId(DRTestConstants.CLIENT_drTestHibernate0, true),
			new ClientId(DRTestConstants.CLIENT_drTestHBase, true),
			new ClientId(DRTestConstants.CLIENT_drTestMemcached, true));
	
	@Override
	public List<ClientId> getClientIds(){
		return CLIENT_IDS;
	}


	
	/********************************** nodes **********************************/
	
	protected Node<BackupBeanKey,BackupBean> backupBeanNode;
	protected Node<BackupRecordKey,BackupRecord> backupRecordNode;

	
	/*************************** get/set ***********************************/

	public SortedMapStorageNode<BackupBeanKey,BackupBean> backupBeanNode(){
		return cast(backupBeanNode);
	}

	public SortedMapStorageNode<BackupRecordKey,BackupRecord> backupRecordNode(){
		return cast(backupRecordNode);
	}
	
}





