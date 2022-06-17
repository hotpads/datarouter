/*
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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.httpclient.client.BaseApplicationHttpClient;
import io.datarouter.httpclient.client.DatarouterHttpClient;
import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.test.TestableService;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.DaosTestService;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.Require;
import io.datarouter.util.clazz.AnnotationTool;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.dispatcher.DispatcherServletTestService;
import io.datarouter.web.dispatcher.RouteSet;
import io.datarouter.web.file.AppFilesTestService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.BaseHandler.Handler;
import io.datarouter.web.handler.HandlerTool;
import io.datarouter.web.listener.AppListenersClasses;
import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.web.user.session.service.Role;
import io.datarouter.web.user.session.service.RoleManager;

/**
 * Use this class to check for injection problems
 */
@Singleton
public class DatarouterWebBoostrapIntegrationService implements TestableService{

	private static final Map<Class<?>,Boolean> SINGLETON_CHECKS = Map.of(
			BaseDao.class, true,
			SettingNode.class, true,
			BaseHandler.class, false,
			BaseRouteSet.class, true,
			BaseApplicationHttpClient.class, true,
			DatarouterHttpClient.class, true);

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
	@Inject
	private RoleManager manager;
	@Inject
	private DatarouterInjector injector;
	@Inject
	private RouteSetRegistry routeSetRegistry;

	@Override
	public void testAll(){
		testDaos();
		testHandlers();
		testFiles();
		testSingletons();
		testSingletonsForAppListeners();
		testSingletonsForSeralizers();
		testAllRoles();
		testHandlerMethodNameAndPathMatching();
//		testHandlerMatching();
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
		SINGLETON_CHECKS.forEach((key, value) -> singletonTestService.checkSingletonForSubClasses(key, value));
	}

	private void testSingletonsForAppListeners(){
		appListeners.getAppListenerClasses().forEach(clazz -> AnnotationTool.checkSingletonForClass(clazz, true));
	}

	private void testSingletonsForSeralizers(){
		injector.getInstancesOfType(DatarouterHttpClient.class).values().stream()
				.map(DatarouterHttpClient::getJsonSerializer)
				.distinct()
				.map(JsonSerializer::getClass)
				.forEach(clazz -> AnnotationTool.checkSingletonForClass(clazz, true));
	}

	// Make sure RoleEnum overriders have all values
	private void testAllRoles(){
		Set<Role> roles = manager.getAllRoles();
		Scanner.of(DatarouterUserRole.values())
				.forEach(role -> Require.isTrue(roles.contains(role.getRole()),
						role.getPersistentString() + " needs to be added to the RoleEnum"));
	}

	@SuppressWarnings("unused")
	private void testHandlerPublicMethods(){
		List<String> handlersWithPrivateMethods = Scanner.of(routeSetRegistry.get())
				.concatIter(RouteSet::getDispatchRules)
				.map(DispatchRule::getHandlerClass)
				.concatIter(clazz -> Scanner.of(clazz.getDeclaredMethods())
						.exclude(method -> method.getAnnotation(Handler.class) == null)
						.include(method -> Modifier.isPrivate(method.getModifiers()))
						.map(method -> clazz.getSimpleName() + "." + method.getName())
						.list())
				.distinct()
				.sort()
				.list();
		Require.isTrue(handlersWithPrivateMethods.size() == 0, "The following methods need to be public: \n"
				+ handlersWithPrivateMethods.stream().collect(Collectors.joining("\n")));
	}

	private void testHandlerMethodNameAndPathMatching(){
		List<String> exceptions = new ArrayList<>();
		Scanner.of(routeSetRegistry.get())
				.concatIter(RouteSet::getDispatchRules)
				.exclude(dispatchRule -> dispatchRule.getTag() == Tag.DATAROUTER)
				.exclude(dispatchRule -> dispatchRule.getPattern().toString().endsWith("[/]?[^/]*"))
				.exclude(dispatchRule -> dispatchRule.getPattern().toString().endsWith("*"))
				.exclude(dispatchRule -> dispatchRule.getPattern().toString().endsWith("|/"))
				.exclude(dispatchRule -> dispatchRule.getPattern().toString().endsWith("?"))
				.forEach(dispatchRule -> {
					String path = Scanner.of(dispatchRule.getPattern().toString().split("/"))
							.findLast()
							.get();
					try{
						HandlerTool.assertHandlerHasMethod(dispatchRule.getHandlerClass(), path);
					}catch(IllegalArgumentException ex){
						exceptions.add(ex.getMessage());
					}
				});
		if(exceptions.size() != 0){
			throw new IllegalArgumentException(String.join("\n", exceptions));
		}
	}

}
