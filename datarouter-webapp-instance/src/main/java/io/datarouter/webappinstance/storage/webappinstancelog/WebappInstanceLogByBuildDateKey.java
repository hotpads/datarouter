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

import java.util.Date;
import java.util.List;

import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.key.FieldlessIndexEntryPrimaryKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.webappinstance.storage.webappinstance.BaseWebappInstance;
import io.datarouter.webappinstance.storage.webappinstance.BaseWebappInstanceKey;

public class WebappInstanceLogByBuildDateKey
extends BaseRegularPrimaryKey<WebappInstanceLogByBuildDateKey>
implements FieldlessIndexEntryPrimaryKey<WebappInstanceLogByBuildDateKey,WebappInstanceLogKey,WebappInstanceLog>{

	private Date buildDate;
	private String webappName;
	private String serverName;
	private Date startupDate;

	public WebappInstanceLogByBuildDateKey(){
	}

	public WebappInstanceLogByBuildDateKey(Date buildDate, String webappName, String serverName, Date startupDate){
		this.buildDate = buildDate;
		this.webappName = webappName;
		this.serverName = serverName;
		this.startupDate = startupDate;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new DateField(BaseWebappInstance.FieldKeys.buildDate, buildDate),
				new StringField(BaseWebappInstanceKey.FieldKeys.webappName, webappName),
				new StringField(BaseWebappInstanceKey.FieldKeys.serverName, serverName),
				new DateField(BaseWebappInstance.FieldKeys.startupDate, startupDate));
	}

	@Override
	public WebappInstanceLogKey getTargetKey(){
		return new WebappInstanceLogKey(webappName, serverName, startupDate, buildDate);
	}

	@Override
	public FieldlessIndexEntry<WebappInstanceLogByBuildDateKey,WebappInstanceLogKey,WebappInstanceLog>
	createFromDatabean(WebappInstanceLog target){
		var index = new WebappInstanceLogByBuildDateKey(
				target.getKey().getBuildDate(),
				target.getKey().getWebappName(),
				target.getKey().getServerName(),
				target.getKey().getStartupDate());
		return new FieldlessIndexEntry<>(WebappInstanceLogByBuildDateKey.class, index);
	}

	public Date getBuildDate(){
		return buildDate;
	}

	public String getWebappName(){
		return webappName;
	}

	public String getServerName(){
		return serverName;
	}

	public Date getStartupDate(){
		return startupDate;
	}

}
