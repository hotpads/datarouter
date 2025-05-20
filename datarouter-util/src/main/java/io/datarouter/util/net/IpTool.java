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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.datarouter.util.string.StringTool;

public class IpTool{

	private static final Subnet LOOPBACK_RANGE = new Subnet("127.0.0.0/8");

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
		dottedDecimal = formatIp(dottedDecimal);
		if(dottedDecimal == null){
			return -1L;
		}
		String[] octets = dottedDecimal.split("\\.");
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
	public static boolean isIpAddressInSubnet(String dottedDecimalIp, Subnet subnet){
		long ip = getLongValue(dottedDecimalIp);
		return (subnet.subnetMask & subnet.baseAddress) == (subnet.subnetMask & ip);
	}

	//main use case for this is to allow these methods to work with protocol-prefixed IP addresses
	private static String formatIp(String input){
		if(input == null){
			return null;
		}
		String[] components = input.split("\\.");
		if(components.length != 4){
			return null;
		}
		for(int i = 0; i < 4; i++){
			components[i] = StringTool.retainDigits(components[i]);
			if(StringTool.isEmpty(components[i])){
				return null;
			}
			int value = Integer.parseInt(components[i]);
			if(value > 255){//can't be negative, since '-' won't make it past retainDigits
				return null;
			}
		}
		return String.join(".", components);
	}

	public static boolean isIpAddressInSubnets(String dottedDecimalIp, Subnet... subnets){
		return isIpAddressInSubnets(dottedDecimalIp, Arrays.asList(subnets));
	}

	public static boolean isIpAddressInSubnets(String dottedDecimalIp, List<Subnet> subnets){
		String formattedDottedDecimalIp = formatIp(dottedDecimalIp);
		if(formattedDottedDecimalIp == null){
			return false;
		}
		return subnets.stream()
				.anyMatch(subnet -> isIpAddressInSubnet(formattedDottedDecimalIp, subnet));
	}

	public static boolean isLoopback(String dottedDecimalIp){
		return isIpAddressInSubnets(dottedDecimalIp, LOOPBACK_RANGE);
	}

}
