/**
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

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IpTool{

	private static final String LOOPBACK_RANGE = "127.0.0.0/8";

	/**
	 * Convert long ip representation into ipv4 octet string.
	 * Same as mysql's INET_NTOA(ipNum)
	 */
	public static String getDottedDecimal(long num){
		long part1 = num >> 24;
		long part2 = num >> 16 & 0xFF;
		long part3 = num >> 8 & 0xFF;
		long part4 = num & 0xFF;
		return part1 + "." + part2 + "." + part3 + "." + part4;
	}

	public static List<String> getIpsInRange(long startIp, long endIp){
		if(startIp > endIp){
			throw new IllegalArgumentException("startIp should be less than endIp");
		}
		List<String> ips = new ArrayList<>();
		for(long i = startIp; i <= endIp; i++){
			ips.add(getDottedDecimal(i));
		}
		return ips;
	}

	/**
	 * Convert ipv4 octet string into long ip representation.
	 * Same as mysql's INET_ATON(dottedDecimal)
	 */
	public static long getLongValue(String dottedDecimal){
		String[] octets = dottedDecimal.split("\\.");
		if(octets == null || octets.length != 4){
			return -1L;
		}
		long part1 = Long.parseLong(octets[0]);
		long part2 = Long.parseLong(octets[1]);
		long part3 = Long.parseLong(octets[2]);
		long part4 = Long.parseLong(octets[3]);
		return (part1 << 24) + (part2 << 16) + (part3 << 8) + part4;
	}

	/**
	 * @param dottedDecimalIp - formatted like 192.168.1.1
	 * @param subnet - formatted like 192.168.1.0/28
	 */
	private static boolean isIpAddressInSubnet(String dottedDecimalIp, String subnet){
		long ip = getLongValue(dottedDecimalIp);
		long baseAddress;
		long subnetMask;
		try{
			String[] components = subnet.split("/");
			baseAddress = getLongValue(components[0]);
			int maskIdentifier = Integer.parseInt(components[1]);

			subnetMask = (1 << maskIdentifier) - 1;
			subnetMask = subnetMask << 32 - maskIdentifier;
		}catch(Exception e){
			throw new IllegalArgumentException("invalid subnet");
		}
		return (subnetMask & baseAddress) == (subnetMask & ip);
	}

	public static boolean isIpAddressInSubnets(String dottedDecimalIp, String... subnets){
		for(String subnet : subnets){
			if(isIpAddressInSubnet(dottedDecimalIp, subnet)){
				return true;
			}
		}
		return false;
	}

	public static boolean isLoopback(String dottedDecimalIp){
		return isIpAddressInSubnets(dottedDecimalIp, LOOPBACK_RANGE);
	}

	public static class Tests{
		@Test
		public void testGetDottedDecimal(){
			Assert.assertEquals(getDottedDecimal(0L), "0.0.0.0");
		}

		@Test
		public void testGetLongValue(){
			Assert.assertEquals(getLongValue("192.168.1.1"), 3232235777L);
			Assert.assertEquals(getLongValue("1.1.1.1"), 16843009L);
			Assert.assertEquals(getLongValue("0.0.0.0"), 0L);
			Assert.assertEquals(getLongValue("255.255.255.255"), 4294967295L);
		}

		@Test
		public void testRoundTripIpConversion(){
			String ip = "255.255.255.255";
			Assert.assertEquals(getDottedDecimal(getLongValue(ip)), ip);
			ip = "0.0.0.0";
			Assert.assertEquals(getDottedDecimal(getLongValue(ip)), ip);
			ip = "192.168.1.1";
			Assert.assertEquals(getDottedDecimal(getLongValue(ip)), ip);
			ip = "86.54.29.4";
			Assert.assertEquals(getDottedDecimal(getLongValue(ip)), ip);
		}

		@Test
		public void testIpInSubnet(){
			Assert.assertFalse(isIpAddressInSubnet("67.15.102.175", "67.15.102.176/28"));
			Assert.assertFalse(isIpAddressInSubnet("67.15.102.192", "67.15.102.176/28"));
			Assert.assertTrue(isIpAddressInSubnet("67.15.102.176", "67.15.102.176/28"));
			Assert.assertTrue(isIpAddressInSubnet("67.15.102.191", "67.15.102.176/28"));
		}

		@Test
		public void testGetIpsInRange(){
			String startiP = "0.0.0.0";
			String endiP = "0.0.1.0";
			Assert.assertEquals(257, getIpsInRange(getLongValue(startiP), getLongValue(endiP)).size());
			Assert.assertEquals("0.0.0.255", getIpsInRange(getLongValue(startiP), getLongValue(endiP)).get(255));
		}

		@Test
		public void testIsLoopback(){
			Assert.assertFalse(isLoopback("126.255.255.255"));
			Assert.assertTrue(isLoopback("127.0.0.0"));
			Assert.assertTrue(isLoopback("127.0.0.255"));
			Assert.assertFalse(isLoopback("128.0.0.0"));
		}
	}

}
