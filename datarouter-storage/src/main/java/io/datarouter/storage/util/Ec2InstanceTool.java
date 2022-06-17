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
package io.datarouter.storage.util;

import java.time.Duration;
import java.util.Optional;

import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.util.net.NetTool;
import io.datarouter.util.tuple.Pair;

public class Ec2InstanceTool{

	public static final String
			EC2_TOKEN_URL = "http://169.254.169.254/latest/api/token",
			EC2_INSTANCE_IDENTITY_DOCUMENT_URL = "http://169.254.169.254/latest/dynamic/instance-identity/document",
			EC2_PRIVATE_IP_URL = "http://169.254.169.254/latest/meta-data/local-ipv4",
			EC2_PUBLIC_IP_URL = "http://169.254.169.254/latest/meta-data/public-ipv4",
			TTL_HEADER = "X-aws-ec2-metadata-token-ttl-seconds",
			TOKEN_HEADER = "X-aws-ec2-metadata-token";
	private static final String TTL_S = Long.toString(Duration.ofSeconds(10).toSeconds());

	private static Optional<String> makeEc2Call(String ec2Url, boolean logError){
		// IMDSv2
		return NetTool.curl("PUT", EC2_TOKEN_URL, logError, new Pair<>(TTL_HEADER, TTL_S))
				.flatMap(token -> NetTool.curl("GET", ec2Url, logError, new Pair<>(TOKEN_HEADER, token)))
				// IMDSv1
				.or(() -> NetTool.curl("GET", ec2Url, logError, null));
	}

	public static Optional<Ec2InstanceDetailsDto> getEc2InstanceDetails(boolean logError){
		return makeEc2Call(EC2_INSTANCE_IDENTITY_DOCUMENT_URL, logError)
				.map(json -> GsonTool.GSON.fromJson(json, Ec2InstanceDetailsDto.class));
	}

	public static String getEc2InstancePublicIp(){
		return makeEc2Call(EC2_PUBLIC_IP_URL, false)
				.orElse(null);
	}

	public static String getEc2InstancePrivateIp(){
		return makeEc2Call(EC2_PUBLIC_IP_URL, false)
				.orElse(null);
	}

	public static boolean isEc2(){
		return getEc2InstanceDetails(false).isPresent();
	}

}
