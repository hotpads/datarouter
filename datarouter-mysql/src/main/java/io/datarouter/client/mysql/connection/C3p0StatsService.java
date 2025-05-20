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
package io.datarouter.client.mysql.connection;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.JmxTool;
import io.datarouter.util.string.StringTool;
import jakarta.inject.Singleton;

@Singleton
public class C3p0StatsService{
	private static final Logger logger = LoggerFactory.getLogger(C3p0StatsService.class);

	public List<C3p0StatsDto> getC3p0Stats(){
		ObjectName query;
		try{
			query = new ObjectName("com.mchange.v2.c3p0:type=PooledDataSource,*");
		}catch(MalformedObjectNameException e){
			throw new RuntimeException(e);
		}
		return JmxTool.SERVER.queryNames(query, null).stream()
				.<Optional<C3p0StatsDto>>map(objectName -> {
					String jdbcUrl = (String)JmxTool.getAttribute(objectName, "jdbcUrl");
					Optional<String> clientName = extractClientName(jdbcUrl);
					if(clientName.isEmpty()){
						return Optional.empty();
					}
					int total = (int)JmxTool.getAttribute(objectName, "numConnections");
					int busy = (int)JmxTool.getAttribute(objectName, "numBusyConnections");
					return Optional.of(new C3p0StatsDto(clientName.get(), total, busy));
				})
				.flatMap(Optional::stream)
				.toList();
	}

	protected static Optional<String> extractClientName(String jdbcUrl){
		String queryParams = StringTool.getStringAfterLastOccurrence('?', jdbcUrl);
		if(queryParams == null){
			logger.warn("could not extract client name from jdbcUrl={}", jdbcUrl);
			return Optional.empty();
		}
		return Arrays.stream(queryParams.split("&"))
				.filter(part -> part.startsWith(MysqlConnectionPoolHolder.CLIENT_NAME_KEY))
				.findAny()
				.map(part -> part.substring(MysqlConnectionPoolHolder.CLIENT_NAME_KEY.length()));
	}

}
