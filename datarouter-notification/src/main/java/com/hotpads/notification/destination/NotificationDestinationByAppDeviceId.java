package com.hotpads.notification.destination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;

public class NotificationDestinationByAppDeviceId
extends BaseDatabean<NotificationDestinationByAppDeviceIdKey,NotificationDestinationByAppDeviceId>
implements UniqueIndexEntry<NotificationDestinationByAppDeviceIdKey,NotificationDestinationByAppDeviceId,
NotificationDestinationKey,NotificationDestination>{

	private NotificationDestinationByAppDeviceIdKey key;

	@SuppressWarnings("unused") // used by datarouter reflection
	private NotificationDestinationByAppDeviceId(){
		this(null, null, null);
	}

	public NotificationDestinationByAppDeviceId(NotificationDestinationAppName app, String deviceId, String token){
		this.key = new NotificationDestinationByAppDeviceIdKey(app, deviceId, token);
	}

	@Override
	public NotificationDestinationKey getTargetKey(){
		return new NotificationDestinationKey(key.getToken(), key.getApp(), key.getDeviceId());
	}

	@Override
	public List<NotificationDestinationByAppDeviceId> createFromDatabean(NotificationDestination target){
		return Arrays.asList(new NotificationDestinationByAppDeviceId(target.getKey().getApp(), target.getKey()
				.getDeviceId(), target.getKey().getToken()));
	}

	@Override
	public Class<NotificationDestinationByAppDeviceIdKey> getKeyClass(){
		return NotificationDestinationByAppDeviceIdKey.class;
	}

	@Override
	public NotificationDestinationByAppDeviceIdKey getKey(){
		return key;
	}

	public static class NotificationDestinationByAppDeviceIdFielder
	extends BaseDatabeanFielder<NotificationDestinationByAppDeviceIdKey, NotificationDestinationByAppDeviceId>{

		@Override
		public Class<? extends Fielder<NotificationDestinationByAppDeviceIdKey>> getKeyFielderClass(){
			return NotificationDestinationByAppDeviceIdKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationDestinationByAppDeviceId databean){
			return new ArrayList<>();
		}

	}

	public static List<NotificationDestinationKey> getDestinationKeys(
			Iterable<NotificationDestinationByAppDeviceId> destinationByAppDeviceIds){
		List<NotificationDestinationKey> destinationKeys = new ArrayList<>();
		for(NotificationDestinationByAppDeviceId notificationDestinationByAppDeviceId : destinationByAppDeviceIds){
			destinationKeys.add(notificationDestinationByAppDeviceId.getTargetKey());
		}
		return destinationKeys;
	}

}
