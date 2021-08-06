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
package io.datarouter.client.hbase.cluster;

import java.util.Comparator;

import org.apache.hadoop.hbase.ServerLoad;
import org.apache.hadoop.hbase.ServerName;

public class DrServerInfo{

	public static final Comparator<DrServerInfo> COMPARATOR_DESC_SERVER_LOAD = Comparator
			.comparing(DrServerInfo::getRegionCount)
			.reversed()
			.thenComparing(DrServerInfo::getName);

	private final ServerName serverName;
	private final ServerLoad serverLoad;
	private final String name;
	private final String hostname;

	public DrServerInfo(ServerName serverName, ServerLoad serverLoad){
		this.serverName = serverName;
		this.serverLoad = serverLoad;
		this.name = serverName.getServerName();
		this.hostname = serverName.getHostname();
	}

	private int getRegionCount(){
		return serverLoad.getLoad();
	}

	public String getName(){
		return name;
	}

	public String getHostname(){
		return hostname;
	}

	public ServerName getServerName(){
		return serverName;
	}

	public ServerLoad getServerLoad(){
		return serverLoad;
	}

}
