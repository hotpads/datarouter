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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.metric.Metrics;

public class DatarouterHttpClientDnsResolver extends SystemDefaultDnsResolver{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterHttpClientDnsResolver.class);

	private final String httpClientName;

	public DatarouterHttpClientDnsResolver(String httpClientName){
		this.httpClientName = httpClientName;
	}

	@Override
	public InetAddress[] resolve(String host) throws UnknownHostException{
		logger.debug("resolving httpClientName={} host={}", httpClientName, host);
		Metrics.count("HttpClientDnsResolver global");
		Metrics.count("HttpClientDnsResolver host " + host);
		Metrics.count("HttpClientDnsResolver client-host " + httpClientName + " " + host);
		return super.resolve(host);
	}

}
