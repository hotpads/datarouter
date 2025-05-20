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

import org.testng.Assert;
import org.testng.annotations.Test;

public class SubnetTests{

	private static final String[] SUBNET_MASKS = {
			"0.0.0.0",
			"128.0.0.0",
			"192.0.0.0",
			"224.0.0.0",
			"240.0.0.0",
			"248.0.0.0",
			"252.0.0.0",
			"254.0.0.0",
			"255.0.0.0",
			"255.128.0.0",
			"255.192.0.0",
			"255.224.0.0",
			"255.240.0.0",
			"255.248.0.0",
			"255.252.0.0",
			"255.254.0.0",
			"255.255.0.0",
			"255.255.128.0",
			"255.255.192.0",
			"255.255.224.0",
			"255.255.240.0",
			"255.255.248.0",
			"255.255.252.0",
			"255.255.254.0",
			"255.255.255.0",
			"255.255.255.128",
			"255.255.255.192",
			"255.255.255.224",
			"255.255.255.240",
			"255.255.255.248",
			"255.255.255.252",
			"255.255.255.254",
			"255.255.255.255"
	};

	@Test
	public void testSubnetMasks(){
		for(int maskIdentifier = 0; maskIdentifier <= 32; maskIdentifier++){
			Subnet subnet = new Subnet("192.168.0.0/" + maskIdentifier);
			String expectedSubnetMask = SUBNET_MASKS[maskIdentifier];
			Assert.assertEquals(
					IpTool.getDottedDecimal(subnet.subnetMask),
					expectedSubnetMask,
					"Failed for maskIdentifier: " + maskIdentifier);
		}
	}

}
