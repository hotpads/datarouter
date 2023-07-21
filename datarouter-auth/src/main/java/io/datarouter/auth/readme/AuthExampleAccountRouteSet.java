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
package io.datarouter.auth.readme;

import io.datarouter.auth.service.DatarouterAccountApiKeyPredicate;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.handler.TestApiHandler;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class AuthExampleAccountRouteSet extends BaseRouteSet{

	private final DatarouterAccountApiKeyPredicate datarouterAccountApiKeyPredicate;

	@Inject
	public AuthExampleAccountRouteSet(DatarouterAccountApiKeyPredicate datarouterAccountApiKeyPredicate){
		this.datarouterAccountApiKeyPredicate = datarouterAccountApiKeyPredicate;

		handleDir("/testApi")
				.withHandler(TestApiHandler.class)
				.withPersistentString("/testApi");
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule.allowAnonymous()
				.withApiKey(datarouterAccountApiKeyPredicate);
	}

}
