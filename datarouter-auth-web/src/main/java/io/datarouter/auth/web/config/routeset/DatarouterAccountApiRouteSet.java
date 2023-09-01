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

import io.datarouter.auth.web.service.DatarouterAccountApiKeyPredicate;
import io.datarouter.auth.web.service.DatarouterAccountSignatureValidator;
import io.datarouter.auth.web.web.DatarouterAccountApiHandler;
import io.datarouter.httpclient.DatarouterServicePaths;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.security.SignatureValidator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAccountApiRouteSet extends BaseRouteSet{

	private final DatarouterAccountApiKeyPredicate apiKeyPredicate;
	private final SignatureValidator signatureValidator;

	@Inject
	public DatarouterAccountApiRouteSet(
			DatarouterServicePaths paths,
			DatarouterAccountApiKeyPredicate apiKeyPredicate,
			DatarouterAccountSignatureValidator signatureValidator){
		this.apiKeyPredicate = apiKeyPredicate;
		this.signatureValidator = signatureValidator;
		handleAnyStringAfterPath(paths.datarouter.api.accounts).withHandler(DatarouterAccountApiHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowAnonymous()
				.withApiKey(apiKeyPredicate)
				//TODO consider removing this and using apiKey only for fewer 403s/more control in handler
				.withSignature(signatureValidator)
				.withTag(Tag.DATAROUTER);
	}

}
