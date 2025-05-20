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
package io.datarouter.web.util.http;

import java.util.List;
import java.util.Optional;

import io.datarouter.util.net.IpTool;
import io.datarouter.util.net.Subnet;
import jakarta.inject.Singleton;

@Singleton
public class TrustedProxy{

	private final List<Subnet> cloudfront;
	private final List<Subnet> internalProxy;

	public TrustedProxy(List<Subnet> cloudfront, List<Subnet> internalProxy){
		this.cloudfront = cloudfront;
		this.internalProxy = internalProxy;
	}

	public Optional<Subnet> findCloudfront(String ip){
		return find(ip, cloudfront);
	}

	public Optional<Subnet> findInternalProxy(String ip){
		return find(ip, internalProxy);
	}

	private static Optional<Subnet> find(String ip, List<Subnet> subnets){
		return subnets.stream()
				.filter(subnet -> IpTool.isIpAddressInSubnet(ip, subnet))
				.findFirst();
	}

}
