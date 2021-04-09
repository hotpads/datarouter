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
import java.util.Date;
import java.util.List;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.comparable.InstantField;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.serialize.fielder.Fielder;
import io.datarouter.webappinstance.storage.webappinstance.BaseWebappInstance;

public abstract class BaseWebappInstanceLog<
		PK extends BaseWebappInstanceLogKey<PK>,
		D extends BaseWebappInstanceLog<PK,D>>
extends BaseDatabean<PK,D>{

	protected Instant startup;
	protected Instant build;
	protected String buildId;
	protected String commitId;
	protected String javaVersion;
	protected String servletContainerVersion;
	@Deprecated
	protected Date refreshedLast;
	protected Instant refreshedLastInstant;

	public abstract static class BaseWebappInstanceLogFielder<
			PK extends BaseWebappInstanceLogKey<PK>,
			D extends BaseWebappInstanceLog<PK,D>>
	extends BaseDatabeanFielder<PK,D>{

		public BaseWebappInstanceLogFielder(Class<? extends Fielder<PK>> primaryKeyFielderClass){
			super(primaryKeyFielderClass);
		}

		@Override
		public List<Field<?>> getNonKeyFields(D databean){
			return List.of(
					new InstantField(BaseWebappInstance.FieldKeys.startup, databean.getStartup()),
					new InstantField(BaseWebappInstance.FieldKeys.build, databean.getBuild()),
					new StringField(BaseWebappInstance.FieldKeys.buildId, databean.getBuildId()),
					new StringField(BaseWebappInstance.FieldKeys.commitId, databean.getCommitId()),
					new StringField(BaseWebappInstance.FieldKeys.javaVersion, databean.getJavaVersion()),
					new StringField(BaseWebappInstance.FieldKeys.servletContainerVersion,
						databean.getServletContainerVersion()),
					new DateField(BaseWebappInstance.FieldKeys.refreshedLast, databean.getRefreshedLast()),
					new InstantField(BaseWebappInstance.FieldKeys.refreshedLastInstant,
							databean.getRefreshedLastInstant()));
		}
	}

	public BaseWebappInstanceLog(PK key){
		super(key);
	}

	public BaseWebappInstanceLog(
			PK key,
			Instant startup,
			Instant build,
			String buildId,
			String commitId,
			String javaVersion,
			String servletContainerVersion,
			Date refreshedLast,
			Instant refreshedInstant){
		super(key);
		this.startup = startup;
		this.build = build;
		this.buildId = buildId;
		this.commitId = commitId;
		this.javaVersion = javaVersion;
		this.servletContainerVersion = servletContainerVersion;
		this.refreshedLast = refreshedLast;
		this.refreshedLastInstant = refreshedInstant;
	}

	public BaseWebappInstanceLog(PK key, BaseWebappInstance<?,?> instance){
		super(key);
		this.startup = instance.getStartupInstant();
		this.build = instance.getBuildInstant();
		this.buildId = instance.getBuildId();
		this.commitId = instance.getCommitId();
		this.javaVersion = instance.getJavaVersion();
		this.servletContainerVersion = instance.getServletContainerVersion();
		this.refreshedLast = instance.getRefreshedLast();
		this.refreshedLastInstant = instance.getRefreshedLastInstant();
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

	public Date getRefreshedLast(){
		return refreshedLast;
	}

	public Instant getRefreshedLastInstant(){
		return refreshedLastInstant;
	}

	public Instant getStartup(){
		return build;
	}

	public Instant getBuild(){
		return build;
	}

}
