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
package io.datarouter.storage.config.properties;

import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.ComputedPropertiesFinder;
import io.datarouter.storage.config.ComputedPropertiesFinder.FallbackPropertyValueSupplierDto;
import io.datarouter.storage.util.Ec2InstanceTool;
import io.datarouter.util.SystemTool;

@Singleton
public class ServerPrivateIp implements Supplier<String>{

	public static final String SERVER_PRIVATE_IP = "server.privateIp";

	private final String serverPrivateIp;

	@Inject
	private ServerPrivateIp(ComputedPropertiesFinder finder){
		this.serverPrivateIp = finder.findProperty(
				SERVER_PRIVATE_IP,
				List.of(
						new FallbackPropertyValueSupplierDto(
								"InetAddress.getLocalHost().getHostAddress()",
								SystemTool::getHostPrivateIp),
						new FallbackPropertyValueSupplierDto(
								Ec2InstanceTool.EC2_PRIVATE_IP_URL,
								Ec2InstanceTool::getEc2InstancePrivateIp)));
	}

	@Override
	public String get(){
		return serverPrivateIp;
	}

}
