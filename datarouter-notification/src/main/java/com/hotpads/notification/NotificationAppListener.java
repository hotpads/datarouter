package com.hotpads.notification;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.listener.DatarouterAppListener;

@Singleton
public class NotificationAppListener extends DatarouterAppListener{
	private static final Logger logger = LoggerFactory.getLogger(NotificationAppListener.class);

	@Override
	protected void onStartUp(){
		logger.info("Degemer mat, this is the example app");
	}

	@Override
	protected void onShutDown(){
		logger.info("Kenavo");
	}

}
