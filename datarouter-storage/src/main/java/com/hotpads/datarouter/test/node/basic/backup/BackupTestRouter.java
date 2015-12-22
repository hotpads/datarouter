package com.hotpads.datarouter.test.node.basic.backup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.backup.databean.BackupRecord;
import com.hotpads.datarouter.backup.databean.BackupRecord.BackupRecordFielder;
import com.hotpads.datarouter.backup.databean.BackupRecordKey;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.backup.BackupBean.BackupBeanFielder;

@Singleton
public class BackupTestRouter extends BaseRouter{

	private final List<ClientId> clientIds;

	protected SortedMapStorageNode<BackupBeanKey,BackupBean> backupBeanNode;
	protected SortedMapStorageNode<BackupRecordKey,BackupRecord> backupRecordNode;

	@Inject
	public BackupTestRouter(Datarouter datarouter, NodeFactory nodeFactory, ClientId clientId){
		super(datarouter, DrTestConstants.CONFIG_PATH, BackupTestRouter.class.getSimpleName());

		this.clientIds = new ArrayList<>();
		this.clientIds.add(clientId);

		backupBeanNode = register(nodeFactory.create(clientId, BackupBean.class, BackupBeanFielder.class, this,
				false));
		backupRecordNode = register(nodeFactory.create(clientId, BackupRecord.class, BackupRecordFielder.class, this,
				false));

	}

	/********************************** config **********************************/

	@Override
	public List<ClientId> getClientIds(){
		return clientIds;
	}

	/*************************** get/set ***********************************/

	public SortedMapStorageNode<BackupBeanKey,BackupBean> backupBeanNode(){
		return backupBeanNode;
	}

	public SortedMapStorageNode<BackupRecordKey,BackupRecord> backupRecordNode(){
		return backupRecordNode;
	}

}