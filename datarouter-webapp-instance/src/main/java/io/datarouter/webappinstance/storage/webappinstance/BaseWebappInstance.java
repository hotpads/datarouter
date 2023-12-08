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
package io.datarouter.webappinstance.storage.webappinstance;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import io.datarouter.instrumentation.webappinstance.WebappInstanceDto;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.InstantField;
import io.datarouter.model.field.imp.comparable.InstantFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
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
	private Instant refreshedLastInstant;
	private Instant startup;
	private Instant build;
	private String buildId;
	private String commitId;
	private String javaVersion;
	private String servletContainerVersion;
	private String gitBranch;
	private Integer httpsPort;

	public static class FieldKeys{
		public static final StringFieldKey serverType = new StringFieldKey("serverType");
		public static final StringFieldKey servletContextPath = new StringFieldKey("servletContextPath");
		public static final StringFieldKey serverPublicIp = new StringFieldKey("serverPublicIp");
		public static final StringFieldKey serverPrivateIp = new StringFieldKey("serverPrivateIp");
		public static final InstantFieldKey refreshedLastInstant = new InstantFieldKey("refreshedLastInstant");
		public static final InstantFieldKey startup = new InstantFieldKey("startup");
		public static final InstantFieldKey build = new InstantFieldKey("build");
		public static final StringFieldKey buildId = new StringFieldKey("buildId");
		public static final StringFieldKey commitId = new StringFieldKey("commitId");
		public static final StringFieldKey javaVersion = new StringFieldKey("javaVersion");
		public static final StringFieldKey servletContainerVersion = new StringFieldKey("servletContainerVersion");
		public static final StringFieldKey gitBranch = new StringFieldKey("gitBranch");
		public static final IntegerFieldKey httpsPort = new IntegerFieldKey("httpsPort");
	}

	public abstract static class BaseWebappInstanceFielder<
			PK extends BaseWebappInstanceKey<PK>,
			D extends BaseWebappInstance<PK,D>>
	extends BaseDatabeanFielder<PK,D>{

		public BaseWebappInstanceFielder(Supplier<? extends Fielder<PK>> primaryKeyFielderSupplier){
			super(primaryKeyFielderSupplier);
		}

		@Override
		public List<Field<?>> getNonKeyFields(D databean){
			return List.of(
					new StringField(FieldKeys.serverType, databean.getServerType()),
					new StringField(FieldKeys.servletContextPath, databean.getServletContextPath()),
					new StringField(FieldKeys.serverPublicIp, databean.getServerPublicIp()),
					new StringField(FieldKeys.serverPrivateIp, databean.getServerPrivateIp()),
					new InstantField(FieldKeys.refreshedLastInstant, databean.getRefreshedLastInstant()),
					new InstantField(FieldKeys.startup, databean.getStartupInstant()),
					new InstantField(FieldKeys.build, databean.getBuildInstant()),
					new StringField(FieldKeys.buildId, databean.getBuildId()),
					new StringField(FieldKeys.commitId, databean.getCommitId()),
					new StringField(FieldKeys.javaVersion, databean.getJavaVersion()),
					new StringField(FieldKeys.servletContainerVersion, databean.getServletContainerVersion()),
					new IntegerField(FieldKeys.httpsPort, databean.getHttpsPort()));
		}
	}

	public BaseWebappInstance(PK key){
		super(key);
	}

	public BaseWebappInstance(
			PK key,
			String serverType,
			String servletContextPath,
			String serverPublicIp,
			String serverPrivateIp,
			Instant refreshedLastInstant,
			Instant startup,
			Instant build,
			String buildId,
			String commitId,
			String javaVersion,
			String servletContainerVersion,
			String gitBranch,
			Integer httpsPort){
		super(key);
		this.serverType = serverType;
		this.servletContextPath = servletContextPath;
		this.serverPublicIp = serverPublicIp;
		this.serverPrivateIp = serverPrivateIp;
		this.refreshedLastInstant = refreshedLastInstant;
		this.startup = startup;
		this.build = build;
		this.buildId = buildId;
		this.commitId = commitId;
		this.javaVersion = javaVersion;
		this.servletContainerVersion = servletContainerVersion;
		this.gitBranch = gitBranch;
		this.httpsPort = httpsPort;
	}

	public BaseWebappInstance(PK key, WebappInstanceDto dto){
		this(key,
				dto.serverType,
				dto.servletContextPath,
				dto.serverPublicIp,
				dto.serverPrivateIp,
				dto.getRefreshedLast(),
				dto.getStartup(),
				dto.getBuild(),
				dto.buildId,
				dto.commitId,
				dto.javaVersion,
				dto.servletContainerVersion,
				dto.gitBranch,
				dto.httpsPort);
	}

	public WebappInstanceDto toDto(){
		return new WebappInstanceDto(
				getKey().getWebappName(),
				getKey().getServerName(),
				getServerType(),
				getServletContextPath(),
				getServerPublicIp(),
				getServerPrivateIp(),
				getRefreshedLastInstant(),
				getStartupInstant(),
				getBuildInstant(),
				getBuildId(),
				getCommitId(),
				getJavaVersion(),
				getServletContainerVersion(),
				getGitBranch(),
				getHttpsPort());
	}

	public static Set<String> getUniqueServerNames(Iterable<WebappInstance> ins){
		return Scanner.of(ins)
				.map(WebappInstance::getKey)
				.map(WebappInstanceKey::getServerName)
				.collect(HashSet::new);
	}

	public Duration getDurationSinceLastUpdatedMs(){
		long nowMs = System.currentTimeMillis();
		long refreshedLastOrNowMs = refreshedLastInstant == null ? nowMs : refreshedLastInstant.toEpochMilli();
		return Duration.ofMillis(nowMs - refreshedLastOrNowMs);
	}

	public String getServerType(){
		return serverType;
	}

	public String getServletContextPath(){
		return servletContextPath;
	}

	public String getServerPublicIp(){
		return serverPublicIp;
	}

	public String getServerPrivateIp(){
		return serverPrivateIp;
	}

	public Instant getRefreshedLastInstant(){
		return refreshedLastInstant;
	}

	public Instant getStartupInstant(){
		return startup;
	}

	public Instant getBuildInstant(){
		return build;
	}

	public String getBuildId(){
		return buildId;
	}

	public String getBuildIdOrEmpty(){
		return buildId != null ? buildId : "";
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

	public Integer getHttpsPort(){
		return httpsPort;
	}

}
