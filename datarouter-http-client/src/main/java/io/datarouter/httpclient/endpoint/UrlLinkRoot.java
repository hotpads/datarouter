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
package io.datarouter.httpclient.endpoint;

import java.util.Optional;

@Deprecated
public interface UrlLinkRoot{

	default String getScheme(){
		return "https";
	}

	String getProductionDomain();
	String getContextName();

	default String getContextPath(){
		return Optional.ofNullable(getContextName())
				.map(contextName -> "/" + contextName)
				.orElse("");
	}

	default String getProductionUrlWithContext(){
		return getScheme() + "://" + getProductionDomain() + getContextPath();
	}

	class DefaultUrlLinkRoot implements UrlLinkRoot{

		public final String productionDomain;
		public final String contextName;

		public DefaultUrlLinkRoot(String productionDomain, String contextName){
			this.productionDomain = productionDomain;
			this.contextName = contextName;
		}

		@Override
		public String getProductionDomain(){
			return productionDomain;
		}

		@Override
		public String getContextName(){
			return contextName;
		}

	}

}
