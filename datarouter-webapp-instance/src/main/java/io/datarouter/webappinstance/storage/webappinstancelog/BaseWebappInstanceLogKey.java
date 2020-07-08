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

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.key.primary.RegularPrimaryKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.webappinstance.storage.webappinstance.BaseWebappInstance;
import io.datarouter.webappinstance.storage.webappinstance.BaseWebappInstanceKey;

public abstract class BaseWebappInstanceLogKey<PK extends RegularPrimaryKey<PK>> extends BaseRegularPrimaryKey<PK>{

	private String webappName;
	private String serverName;
	private Date startupDate;
	private Date buildDate;

	public BaseWebappInstanceLogKey(){
	}

	public BaseWebappInstanceLogKey(String webappName, String serverName, Date startupDate, Date buildDate){
		this.webappName = webappName;
		this.serverName = serverName;
		this.startupDate = startupDate;
		this.buildDate = buildDate;
	}

	public BaseWebappInstanceLogKey(BaseWebappInstance<?,?> instance){
		this.serverName = instance.getKey().getServerName();
		this.webappName = instance.getKey().getWebappName();
		this.startupDate = instance.getStartupDate();
		this.buildDate = instance.getBuildDate();
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(BaseWebappInstanceKey.FieldKeys.webappName, webappName),
				new StringField(BaseWebappInstanceKey.FieldKeys.serverName, serverName),
				new DateField(BaseWebappInstance.FieldKeys.startupDate, startupDate),
				new DateField(BaseWebappInstance.FieldKeys.buildDate, buildDate));
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

	public Date getBuildDate(){
		return buildDate;
	}

}
