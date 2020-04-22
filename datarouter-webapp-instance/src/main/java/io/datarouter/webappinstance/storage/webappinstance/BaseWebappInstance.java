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
package io.datarouter.webappinstance.storage.webappinstance;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.datarouter.instrumentation.webappinstance.WebappInstanceDto;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.serialize.fielder.Fielder;
import io.datarouter.scanner.Scanner;

public abstract class BaseWebappInstance<
		PK extends BaseWebappInstanceKey<PK>,
		D extends BaseWebappInstance<PK,D>>
extends BaseDatabean<PK,D>{

	private String serverType;
	private String servletContextPath;
	private String serverPublicIp;
	private String serverPrivateIp;
	private Date refreshedLast;
	private Date startupDate;
	private Date buildDate;
	private String buildId;
	private String commitId;
	private String javaVersion;
	private String servletContainerVersion;
	private String gitBranch;

	public static class FieldKeys{
		public static final StringFieldKey serverType = new StringFieldKey("serverType");
		public static final StringFieldKey servletContextPath = new StringFieldKey("servletContextPath");
		public static final StringFieldKey serverPublicIp = new StringFieldKey("serverPublicIp");
		public static final StringFieldKey serverPrivateIp = new StringFieldKey("serverPrivateIp");
		public static final DateFieldKey refreshedLast = new DateFieldKey("refreshedLast");
		public static final DateFieldKey startupDate = new DateFieldKey("startupDate");
		public static final DateFieldKey buildDate = new DateFieldKey("buildDate");
		public static final StringFieldKey buildId = new StringFieldKey("buildId");
		public static final StringFieldKey commitId = new StringFieldKey("commitId");
		public static final StringFieldKey javaVersion = new StringFieldKey("javaVersion");
		public static final StringFieldKey servletContainerVersion = new StringFieldKey("servletContainerVersion");
		public static final StringFieldKey gitBranch = new StringFieldKey("gitBranch");
	}

	public abstract static class BaseWebappInstanceFielder<
			PK extends BaseWebappInstanceKey<PK>,
			D extends BaseWebappInstance<PK,D>>
	extends BaseDatabeanFielder<PK,D>{

		public BaseWebappInstanceFielder(Class<? extends Fielder<PK>> primaryKeyFielderClass){
			super(primaryKeyFielderClass);
		}

		@Override
		public List<Field<?>> getNonKeyFields(D databean){
			return Arrays.asList(
					new StringField(FieldKeys.serverType, databean.getServerType()),
					new StringField(FieldKeys.servletContextPath, databean.getServletContextPath()),
					new StringField(FieldKeys.serverPublicIp, databean.getServerPublicIp()),
					new StringField(FieldKeys.serverPrivateIp, databean.getServerPrivateIp()),
					new DateField(FieldKeys.refreshedLast, databean.getRefreshedLast()),
					new DateField(FieldKeys.startupDate, databean.getStartupDate()),
					new DateField(FieldKeys.buildDate, databean.getBuildDate()),
					new StringField(FieldKeys.buildId, databean.getBuildId()),
					new StringField(FieldKeys.commitId, databean.getCommitId()),
					new StringField(FieldKeys.javaVersion, databean.getJavaVersion()),
					new StringField(FieldKeys.servletContainerVersion, databean.getServletContainerVersion()));
		}
	}

	public BaseWebappInstance(PK key){
		super(key);
	}

	public BaseWebappInstance(PK key, String serverType, String servletContextPath, String serverPublicIp,
			String serverPrivateIp, Date refreshedLast, Date startupDate, Date buildDate, String buildId,
			String commitId, String javaVersion, String servletContainerVersion, String gitBranch){
		super(key);
		this.serverType = serverType;
		this.servletContextPath = servletContextPath;
		this.serverPublicIp = serverPublicIp;
		this.serverPrivateIp = serverPrivateIp;
		this.refreshedLast = refreshedLast;
		this.startupDate = startupDate;
		this.buildDate = buildDate;
		this.buildId = buildId;
		this.commitId = commitId;
		this.javaVersion = javaVersion;
		this.servletContainerVersion = servletContainerVersion;
		this.gitBranch = gitBranch;
	}

	public BaseWebappInstance(PK key, WebappInstanceDto dto){
		this(key, dto.serverType, dto.servletContextPath, dto.serverPublicIp, dto.serverPrivateIp, dto.refreshedLast,
				dto.startupDate, dto.buildDate, dto.buildId, dto.commitId, dto.javaVersion, dto.servletContainerVersion,
				dto.gitBranch);
	}

	public WebappInstanceDto toDto(){
		return new WebappInstanceDto(
				getKey().getWebappName(),
				getKey().getServerName(),
				getServerType(),
				getServletContextPath(),
				getServerPublicIp(),
				getServerPrivateIp(),
				getRefreshedLast(),
				getStartupDate(),
				getBuildDate(),
				getBuildId(),
				getCommitId(),
				getJavaVersion(),
				getServletContainerVersion(),
				getGitBranch());
	}

	public static Set<String> getUniqueServerNames(Iterable<WebappInstance> ins){
		return Scanner.of(ins)
				.map(WebappInstance::getKey)
				.map(WebappInstanceKey::getServerName)
				.collect(HashSet::new);
	}

	public Duration getDurationSinceLastUpdatedMs(){
		long nowMs = System.currentTimeMillis();
		long refreshedLastOrNowMs = refreshedLast == null ? nowMs : refreshedLast.getTime();
		return Duration.ofMillis(nowMs - refreshedLastOrNowMs);
	}

	public Instant getRefreshedLastInstant(){
		return Optional.ofNullable(refreshedLast)
				.map(Date::getTime)
				.map(Instant::ofEpochMilli)
				.orElse(null);
	}

	public String getServerType(){
		return serverType;
	}

	public String getServletContextPath(){
		return servletContextPath;
	}

	public Date getRefreshedLast(){
		return refreshedLast;
	}

	public String getServerPublicIp(){
		return serverPublicIp;
	}

	public String getServerPrivateIp(){
		return serverPrivateIp;
	}

	public Date getStartupDate(){
		return startupDate;
	}

	public Date getBuildDate(){
		return buildDate;
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

	public String getGitBranch(){
		return gitBranch;
	}

}
