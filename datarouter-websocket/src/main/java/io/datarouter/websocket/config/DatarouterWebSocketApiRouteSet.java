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
package io.datarouter.websocket.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.httpclient.security.DefaultCsrfGenerator;
import io.datarouter.httpclient.security.DefaultSignatureGenerator;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DefaultApiKeyPredicate;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.security.DefaultCsrfValidator;
import io.datarouter.web.security.DefaultSignatureValidator;
import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.websocket.session.PushServiceSettingsSupplier;
import io.datarouter.websocket.session.WebSocketApiHandler;
import io.datarouter.websocket.session.WebSocketToolHandler;

@Singleton
public class DatarouterWebSocketApiRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterWebSocketApiRouteSet(DatarouterWebSocketPaths paths, PushServiceSettingsSupplier settings){
		super("");

		handle(paths.websocketCommand.isAlive)
				.withHandler(WebSocketApiHandler.class)
				.allowAnonymous()
				.withApiKey(new DefaultApiKeyPredicate(settings::getApiKey))
				.withCsrfToken(new DefaultCsrfValidator(new DefaultCsrfGenerator(settings::getCipherKey)))
				.withSignature(new DefaultSignatureValidator(new DefaultSignatureGenerator(settings::getSalt)));
		handle(paths.websocketCommand.push)
				.withHandler(WebSocketApiHandler.class)
				.allowAnonymous()
				.withApiKey(new DefaultApiKeyPredicate(settings::getApiKey))
				.withCsrfToken(new DefaultCsrfValidator(new DefaultCsrfGenerator(settings::getCipherKey)))
				.withSignature(new DefaultSignatureValidator(new DefaultSignatureGenerator(settings::getSalt)));

		handle(paths.datarouter.websocketTool.list)
				.withHandler(WebSocketToolHandler.class)
				.allowRoles(DatarouterUserRole.ADMIN);
		handle(paths.datarouter.websocketTool.subscriptions)
				.withHandler(WebSocketToolHandler.class)
				.allowRoles(DatarouterUserRole.ADMIN);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.withIsSystemDispatchRule(true);
	}

}
