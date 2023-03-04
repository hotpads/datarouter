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
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.count.Counters;

public class DatarouterConnectionSocketFactory implements ConnectionSocketFactory{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterConnectionSocketFactory.class);

	public static final String REMOTE_ADDRESS = "remoteAddress";

	private final ConnectionSocketFactory delegate;
	protected final String httpClientName;

	public DatarouterConnectionSocketFactory(ConnectionSocketFactory delegate, String httpClientName){
		this.delegate = delegate;
		this.httpClientName = httpClientName;
	}

	@Override
	public Socket createSocket(HttpContext context) throws IOException{
		return delegate.createSocket(context);
	}

	@Override
	public Socket connectSocket(int connectTimeout, Socket sock, HttpHost host, InetSocketAddress remoteAddress,
			InetSocketAddress localAddress, HttpContext context) throws IOException{
		logger.debug("connecting httpClientName={} remoteAddress={}", httpClientName, remoteAddress);
		String hostString = remoteAddress.getHostString();
		Counters.inc("HttpClientConnection global");
		Counters.inc("HttpClientConnection host " + hostString);
		Counters.inc("HttpClientConnection client-host " + httpClientName + " " + hostString);
		context.setAttribute(REMOTE_ADDRESS, remoteAddress);
		return delegate.connectSocket(connectTimeout, sock, host, remoteAddress, localAddress, context);
	}

}
