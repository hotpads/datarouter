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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.datarouter.auth.role.Role;
import io.datarouter.auth.role.RoleApprovalType;
import io.datarouter.auth.role.RoleManager;
import io.datarouter.httpclient.client.BaseApplicationHttpClient;
import io.datarouter.httpclient.client.DatarouterHttpClient;
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
import io.datarouter.web.dispatcher.DispatchType;
import io.datarouter.web.dispatcher.DispatcherServletTestService;
import io.datarouter.web.dispatcher.RouteSet;
import io.datarouter.web.file.AppFilesTestService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.BaseHandler.Handler;
import io.datarouter.web.handler.HandlerDtoTypeTestService;
import io.datarouter.web.handler.HandlerTool;
import io.datarouter.web.listener.AppListenersClasses;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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
	private HandlerDtoTypeTestService handlerDtoTypeTestService;
	@Inject
	private AppFilesTestService appFilesTestService;
	@Inject
	private AppListenersClasses appListeners;
	@Inject
	private SingletonTestService singletonTestService;
	@Inject
	private RoleManager roleManager;
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
		testRoleApprovalTypeValidators();
		testAllApprovalTypesPresent();
		testHandlerMethodNameAndPathMatching();
		testEncoderDecoderInjection();
		testUniquePathToHandlerMapping();
		testHandlerDeprecationAnnotations();
		testExternalEndpointDispatchRules();
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
		handlerDtoTypeTestService.testHandlerDtoTypes();
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

	// Make sure RoleManager has a validator for each RoleApprovalType
	private void testRoleApprovalTypeValidators(){
		Map<Role,Map<RoleApprovalType,Integer>> roleApprovalRequirements = roleManager.getAllRoleApprovalRequirements();
		Set<RoleApprovalType> approvalTypes = Scanner.of(roleApprovalRequirements.values())
				.concatIter(Map::keySet)
				.collect(HashSet::new);
		for(RoleApprovalType approvalType : approvalTypes){
			Require.isTrue(roleManager.getApprovalTypeAuthorityValidators().containsKey(approvalType)
							&& roleManager.getApprovalTypeAuthorityValidators().get(approvalType) != null,
					"Approval type validator not found for " + approvalType);
		}
	}

	private void testAllApprovalTypesPresent(){
		Set<String> allRoleApprovalTypes = Scanner.of(roleManager.getAllRoleApprovalRequirements().values())
				.concatIter(Map::keySet)
				.map(RoleApprovalType::persistentString)
				.collect(HashSet::new);
		Set<String> missingRoleApprovalTypes = Scanner.of(allRoleApprovalTypes)
					.include(approvalType ->
							roleManager.getRoleApprovalTypeEnum().fromPersistentString(approvalType) == null)
					.collect(TreeSet::new);
		Require.isTrue(missingRoleApprovalTypes.isEmpty(), "Missing approval types: " + missingRoleApprovalTypes);
	}

	@SuppressWarnings("unused")
	private void testHandlerPublicMethods(){
		List<String> handlersWithPrivateMethods = Scanner.of(routeSetRegistry.get())
				.concatIter(RouteSet::getDispatchRulesNoRedirects)
				.map(DispatchRule::getHandlerClass)
				.concatIter(clazz -> Scanner.of(clazz.getDeclaredMethods())
						.exclude(method -> method.getAnnotation(Handler.class) == null)
						.include(method -> Modifier.isPrivate(method.getModifiers()))
						.map(method -> clazz.getSimpleName() + "." + method.getName())
						.list())
				.distinct()
				.sort()
				.list();
		Require.isTrue(handlersWithPrivateMethods.isEmpty(), "The following methods need to be public: \n"
				+ String.join("\n", handlersWithPrivateMethods));
	}

	private void testHandlerMethodNameAndPathMatching(){
		List<String> exceptions = new ArrayList<>();
		scanServiceDispatchRules()
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
		if(!exceptions.isEmpty()){
			throw new IllegalArgumentException(String.join("\n", exceptions));
		}
	}

	private void testEncoderDecoderInjection(){
		routeSetRegistry.get().forEach(this::testRouteSetHasHandlerEncoders);
		routeSetRegistry.get().forEach(this::testRouteSetHasHandlerDecoders);
	}

	private void testRouteSetHasHandlerEncoders(RouteSet routeSet){
		try{
			routeSet.getDispatchRulesNoRedirects().forEach(dispatchRule -> {
				var encoderClass = dispatchRule.getDefaultHandlerEncoder();
				injector.getInstance(encoderClass);
			});
		}catch(RuntimeException e){
			String message = String.format(
					"Invalid default encoder for routeSet=%s",
					routeSet.getClass().getCanonicalName());
			throw new RuntimeException(message);
		}
	}

	private void testRouteSetHasHandlerDecoders(RouteSet routeSet){
		try{
			routeSet.getDispatchRulesNoRedirects().forEach(dispatchRule -> {
				var decoderClass = dispatchRule.getDefaultHandlerDecoder();
				injector.getInstance(decoderClass);
			});
		}catch(RuntimeException e){
			String message = String.format(
					"Invalid default decoder for routeSet=%s",
					routeSet.getClass().getCanonicalName());
			throw new RuntimeException(message);
		}
	}

	private void testUniquePathToHandlerMapping(){
		Map<String,DispatchRule> pathToDispatchRule = new HashMap<>();
		List<String> exceptions = new ArrayList<>();
		scanServiceDispatchRules()
				.forEach(dispatchRule -> {
					String path = dispatchRule.getPattern().toString();
					if(pathToDispatchRule.containsKey(path)
							&& pathToDispatchRule.get(path).getDispatchType().equals(dispatchRule.getDispatchType())){
						exceptions.add(String.format(
								"Duplicate path pattern in %s and %s: %s",
								pathToDispatchRule.get(path).getHandlerClass().getSimpleName(),
								dispatchRule.getHandlerClass().getSimpleName(),
								path));
					}else{
						pathToDispatchRule.put(path, dispatchRule);
					}
				});
		if(!exceptions.isEmpty()){
			throw new IllegalArgumentException(String.join("\n", exceptions));
		}
	}

	private Scanner<DispatchRule> scanServiceDispatchRules(){
		return Scanner.of(routeSetRegistry.get())
				.concatIter(RouteSet::getDispatchRulesNoRedirects)
				.exclude(dispatchRule -> dispatchRule.getTag() == Tag.DATAROUTER)
				.exclude(dispatchRule -> dispatchRule.getPattern().toString().endsWith("[/]?[^/]*"))
				.exclude(dispatchRule -> dispatchRule.getPattern().toString().endsWith("*"))
				.exclude(dispatchRule -> dispatchRule.getPattern().toString().endsWith("|/"))
				.exclude(dispatchRule -> dispatchRule.getPattern().toString().endsWith("?"));
	}

	private Scanner<DispatchRule> scanServiceDispatchRulesIncludingHandleDir(){
		return Scanner.of(routeSetRegistry.get())
				.concatIter(RouteSet::getDispatchRulesNoRedirects)
				.exclude(dispatchRule -> dispatchRule.getTag() == Tag.DATAROUTER);
	}

	private void testHandlerDeprecationAnnotations(){
		List<String> exceptions = new ArrayList<>();
		scanServiceDispatchRulesIncludingHandleDir()
				.map(DispatchRule::getHandlerClass)
				.deduplicateConsecutive()
				.concatIter(HandlerTool::getHandlerAnnotatedMethods)
				.forEach(handlerMethod -> {
					try{
						HandlerTool.validateHandlerMethodDeprecationAnnotation(handlerMethod);
					}catch(IllegalArgumentException ex){
						exceptions.add(ex.getMessage());
					}
				});
		if(!exceptions.isEmpty()){
			throw new IllegalArgumentException(String.join("\n", exceptions));
		}
	}

	private void testExternalEndpointDispatchRules(){
		List<String> exceptions = new ArrayList<>();
		Scanner.of(routeSetRegistry.get())
				.concatIter(RouteSet::getDispatchRulesNoRedirects)
				.include(rule -> rule.getDispatchType() == DispatchType.EXTERNAL_ENDPOINT)
				.include(rule -> rule.getPersistentString().isEmpty())
				.forEach(rule -> {
					exceptions.add(String.format(
							"Dispatch rules for external endpoints must have a persistent string set via "
									+ "\"withPersistentString()\". RouteSet=%s, Path=%s.",
							rule.getRouteSet().getClass().getSimpleName(),
							rule.getPattern().toString()));
				});
		if(!exceptions.isEmpty()){
			throw new IllegalArgumentException(String.join("\n", exceptions));
		}
	}

}
