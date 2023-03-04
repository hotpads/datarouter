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
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.count.Counters;

public class DatarouterLayeredConnectionSocketFactory
extends DatarouterConnectionSocketFactory
implements LayeredConnectionSocketFactory{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterLayeredConnectionSocketFactory.class);

	private final LayeredConnectionSocketFactory delegate;

	public DatarouterLayeredConnectionSocketFactory(LayeredConnectionSocketFactory delegate, String httpClientName){
		super(delegate, httpClientName);
		this.delegate = delegate;
	}

	@Override
	public Socket createLayeredSocket(Socket socket, String target, int port, HttpContext context) throws IOException,
			UnknownHostException{
		String host = target + ":" + port;
		logger.debug("connecting through layer httpClientName={} host={}", httpClientName, host);
		Counters.inc("HttpClientLayeredConnection global");
		Counters.inc("HttpClientLayeredConnection host " + host);
		Counters.inc("HttpClientLayeredConnection client-host " + httpClientName + " " + host);
		return delegate.createLayeredSocket(socket, target, port, context);
	}

}
