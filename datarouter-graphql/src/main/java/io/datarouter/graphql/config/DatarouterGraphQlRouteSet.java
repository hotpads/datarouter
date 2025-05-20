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
package io.datarouter.graphql.config;

import io.datarouter.auth.role.DatarouterUserRoleRegistry;
import io.datarouter.graphql.example.ExampleGraphQlHandler;
import io.datarouter.graphql.playground.GraphqlPlaygroundHandler;
import io.datarouter.graphql.web.GraphQlDecoder;
import io.datarouter.graphql.web.GraphQlEncoder;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.handler.encoder.DatarouterDefaultHandlerCodec;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterGraphQlRouteSet extends BaseRouteSet{

	@SuppressWarnings("deprecation")
	@Inject
	public DatarouterGraphQlRouteSet(DatarouterGraphQlPaths paths){
		handleDir(paths.graphql.playground).withHandler(GraphqlPlaygroundHandler.class);
		handle(paths.graphql.example).withHandler(ExampleGraphQlHandler.class)
				.withDefaultHandlerDecoder(GraphQlDecoder.class)
				.withDefaultHandlerEncoder(GraphQlEncoder.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(
						DatarouterUserRoleRegistry.DATAROUTER_ADMIN,
						DatarouterUserRoleRegistry.DOC_USER,
						DatarouterUserRoleRegistry.USER)
				.withDefaultHandlerCodec(DatarouterDefaultHandlerCodec.INSTANCE)
				.withTag(Tag.DATAROUTER);
	}

}
