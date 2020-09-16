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
package io.datarouter.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemTool{
	private static final Logger logger = LoggerFactory.getLogger(SystemTool.class);

	public static String getUserHome(){
		return System.getProperty("user.home");
	}

	public static String getJavaVersion(){
		return System.getProperty("java.version");
	}

	public static String getHostname(){
		String hostname = null;
		try{
			hostname = InetAddress.getLocalHost().getHostName();
			if(hostname.contains(".")){
				hostname = hostname.substring(0, hostname.indexOf('.'));//drop the dns suffixes
			}
		}catch(UnknownHostException e){
			logger.error("Unable to get the hostname from InetAddress.getLocalHost().getHostName()");
		}
		return hostname;
	}

	public static String getHostPrivateIp(){
		String privateIp = null;
		try{
			privateIp = InetAddress.getLocalHost().getHostAddress();
		}catch(UnknownHostException e){
			logger.error("Unable to get the private IP from InetAddress.getLocalHost().getHostAddress()");
		}
		return privateIp;
	}

}
