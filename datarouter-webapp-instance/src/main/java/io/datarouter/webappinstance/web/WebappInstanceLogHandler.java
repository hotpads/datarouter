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
package io.datarouter.webappinstance.web;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.util.DateTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.user.session.CurrentUserSessionInfoService;
import io.datarouter.webappinstance.config.DatarouterWebappInstanceFiles;
import io.datarouter.webappinstance.storage.webappinstancelog.DatarouterWebappInstanceLogDao;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLog;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLogKey;

public class WebappInstanceLogHandler extends BaseHandler{

	@Inject
	private DatarouterWebappInstanceLogDao dao;
	@Inject
	private DatarouterWebappInstanceFiles files;
	@Inject
	private CurrentUserSessionInfoService currentUserSessionInfoService;

	@Handler(defaultHandler = true)
	public Mav webappInstanceLog(String webappName, String serverName){
		Mav mav = new Mav(files.jsp.admin.datarouter.webappInstances.webappInstanceLogJsp);
		WebappInstanceLogKey prefix = new WebappInstanceLogKey(webappName, serverName, null, null);
		List<WebappInstanceLog> logs = dao.scanWithPrefix(prefix)
				.sort(Comparator.comparing(log -> log.getKey().getStartup(), Collections.reverseOrder()))
				.list();
		int logCount = logs.size();
		ZoneId zoneId = currentUserSessionInfoService.getZoneId(request);
		List<WebappInstanceLogJspDto> logJspDtos = new ArrayList<>(logCount);
		for(int i = 0; i < logCount; ++i){
			Instant fallbackRefreshedLast = i == 0 ? Instant.now() : logs.get(i - 1).getKey().getStartup();
			logJspDtos.add(new WebappInstanceLogJspDto(logs.get(i), fallbackRefreshedLast, zoneId));
		}
		mav.put("logs", logJspDtos);
		return mav;
	}

	public static class WebappInstanceLogJspDto{

		private static final int LATEST_WEBAPP_INSTANCES = 1;

		private final String webappName;
		private final String serverName;
		private final Instant startup;
		private final Instant refreshedLast;
		private final Instant build;
		private final String buildId;
		private final String commitId;
		private final String javaVersion;
		private final String servletContainerVersion;

		private final ZoneId zoneId;

		public WebappInstanceLogJspDto(WebappInstanceLog log, Instant fallbackRefreshedLast, ZoneId zoneId){
			this(
					log.getKey().getWebappName(),
					log.getKey().getServerName(),
					log.getKey().getStartup(),
					log.getKey().getBuild(),
					log.getRefreshedLast() == null ? fallbackRefreshedLast : log.getRefreshedLast(),
					log.getBuildId(),
					log.getCommitId(),
					log.getJavaVersion(),
					log.getServletContainerVersion(),
					zoneId);
		}

		protected WebappInstanceLogJspDto(
				String webappName,
				String serverName,
				Instant startupDate,
				Instant buildDate,
				Instant refreshedLast,
				String buildId,
				String commitId,
				String javaVersion,
				String servletContainerVersion,
				ZoneId zoneId){
			this.webappName = webappName;
			this.serverName = serverName;
			this.startup = startupDate;
			this.build = buildDate;
			this.refreshedLast = refreshedLast;
			this.buildId = buildId;
			this.commitId = commitId;
			this.javaVersion = javaVersion;
			this.servletContainerVersion = servletContainerVersion;

			this.zoneId = zoneId;
		}

		public String getWebappName(){
			return webappName;
		}

		public String getServerName(){
			return serverName;
		}

		public long getStartupMs(){
			return startup.toEpochMilli();
		}

		public long getBuildMs(){
			return build.toEpochMilli();
		}

		public Date getStartupDate(){
			return new Date(startup.toEpochMilli());
		}

		public Date getBuildDate(){
			return new Date(build.toEpochMilli());
		}

		public String getStartupString(){
			return DateTool.formatInstantWithZone(startup, zoneId);
		}

		public Instant getRefreshedLast(){
			return refreshedLast;
		}

		public long getRefreshedLastMs(){
			return refreshedLast.toEpochMilli();
		}

		public String getStartupDatePrintable(){
			return DateTool.getAgoString(startup);
		}

		public String getBuildDatePrintable(){
			return DateTool.getAgoString(build);
		}

		public String getBuildId(){
			return buildId;
		}

		public String getCommitId(){
			return commitId;
		}

		public boolean getLatest(){
			Duration duration = Duration.between(build, Instant.now());
			return duration.toDays() < LATEST_WEBAPP_INSTANCES;
		}

		public String getJavaVersion(){
			return javaVersion;
		}

		public String getServletContainerVersion(){
			return servletContainerVersion;
		}

	}

}
