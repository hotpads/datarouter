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
package io.datarouter.util.aws;

import java.util.Optional;

import io.datarouter.util.net.NetTool;
import io.datarouter.util.serialization.GsonTool;

public class Ec2InstanceTool{

	public static final String EC2_INSTANCE_IDENTITY_DOCUMENT_URL =
			"http://169.254.169.254/latest/dynamic/instance-identity/document";
	public static final String EC2_PRIVATE_IP_URL = "http://169.254.169.254/latest/meta-data/local-ipv4";
	public static final String EC2_PUBLIC_IP_URL = "http://169.254.169.254/latest/meta-data/public-ipv4";

	public static Optional<Ec2InstanceDetailsDto> getEc2InstanceDetails(){
		return NetTool.curl(EC2_INSTANCE_IDENTITY_DOCUMENT_URL, false)
				.map(json -> GsonTool.GSON.fromJson(json, Ec2InstanceDetailsDto.class));
	}

	public static String getEc2InstancePublicIp(){
		return NetTool.curl(EC2_PUBLIC_IP_URL, false)
				.orElse(null);
	}

	public static String getEc2InstancePrivateIp(){
		return NetTool.curl(EC2_PRIVATE_IP_URL, false)
				.orElse(null);
	}

	public static boolean isEc2(){
		return getEc2InstanceDetails().isPresent();
	}

}
