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

public class Ec2InstanceDetailsDto{

	public final String instanceId;
	public final String instanceType;
	public final String availabilityZone;
	public final String region;

	public Ec2InstanceDetailsDto(String instanceId, String instanceType, String availabilityZone, String region){
		this.instanceId = instanceId;
		this.instanceType = instanceType;
		this.availabilityZone = availabilityZone;
		this.region = region;
	}

}
