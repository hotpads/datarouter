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
package io.datarouter.webappinstance.storage.webappinstancelog;

import java.time.Instant;
import java.util.List;

import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.comparable.InstantField;
import io.datarouter.model.key.FieldlessIndexEntryPrimaryKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.webappinstance.storage.webappinstance.BaseWebappInstance;
import io.datarouter.webappinstance.storage.webappinstance.BaseWebappInstanceKey;

public class WebappInstanceLogByBuildInstantKey
extends BaseRegularPrimaryKey<WebappInstanceLogByBuildInstantKey>
implements FieldlessIndexEntryPrimaryKey<WebappInstanceLogByBuildInstantKey,WebappInstanceLogKey,WebappInstanceLog>{

	private Instant build;
	private String webappName;
	private String serverName;
	private Instant startup;

	public WebappInstanceLogByBuildInstantKey(){
	}

	public WebappInstanceLogByBuildInstantKey(Instant build, String webappName, String serverName, Instant startup){
		this.build = build;
		this.webappName = webappName;
		this.serverName = serverName;
		this.startup = startup;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new InstantField(BaseWebappInstance.FieldKeys.build, build),
				new StringField(BaseWebappInstanceKey.FieldKeys.webappName, webappName),
				new StringField(BaseWebappInstanceKey.FieldKeys.serverName, serverName),
				new InstantField(BaseWebappInstance.FieldKeys.startup, startup));
	}

	@Override
	public WebappInstanceLogKey getTargetKey(){
		return new WebappInstanceLogKey(webappName, serverName, startup, build);
	}

	@Override
	public FieldlessIndexEntry<WebappInstanceLogByBuildInstantKey,WebappInstanceLogKey,WebappInstanceLog>
	createFromDatabean(WebappInstanceLog target){
		var index = new WebappInstanceLogByBuildInstantKey(
				target.getKey().getBuild(),
				target.getKey().getWebappName(),
				target.getKey().getServerName(),
				target.getKey().getStartup());
		return new FieldlessIndexEntry<>(WebappInstanceLogByBuildInstantKey::new, index);
	}

	public Instant getBuild(){
		return build;
	}

	public String getWebappName(){
		return webappName;
	}

	public String getServerName(){
		return serverName;
	}

	public Instant getStartupDate(){
		return startup;
	}

}
