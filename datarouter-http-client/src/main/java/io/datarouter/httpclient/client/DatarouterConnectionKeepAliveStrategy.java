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

import java.time.Duration;

import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.protocol.HttpContext;

import io.datarouter.instrumentation.metric.Metrics;

/**
 * ConnectionKeepAliveStrategy that limit the usage of idle connections when the server doesn't specify an idle timeout
 */
public class DatarouterConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy{

	private final long fallbackIdleTimeoutMs;
	private final String clientName;

	public DatarouterConnectionKeepAliveStrategy(
			Duration fallbackIdleTimeout,
			String clientName){
		this.fallbackIdleTimeoutMs = fallbackIdleTimeout.toMillis();
		this.clientName = clientName;
	}

	@Override
	public long getKeepAliveDuration(HttpResponse response, HttpContext context){
		long durationMs = DefaultConnectionKeepAliveStrategy.INSTANCE.getKeepAliveDuration(response, context);
		if(durationMs >= 0){
			count("usingResponseHeader");
			return durationMs;
		}
		count("usingFallback");
		return fallbackIdleTimeoutMs;
	}

	private void count(String key){
		Metrics.count("DatarouterConnectionKeepAliveStrategy " + clientName + " " + key);
	}

}
