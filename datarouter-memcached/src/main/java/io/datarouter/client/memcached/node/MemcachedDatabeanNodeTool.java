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
package io.datarouter.client.memcached.node;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.memcached.codec.MemcachedReservedPaths;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.HashMethods;
import io.datarouter.util.string.StringTool;

public class MemcachedDatabeanNodeTool{
	private static final Logger logger = LoggerFactory.getLogger(MemcachedDatabeanNodeTool.class);

	/**
	 * @param codecVersion Serialization format used by datarouter-memcached
	 * @param serviceName Distinguishes multiple services sharing a cluster
	 * @param clientName ClientId.getName()
	 * @param clientVersion A version that can be changed to invalidate all keys in the client
	 * @param tableName String unique to the databean type, usually matches the databean class name and backing table
	 * @param nodeVersion A version that can be changed to invalidate all keys for the node
	 * @param databeanVersion Calculated from the schema of the databean to automatically handle databean changes
	 */
	public static Subpath makeSubpath(
			String codecVersion,
			String serviceName,
			String clientName,
			String clientVersion,
			String tableName,
			String nodeVersion,
			String databeanVersion){
		List<String> segments = Scanner.of(
				codecVersion,
				serviceName,
				clientName,
				clientVersion,
				tableName,
				nodeVersion,
				databeanVersion)
				.exclude(StringTool::isEmpty)
				.each(Subpath::new)//for validation
				.list();
		String hashInput = String.join("/", segments);
		String shortenedString = Long.toString(HashMethods.longDjbHash(hashInput));
		Subpath subpath = MemcachedReservedPaths.DATABEAN.append(new Subpath(shortenedString));
		logger.warn("shortening {} to {}",
				MemcachedReservedPaths.DATABEAN.append(new Subpath(segments)),
				subpath);
		return subpath;
	}

	public static String makeDatabeanVersion(List<String> columnNames){
		String schema = String.join(",", columnNames);
		long longVersion = HashMethods.longDjbHash(schema);
		return Long.toString(longVersion);
	}

}
