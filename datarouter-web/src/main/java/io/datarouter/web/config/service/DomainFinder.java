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
package io.datarouter.web.config.service;

import java.util.Collection;

import io.datarouter.storage.config.properties.ServerClusterDomains;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DomainFinder{

	@Inject
	private PrivateDomain privateDomain;
	@Inject
	private PublicDomain publicDomain;
	@Inject
	private ServerClusterDomains serverClusterDomains;

	public String getDomainPreferPrivate(){
		return privateDomain.hasPrivateDomain() ? privateDomain.get() : publicDomain.get();
	}

	public String getDomainPreferPublic(){
		return publicDomain.hasPublicDomain() ? publicDomain.get() : privateDomain.get();
	}

	public String getRelativeDomainPreferPublic(){
		Collection<String> domains = serverClusterDomains.get();
		if(domains.isEmpty()){
			return getDomainPreferPublic();
		}
		boolean islocalhost = domains.stream()
				.anyMatch(domain -> domain.equals("localhost"));
		if(islocalhost){
			return "localhost:8443";
		}

		// can sometimes return private
		String first = domains.stream()
				.findFirst()
				.get();
		return domains.stream()
				.filter(domain -> !domain.contains("private"))
				.findFirst()
				.orElse(first);
	}

}
