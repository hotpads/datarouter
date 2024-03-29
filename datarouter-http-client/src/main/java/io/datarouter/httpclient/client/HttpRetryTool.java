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
package io.datarouter.httpclient.client;

import java.io.IOException;
import java.util.function.Supplier;

import org.apache.http.HttpEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRetryTool{
	private static final Logger logger = LoggerFactory.getLogger(HttpRetryTool.class);

	public static final int DEFAULT_RETRY_COUNT = 1;
	public static final String RETRY_SAFE_ATTRIBUTE = "retrySafe";

	private static boolean isRetrySafe(HttpContext context){
		Object retrySafe = context.getAttribute(RETRY_SAFE_ATTRIBUTE);
		// if our http client is passed to another library it will be missing the retry safe attribute
		if(retrySafe == null){
			return false;
		}
		return (boolean)retrySafe;
	}

	public static boolean shouldRetry(HttpContext context, int executionCount, Supplier<Integer> retryCount){
		logger.info("executionCount={} retryCount={}", executionCount, retryCount.get());
		return isRetrySafe(context) && executionCount <= retryCount.get();
	}

	public static String tryEntityToString(HttpEntity httpEntity){
		try{
			return EntityUtils.toString(httpEntity);
		}catch(IOException e){
			logger.error("Exception occurred while reading HTTP response entity", e);
			return "INACCESSIBLE ENTITY: " + e.getMessage();
		}finally{
			EntityUtils.consumeQuietly(httpEntity);
		}
	}

}
