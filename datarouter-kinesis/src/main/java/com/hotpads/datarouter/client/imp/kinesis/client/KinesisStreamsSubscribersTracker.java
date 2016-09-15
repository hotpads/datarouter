package com.hotpads.datarouter.client.imp.kinesis.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hotpads.datarouter.config.DatarouterStreamSubscriberAccessor;

public class KinesisStreamsSubscribersTracker{
	private final Map<String,DatarouterStreamSubscriberAccessor> subscriberIdToSubscriberAccessor = new ConcurrentHashMap<>();

	public void registerSubscriber(String subscriberId, DatarouterStreamSubscriberAccessor subscriberAccessor){
		subscriberIdToSubscriberAccessor.putIfAbsent(subscriberId, subscriberAccessor);
	}

	public void deregisterSubscriber(String subscriberId){
		subscriberIdToSubscriberAccessor.remove(subscriberId);
	}

	public void unsubscribeAll(){
		subscriberIdToSubscriberAccessor.values().stream().forEach(subscriber -> subscriber.unsubscribe());
	}
}
