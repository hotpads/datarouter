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
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.index.UniqueIndexNode;
import com.hotpads.datarouter.node.type.indexing.IndexingSortedMapStorageNode;
import com.hotpads.datarouter.node.type.redundant.RedundantSortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.notification.databean.NotificationItemLog;
import com.hotpads.notification.databean.NotificationItemLog.NotificationItemLogFielder;
import com.hotpads.notification.databean.NotificationItemLogKey;
import com.hotpads.notification.databean.NotificationLog;
import com.hotpads.notification.databean.NotificationLog.NotificationLogFielder;
import com.hotpads.notification.databean.NotificationLogKey;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationRequest.NotificationRequestFielder;
import com.hotpads.notification.databean.NotificationRequestKey;
import com.hotpads.notification.destination.NotificationDestination;
import com.hotpads.notification.destination.NotificationDestination.NotificationDestinationFielder;
import com.hotpads.notification.destination.NotificationDestinationByAppDeviceId;
import com.hotpads.notification.destination.NotificationDestinationByAppDeviceId.NotificationDestinationByAppDeviceIdFielder;
import com.hotpads.notification.destination.NotificationDestinationByAppDeviceIdKey;
import com.hotpads.notification.destination.NotificationDestinationKey;
import com.hotpads.notification.itemlog.NotificationItemLogEntity;
import com.hotpads.notification.itemlog.NotificationItemLogEntityKey;
import com.hotpads.notification.itemlog.NotificationItemLogPartitioner16;
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
import com.hotpads.notification.tracking.NotificationTrackingEvent;
import com.hotpads.notification.tracking.NotificationTrackingEvent.NotificationTrackingEventFielder;
import com.hotpads.notification.tracking.NotificationTrackingEventKey;

@Singleton
public class NotificationRouter extends BaseRouter{

	private static final String
		NAME = "notification";

	/********************************** client ids **********************************/

	public static final ClientId
			CLIENT_hbase1 = new ClientId("hbase1", true),
			CLIENT_job = new ClientId("job", true),
			CLIENT_property = new ClientId("property", true),
			CLIENT_user = new ClientId("user",true),
			CLIENT_event = new ClientId("event", true);

	private static final List<ClientId> CLIENT_IDS = Arrays.asList(CLIENT_hbase1, CLIENT_job, CLIENT_property,
			CLIENT_user, CLIENT_event);

	@Override
	public List<ClientId> getClientIds(){
		return CLIENT_IDS;
	}

	/********************************** nodes **********************************/

	public final SortedMapStorageNode<NotificationRequestKey,NotificationRequest> notificationRequest;

	public final SortedMapStorageNode<NotificationLogKey,NotificationLog> notificationLog;
	public final IndexedSortedMapStorageNode<NotificationLogKey,NotificationLog> notificationLogMySql;
	public final SortedMapStorageNode<NotificationLogKey,NotificationLog> notificationLogHbase;
	public final UniqueIndexNode<NotificationLogKey,NotificationLog,NotificationLogByIdKey,NotificationLogById>
			notificationLogById;
	public final UniqueIndexNode<NotificationLogKey,NotificationLog,NotificationLogByIdKey,NotificationLogById>
			notificationLogByIdHbase;

	public final SortedMapStorageNode<NotificationItemLogKey,NotificationItemLog> notificationItemLog;
	public final SortedMapStorageNode<NotificationItemLogKey,NotificationItemLog> notificationItemLogMysql;
	public final SortedMapStorageNode<NotificationItemLogKey,NotificationItemLog> notificationItemLogHbase;

	public final IndexedSortedMapStorageNode<NotificationDestinationKey,NotificationDestination>
			notificationDestination;
	public final UniqueIndexNode<NotificationDestinationKey,NotificationDestination,
			NotificationDestinationByAppDeviceIdKey, NotificationDestinationByAppDeviceId>
			notificationDestinationByAppDeviceId;

	public final IndexedSortedMapStorageNode<NotificationTrackingEventKey, NotificationTrackingEvent>
			notificationTrackingEvent;

	//TODO public final SortedMapStorage<NotificationPreferenceKey,NotificationPreference> notificationPreference;

	@Inject
	public NotificationRouter(Datarouter datarouter, DatarouterProperties datarouterProperties, NodeFactory nodeFactory,
			DatarouterSettings datarouterSettings){
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

		notificationLogMySql = createAndRegister(CLIENT_job, NotificationLog::new, NotificationLogFielder::new);

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
		indexingNode.registerIndexListener(IndexingNodeFactory.newUniqueListener(NotificationLogById::new, indexNode));

		notificationLogHbase = indexingNode;
		notificationLogByIdHbase = IndexingNodeFactory.newManualUnique(notificationLogHbase, indexNode);
		notificationLog = register(new RedundantSortedMapStorageNode<>(NotificationLog::new, this,
				Arrays.asList(notificationLogMySql, notificationLogHbase), notificationLogHbase));

		this.notificationItemLogMysql = createAndRegister(CLIENT_property, NotificationItemLog::new,
				NotificationItemLogFielder::new);

		EntityNodeParams<NotificationItemLogEntityKey,NotificationItemLogEntity> nilNodeParam = new EntityNodeParams<>(
				"NotificationItemLogEntity16",
				NotificationItemLogEntityKey.class,
				NotificationItemLogEntity.class,
				NotificationItemLogPartitioner16.class,
				"NotificationItemLogEntity16");

		this.notificationItemLogHbase = register(nodeFactory.subEntityNode(
				this,
				nilNodeParam,
				CLIENT_hbase1,
				NotificationItemLog::new,
				NotificationItemLogFielder::new,
				NotificationItemLogEntity.QUALIFIER_PREFIX_NOTIFICATION_ITEM_LOG));

		this.notificationItemLog = notificationItemLogHbase;

		notificationDestination = register(nodeFactory.create(CLIENT_property, NotificationDestination.class,
				NotificationDestinationFielder.class, this, true));
		notificationDestinationByAppDeviceId = notificationDestination.registerManaged(IndexingNodeFactory
				.newManagedUnique(this, notificationDestination, NotificationDestinationByAppDeviceIdFielder.class,
						NotificationDestinationByAppDeviceId.class, false));

		notificationTrackingEvent = register(nodeFactory.create(CLIENT_event, NotificationTrackingEvent.class,
				NotificationTrackingEventFielder.class, this, true));

//		notificationPreference = register(nodeFactory.create(CLIENT_user, NotificationPreference::new,
//				NotificationPreferenceFielder::new, this, true));
	}
}
