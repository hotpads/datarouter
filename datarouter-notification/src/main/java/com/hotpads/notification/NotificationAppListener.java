package com.hotpads.notification;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.listener.DatarouterAppListener;

@Singleton
public class NotificationAppListener extends DatarouterAppListener{
	private static final Logger logger = LoggerFactory.getLogger(NotificationAppListener.class);

	@Inject
	private NotificationRouter exampleRouter;

	@Override
	protected void onStartUp(){
		logger.info("Degemer mat, this is the example app");
		Cake keyLimePie = new Cake("Key Lime Pie", Arrays.asList("Cracker", "Lime zest", "Butter", "Suggar", "Milk"),
				25, 18, 130);
		exampleRouter.cake.put(keyLimePie, null);
		Cake kouignAmann = new Cake("Kouign-amann", Arrays.asList("Butter", "Suggar", "Flour", "Salt"), 60, 30, 308);
		exampleRouter.cake.put(kouignAmann, null);
		Cake theBestCakeIntheWorld = exampleRouter.cake.get(new CakeKey("Kouign-amann"), null);
		logger.warn("The best cake in the world is the {}, it contains {} cal", theBestCakeIntheWorld.getKey()
				.getName(), theBestCakeIntheWorld.getCalorie());
	}

	@Override
	protected void onShutDown(){
		logger.info("Kenavo");
	}

}
