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
package io.datarouter.client.mysql.connection;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Singleton;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.string.StringTool;

@Singleton
public class C3p0StatsService{

	public List<C3p0StatsDto> getC3p0Stats(){
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		ObjectName query;
		try{
			query = new ObjectName("com.mchange.v2.c3p0:type=PooledDataSource,*");
		}catch(MalformedObjectNameException e){
			throw new RuntimeException(e);
		}
		return server.queryNames(query, null).stream()
				.<Optional<C3p0StatsDto>>map(objectName -> {
						try{
							String jdbcUrl = (String)server.getAttribute(objectName, "jdbcUrl");
							Optional<String> clientName = extractClientName(jdbcUrl);
							if(clientName.isEmpty()){
								return Optional.empty();
							}
							int total = (int)server.getAttribute(objectName, "numConnections");
							int busy = (int)server.getAttribute(objectName, "numBusyConnections");
							return Optional.of(new C3p0StatsDto(clientName.get(), total, busy));
						}catch(JMException e){
							throw new RuntimeException(e);
						}
				})
				.flatMap(Optional::stream)
				.collect(Collectors.toList());
	}

	private static Optional<String> extractClientName(String jdbcUrl){
		String queryParams = StringTool.getStringAfterLastOccurrence('?', jdbcUrl);
		return Arrays.stream(queryParams.split("&"))
				.filter(part -> part.startsWith(MysqlConnectionPoolHolder.CLIENT_NAME_KEY))
				.findAny()
				.map(part -> part.substring(MysqlConnectionPoolHolder.CLIENT_NAME_KEY.length()));
	}

	public static class C3p0StatsServiceTests{

		@Test
		public void testExtractClientName(){
			String testValue = "testValue";
			Optional<String> clientName;
			clientName = extractClientName("jdbc:mysql://host:port?param1Key=param1Value&"
					+ MysqlConnectionPoolHolder.CLIENT_NAME_KEY + testValue);
			Assert.assertEquals(clientName, Optional.of(testValue));
			clientName = extractClientName("jdbc:mysql://host:port?param1Key=param1Value");
			Assert.assertEquals(clientName, Optional.empty());
		}

	}

}
