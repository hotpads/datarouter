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
package io.datarouter.util.net;

public class Subnet{

	public final String cidr;
	public final long baseAddress;
	public final int maskIdentifier;
	public final long subnetMask;

	public Subnet(String string){
		cidr = string;
		try{
			String[] components = string.split("/");
			baseAddress = IpTool.getLongValue(components[0]);
			maskIdentifier = Integer.parseInt(components[1]);

			subnetMask = 0xFFFFFFFFL << 32 - maskIdentifier & 0xFFFFFFFFL;
		}catch(Exception e){
			throw new IllegalArgumentException("invalid subnet " + string);
		}
	}

}
