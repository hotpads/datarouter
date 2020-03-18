/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.web.config;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.httpclient.client.BaseApplicationHttpClient;
import io.datarouter.httpclient.client.DatarouterHttpClient;
import io.datarouter.instrumentation.test.TestableService;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.DaosTestService;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.util.clazz.AnnotationTool;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatcherServletTestService;
import io.datarouter.web.file.AppFilesTestService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.listener.AppListenersClasses;

/**
 * Use this class to check for injection problems
 */
@Singleton
public class DatarouterWebBoostrapIntegrationService implements TestableService{

	private static final List<Pair<Class<?>,Boolean>> SINGLETON_CHECKS = List.of(
			new Pair<>(BaseDao.class, true),
			new Pair<>(SettingNode.class, true),
			new Pair<>(BaseHandler.class, false),
			new Pair<>(BaseRouteSet.class, true),
			new Pair<>(BaseApplicationHttpClient.class, true),
			new Pair<>(DatarouterHttpClient.class, true));

	@Inject
	private Datarouter datarouter;
	@Inject
	private DaosTestService daosTestService;
	@Inject
	private DispatcherServletTestService dispatcherServletTestService;
	@Inject
	private AppFilesTestService appFilesTestService;
	@Inject
	private AppListenersClasses appListeners;
	@Inject
	private SingletonTestService singletonTestService;

	@Override
	public void testAll(){
		testDaos();
		testHandlers();
		testFiles();
		testSingletons();
		testSingletonsForAppListeners();
	}

	@Override
	public void afterClass(){
		datarouter.shutdown();
	}

	private void testDaos(){
		daosTestService.testInitClients();
	}

	private void testHandlers(){
		dispatcherServletTestService.testHandlerInjection(null);
	}

	private void testFiles(){
		appFilesTestService.testPathNodesFilesExist();
		appFilesTestService.testSystemFilesExistAsPathNodes();
	}

	private void testSingletons(){
		SINGLETON_CHECKS.forEach(pair -> singletonTestService.checkSingletonForSubClasses(pair.getLeft(), pair
				.getRight()));
	}

	private void testSingletonsForAppListeners(){
		appListeners.getAppListenerClasses().forEach(clazz -> AnnotationTool.checkSingletonForClass(clazz, true));
	}

}
