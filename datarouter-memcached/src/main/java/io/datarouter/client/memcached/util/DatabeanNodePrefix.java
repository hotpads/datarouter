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
package io.datarouter.client.memcached.util;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.HashMethods;
import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;

public class DatabeanNodePrefix{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanNodePrefix.class);

	private final Subpath prefix;
	private final String codecVersion;
	private final String serviceName;
	private final String clientVersion;
	private final NodeParams<?,?,?> params;
	private final PhysicalDatabeanFieldInfo<?,?,?> fieldInfo;

	/**
	 * @param codecVersion Serialization format used by datarouter-memcached
	 * @param serviceName Distinguishes multiple services sharing a cluster
	 * @param clientVersion A version that can be changed to invalidate all keys in the client
	 */
	public DatabeanNodePrefix(
			Subpath prefix,
			String codecVersion,
			String serviceName,
			String clientVersion,
			NodeParams<?,?,?> params,
			PhysicalDatabeanFieldInfo<?,?,?> fieldInfo){
		this.prefix = prefix;
		this.codecVersion = codecVersion;
		this.serviceName = serviceName;
		this.clientVersion = clientVersion;
		this.params = params;
		this.fieldInfo = fieldInfo;
	}

	public Subpath makeSubpath(){
		return prefix.append(makeFullSuffix());
	}

	public Subpath makeShortenedSubpath(){
		return prefix.append(makeShortenedSuffix());
	}

	/*-------------- private ---------------*/

	private Subpath makeFullSuffix(){
		return Scanner.of(
				codecVersion,
				serviceName,
				params.getClientId().getName(),
				clientVersion,
				fieldInfo.getTableName(),
				makeNodeVersion(),
				makeDatabeanVersion())
				.exclude(StringTool::isEmpty)
				.listTo(Subpath::new);
	}

	private Subpath makeShortenedSuffix(){
		String fullSuffix = makeFullSuffix().toString();
		//strip trailing slash for backwards compatibility
		Require.isTrue(fullSuffix.endsWith("/"));
		String hashInput = fullSuffix.substring(0, fullSuffix.length() - 1);
		String shortenedSuffix = Long.toString(HashMethods.longDjbHash(hashInput));
		logger.warn("shortening suffix {} to {}", fullSuffix, shortenedSuffix);
		return new Subpath(shortenedSuffix);
	}

	private String makeNodeVersion(){
		return Optional.ofNullable(params.getSchemaVersion())
				.map(Object::toString)
				.orElse("");
	}

	private String makeDatabeanVersion(){
		String schema = String.join(",", fieldInfo.getFieldColumnNames());
		long longVersion = HashMethods.longDjbHash(schema);
		return Long.toString(longVersion);
	}

}
