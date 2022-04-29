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
package io.datarouter.email.email;

import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Optional;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.pathnode.PathNode;

public class DatarouterEmailLinkBuilder{

	private String protocol;
	private String hostPort;
	private String contextPath;
	private String localPath;
	private String fragment;
	private LinkedHashMap<String,String> params = new LinkedHashMap<>();

	public DatarouterEmailLinkBuilder withProtocol(String hrefProtocol){
		this.protocol = hrefProtocol;
		return this;
	}

	public DatarouterEmailLinkBuilder withHostPort(String hostPort){
		this.hostPort = hostPort;
		return this;
	}

	public DatarouterEmailLinkBuilder withContextPath(String hrefContextPath){
		this.contextPath = hrefContextPath;
		return this;
	}

	public DatarouterEmailLinkBuilder withLocalPath(String hrefLocalPath){
		this.localPath = hrefLocalPath;
		return this;
	}

	public DatarouterEmailLinkBuilder withLocalPath(PathNode hrefLocalPathNode){
		this.localPath = hrefLocalPathNode.toSlashedString();
		return this;
	}

	public DatarouterEmailLinkBuilder withParam(String key, String value){
		this.params.put(key, value);
		return this;
	}

	public DatarouterEmailLinkBuilder withFragment(String fragment){
		this.fragment = fragment;
		return this;
	}

	public String build(){
		URIBuilder builder = new URIBuilder();
		builder.setScheme(protocol);
		builder.setHost(hostPort);
		builder.setPath(contextPath + Optional.ofNullable(localPath).orElse(""));
		params.forEach(builder::addParameter);
		builder.setFragment(fragment);
		try{
			return builder.build().toString();
		}catch(URISyntaxException e){
			throw new RuntimeException(e);
		}
	}

}
