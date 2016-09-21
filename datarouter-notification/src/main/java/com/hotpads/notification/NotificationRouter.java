package com.hotpads.notification;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.factory.IndexingNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.factory.QueueNodeFactory;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.index.UniqueIndexNode;
import com.hotpads.datarouter.node.type.indexing.IndexingSortedMapStorageNode;
import com.hotpads.datarouter.node.type.redundant.RedundantSortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.export.DataExportItem;
import com.hotpads.export.DataExportItem.DataExportItemFielder;
import com.hotpads.export.DataExportItemKey;
import com.hotpads.export.DataExportNodes;
import com.hotpads.notification.databean.NotificationLog;
import com.hotpads.notification.databean.NotificationLog.NotificationLogFielder;
import com.hotpads.notification.databean.NotificationLogKey;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationRequest.NotificationRequestFielder;
import com.hotpads.notification.databean.NotificationRequestKey;
import com.hotpads.notification.log.NotificationLogById;
import com.hotpads.notification.log.NotificationLogById.NotificationLogByIdFielder;
import com.hotpads.notification.log.NotificationLogByIdEntity;
import com.hotpads.notification.log.NotificationLogByIdEntityKey;
import com.hotpads.notification.log.NotificationLogByIdKey;
import com.hotpads.notification.log.NotificationLogByIdPartitioner16;
import com.hotpads.notification.log.NotificationLogEntity;
import com.hotpads.notification.log.NotificationLogEntityKey;
import com.hotpads.notification.log.NotificationLogPartitioner16;
import com.hotpads.notification.request.NotificationRequestEntity;
import com.hotpads.notification.request.NotificationRequestEntityKey;
import com.hotpads.notification.request.NotificationRequestPartitioner16;

@Singleton
public class NotificationRouter extends BaseRouter
implements DataExportNodes /*, MetricsNodes*/{

	private static final String
		NAME = "notification";

	/********************************** client ids **********************************/

	public static final ClientId
			CLIENT_hbase1 = new ClientId("hbase1", true),
			CLIENT_notification = new ClientId(NAME, true);

	private static final List<ClientId> CLIENT_IDS = Arrays.asList(CLIENT_hbase1, CLIENT_notification);

	@Override
	public List<ClientId> getClientIds(){
		return CLIENT_IDS;
	}

	public static final String TABLE_JobletRequest = "Joblet";//TODO rename physical table to JobletRequest

	/********************************** nodes **********************************/

	public final SortedMapStorageNode<NotificationRequestKey,NotificationRequest> notificationRequest;
	public final SortedMapStorageNode<NotificationLogKey,NotificationLog> notificationLog;
	public final IndexedSortedMapStorageNode<NotificationLogKey,NotificationLog> notificationLogMySql;
	public final SortedMapStorageNode<NotificationLogKey,NotificationLog> notificationLogHbase;
	public final UniqueIndexNode<NotificationLogKey,NotificationLog,NotificationLogByIdKey,NotificationLogById>
			notificationLogById;
	public final UniqueIndexNode<NotificationLogKey,NotificationLog,NotificationLogByIdKey,NotificationLogById>
			notificationLogByIdHbase;

	public final SortedMapStorage<DataExportItemKey,DataExportItem> dataExportItem;

	@Inject
	public NotificationRouter(Datarouter datarouter, DatarouterProperties datarouterProperties, NodeFactory nodeFactory,
			QueueNodeFactory queueNodeFactory, DatarouterSettings datarouterSettings){
		super(datarouter, datarouterProperties.getConfigPath(), NAME, nodeFactory, datarouterSettings);

		EntityNodeParams<NotificationRequestEntityKey,NotificationRequestEntity> nodeParam = new EntityNodeParams<>(
				"NotificationRequestEntity16",
				NotificationRequestEntityKey.class,
				NotificationRequestEntity.class,
				NotificationRequestPartitioner16.class,
				"NotificationRequestEntity16");

		notificationRequest = register(nodeFactory.subEntityNode(
				this,
				nodeParam,
				CLIENT_hbase1,
				NotificationRequest::new,
				NotificationRequestFielder::new,
				NotificationRequestEntity.QUALIFIER_PREFIX_NOTIFICATION_REQUEST));

		notificationLogMySql = createAndRegister(CLIENT_notification, NotificationLog::new, NotificationLogFielder::new);

		notificationLogById = notificationLogMySql.registerManaged(IndexingNodeFactory.newManagedUnique(this,
				notificationLogMySql, NotificationLogByIdFielder::new, NotificationLogById::new, false,
				"index_notificationId"));

		EntityNodeParams<NotificationLogEntityKey,NotificationLogEntity> nlNodeParam = new EntityNodeParams<>(
				"NotificationLogEntity16",
				NotificationLogEntityKey.class,
				NotificationLogEntity.class,
				NotificationLogPartitioner16.class,
				"NotificationLogEntity16");

		SortedMapStorageNode<NotificationLogKey,NotificationLog> rawNotificationLogHbase = nodeFactory.subEntityNode(
				this,
				nlNodeParam,
				CLIENT_hbase1,
				NotificationLog::new,
				NotificationLogFielder::new,
				NotificationLogEntity.QUALIFIER_PREFIX_NOTIFICATION_LOG);

		EntityNodeParams<NotificationLogByIdEntityKey,NotificationLogByIdEntity> nlByIdNodeParam =
				new EntityNodeParams<>(
				"NotificationLogByIdEntity16",
				NotificationLogByIdEntityKey.class,
				NotificationLogByIdEntity.class,
				NotificationLogByIdPartitioner16.class,
				"NotificationLogByIdEntity16");

		SortedMapStorageNode<NotificationLogByIdKey,NotificationLogById> indexNode = register(nodeFactory.subEntityNode(
				this,
				nlByIdNodeParam,
				CLIENT_hbase1,
				NotificationLogById::new,
				NotificationLogByIdFielder::new,
				NotificationLogByIdEntity.QUALIFIER_PREFIX_NOTIFICATION_LOG_BY_ID));

		IndexingSortedMapStorageNode<NotificationLogKey,NotificationLog,NotificationLogFielder,
				SortedMapStorageNode<NotificationLogKey,NotificationLog>> indexingNode = IndexingNodeFactory
				.newSortedMap(rawNotificationLogHbase);
		notificationLogHbase = indexingNode;
		indexingNode.registerIndexListener(IndexingNodeFactory.newUniqueListener(NotificationLogById::new, indexNode));
		notificationLogByIdHbase = IndexingNodeFactory.newManualUnique(notificationLogHbase, indexNode);

		notificationLog = register(new RedundantSortedMapStorageNode<>(NotificationLog::new, this,
				Arrays.asList(notificationLogMySql, notificationLogHbase), notificationLogHbase));

		dataExportItem = register(nodeFactory.create(CLIENT_notification, DataExportItem.class, DataExportItemFielder.class,
				this, true));
	}

	/*------------ DataExportNodes ---------------*/

	@Override
	public SortedMapStorage<DataExportItemKey,DataExportItem> getDataExportItem(){
		return dataExportItem;
	}
}
