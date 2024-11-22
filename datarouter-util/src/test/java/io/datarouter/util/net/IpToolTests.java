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

public class IpToolTests{

	@Test
	public void testGetDottedDecimal(){
		Assert.assertEquals(IpTool.getDottedDecimal(0L), "0.0.0.0");
	}

	@Test
	public void testGetLongValue(){
		Assert.assertEquals(IpTool.getLongValue("192.168.1.1"), 3232235777L);
		Assert.assertEquals(IpTool.getLongValue("1.1.1.1"), 16843009L);
		Assert.assertEquals(IpTool.getLongValue("0.0.0.0"), 0L);
		Assert.assertEquals(IpTool.getLongValue("255.255.255.255"), 4294967295L);

		Assert.assertEquals(IpTool.getLongValue(null), -1L);
		Assert.assertEquals(IpTool.getLongValue("126.255.255.255.5"), -1L);
		Assert.assertEquals(IpTool.getLongValue("126.255.255.a"), -1L);
		Assert.assertEquals(IpTool.getLongValue("126.255.255.256"), -1L);
	}

	@Test
	public void testRoundTripIpConversion(){
		String ip = "255.255.255.255";
		Assert.assertEquals(IpTool.getDottedDecimal(IpTool.getLongValue(ip)), ip);
		ip = "0.0.0.0";
		Assert.assertEquals(IpTool.getDottedDecimal(IpTool.getLongValue(ip)), ip);
		ip = "192.168.1.1";
		Assert.assertEquals(IpTool.getDottedDecimal(IpTool.getLongValue(ip)), ip);
		ip = "86.54.29.4";
		Assert.assertEquals(IpTool.getDottedDecimal(IpTool.getLongValue(ip)), ip);
	}

	@Test
	public void testGetIpsInRange(){
		String startIp = "0.0.0.0";
		String endIp = "0.0.1.0";
		Assert.assertEquals(257, IpTool.getIpsInRange(IpTool.getLongValue(startIp), IpTool.getLongValue(endIp)).size());
		Assert.assertEquals("0.0.0.255", IpTool.getIpsInRange(IpTool.getLongValue(startIp), IpTool.getLongValue(endIp))
				.get(255));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testIllegalArgumentGetIpsInRange(){
		String startiP = "0.0.0.0";
		String endiP = "0.0.1.0";
		IpTool.getIpsInRange(IpTool.getLongValue(endiP), IpTool.getLongValue(startiP));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testIsIpAddressInSubnetsBadSubnet(){
		IpTool.isIpAddressInSubnets("1.1.1.1", new Subnet("1.1.1.0/a"));
	}

	@Test
	public void testIsIpAddressInSubnetsBadIp(){
		Assert.assertFalse(IpTool.isIpAddressInSubnets(null, new Subnet("1.1.1.0/24")));
	}

	@Test
	public void testIsLoopback(){
		Assert.assertFalse(IpTool.isLoopback("126.255.255.255"));
		Assert.assertTrue(IpTool.isLoopback("127.0.0.0"));
		Assert.assertTrue(IpTool.isLoopback("127.0.0.255"));
		Assert.assertFalse(IpTool.isLoopback("128.0.0.0"));
	}

}