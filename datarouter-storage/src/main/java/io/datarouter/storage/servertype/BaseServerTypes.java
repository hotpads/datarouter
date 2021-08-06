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
package io.datarouter.storage.servertype;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.web.HtmlSelectOptionBean;

public class BaseServerTypes implements ServerTypes{

	private final Set<ServerType> serverTypes;
	private final ServerType webServerType;
	private final ServerType jobServerType;
	private final ServerType jobletServerType;

	public BaseServerTypes(ServerType defaultServerType){
		this(defaultServerType, defaultServerType, defaultServerType);
	}

	protected BaseServerTypes(
			ServerType webServerType,
			ServerType jobServerType,
			ServerType jobletServerType){
		this.serverTypes = new TreeSet<>();
		this.serverTypes.add(ServerType.ALL);
		this.serverTypes.add(ServerType.DEV);
		this.serverTypes.add(ServerType.UNKNOWN);

		this.webServerType = webServerType;
		this.jobServerType = jobServerType;
		this.jobletServerType = jobletServerType;
		this.serverTypes.addAll(List.of(webServerType, jobServerType, jobletServerType));
	}

	public static ServerType makeProduction(String persistentString){
		return new SingleServerType(persistentString, true);
	}

	public BaseServerTypes add(ServerType serverType){
		this.serverTypes.add(serverType);
		return this;
	}

	@Override
	public Scanner<ServerType> values(){
		return Scanner.of(serverTypes);
	}

	@Override
	public ServerType getWebServerType(){
		return webServerType;
	}

	@Override
	public ServerType getJobServerType(){
		return jobServerType;
	}

	@Override
	public ServerType getJobletServerType(){
		return jobletServerType;
	}

	@Override
	public ServerType fromPersistentString(String str){
		return values()
				.include(serverType -> serverType.getPersistentString().equals(str))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("Unknown server type: " + str));
	}

	@Override
	public List<HtmlSelectOptionBean> getHtmlSelectOptionsVarNames(){
		return values()
				.map(serverType -> new HtmlSelectOptionBean(serverType.getDisplay(), serverType.getPersistentString()))
				.list();
	}

}
