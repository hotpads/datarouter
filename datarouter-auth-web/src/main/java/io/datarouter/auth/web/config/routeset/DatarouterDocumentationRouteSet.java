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
package io.datarouter.auth.web.config.routeset;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.role.DatarouterUserRoleRegistry;
import io.datarouter.auth.web.web.DatarouterDocumentationHandler;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.handler.encoder.DatarouterDefaultHandlerCodec;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterDocumentationRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterDocumentationRouteSet(DatarouterAuthPaths paths){
		handle(paths.docs).withHandler(DatarouterDocumentationHandler.class);
		handle(paths.docsV2).withHandler(DatarouterDocumentationHandler.class);
		handle(paths.schema).withHandler(DatarouterDocumentationHandler.class);
		handle(paths.docs.getCsrfIv).withHandler(DatarouterDocumentationHandler.class);
		handle(paths.docs.getSignature).withHandler(DatarouterDocumentationHandler.class);
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
