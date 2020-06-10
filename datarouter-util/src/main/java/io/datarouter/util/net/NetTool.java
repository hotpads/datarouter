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

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.io.ReaderTool;

public class NetTool{
	private static final Logger logger = LoggerFactory.getLogger(NetTool.class);

	public static Optional<String> curl(String location, boolean logError){
		try{
			URLConnection connection = new URL(location).openConnection();
			connection.setConnectTimeout(3_000);
			connection.setReadTimeout(3_000);
			Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
			String content = ReaderTool.accumulateStringAndClose(reader);
			return Optional.of(content);
		}catch(Exception e){
			if(logError){
				logger.error("error reading {}", location, e);
			}
			return Optional.empty();
		}
	}

}
