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
package io.datarouter.client.hbase.test;

import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.hbase.ServerName;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.client.hbase.balancer.HBaseBalanceLeveler.TablePseudoRandomHostAndPortComparator;
import io.datarouter.client.hbase.util.ServerNameTool;

public class HBaseBalanceLevelerTests{

	@Test
	public void testComparator(){
		final int uniqueServers = 7;
		Set<ServerName> serverNames = new TreeSet<>(new TablePseudoRandomHostAndPortComparator("MyTableName"));
		for(int i = 0; i < 100; ++i){
			serverNames.add(ServerNameTool.create("SomeServer" + i % uniqueServers, 123, i));
		}
		Assert.assertEquals(serverNames.size(), uniqueServers);
	}

}
