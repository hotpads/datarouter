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
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.comparable.InstantField;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.serialize.fielder.Fielder;
import io.datarouter.webappinstance.storage.webappinstance.BaseWebappInstance;

public abstract class BaseWebappInstanceLog<
		PK extends BaseWebappInstanceLogKey<PK>,
		D extends BaseWebappInstanceLog<PK,D>>
extends BaseDatabean<PK,D>{

	protected String buildId;
	protected String commitId;
	protected String javaVersion;
	protected String servletContainerVersion;
	protected Instant refreshedLastInstant;
	protected String serverPrivateIp;

	public abstract static class BaseWebappInstanceLogFielder<
			PK extends BaseWebappInstanceLogKey<PK>,
			D extends BaseWebappInstanceLog<PK,D>>
	extends BaseDatabeanFielder<PK,D>{

		public BaseWebappInstanceLogFielder(Supplier<? extends Fielder<PK>> primaryKeyFielderSupplier){
			super(primaryKeyFielderSupplier);
		}

		@Override
		public List<Field<?>> getNonKeyFields(D databean){
			return List.of(
					new StringField(BaseWebappInstance.FieldKeys.buildId, databean.getBuildId()),
					new StringField(BaseWebappInstance.FieldKeys.commitId, databean.getCommitId()),
					new StringField(BaseWebappInstance.FieldKeys.javaVersion, databean.getJavaVersion()),
					new StringField(BaseWebappInstance.FieldKeys.servletContainerVersion,
						databean.getServletContainerVersion()),
					new InstantField(BaseWebappInstance.FieldKeys.refreshedLastInstant,
							databean.getRefreshedLast()),
					new StringField(BaseWebappInstance.FieldKeys.serverPrivateIp, databean.getServerPrivateIp()));
		}
	}

	public BaseWebappInstanceLog(PK key){
		super(key);
	}

	public BaseWebappInstanceLog(
			PK key,
			String buildId,
			String commitId,
			String javaVersion,
			String servletContainerVersion,
			Instant refreshedInstant,
			String serverPrivateIp){
		super(key);
		this.buildId = buildId;
		this.commitId = commitId;
		this.javaVersion = javaVersion;
		this.servletContainerVersion = servletContainerVersion;
		this.refreshedLastInstant = refreshedInstant;
		this.serverPrivateIp = serverPrivateIp;
	}

	public BaseWebappInstanceLog(PK key, BaseWebappInstance<?,?> instance){
		super(key);
		this.buildId = instance.getBuildId();
		this.commitId = instance.getCommitId();
		this.javaVersion = instance.getJavaVersion();
		this.servletContainerVersion = instance.getServletContainerVersion();
		this.refreshedLastInstant = instance.getRefreshedLastInstant();
		this.serverPrivateIp = instance.getServerPrivateIp();
	}

	public String getBuildId(){
		return buildId;
	}

	public String getCommitId(){
		return commitId;
	}

	public String getJavaVersion(){
		return javaVersion;
	}

	public String getServletContainerVersion(){
		return servletContainerVersion;
	}

	public Instant getRefreshedLast(){
		return refreshedLastInstant;
	}

	public String getServerPrivateIp(){
		return serverPrivateIp;
	}

}
