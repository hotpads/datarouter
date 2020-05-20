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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.util.DateTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.webappinstance.config.DatarouterWebappInstanceFiles;
import io.datarouter.webappinstance.storage.webappinstancelog.DatarouterWebappInstanceLogDao;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLog;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLogKey;

public class WebappInstanceLogHandler extends BaseHandler{

	@Inject
	private DatarouterWebappInstanceLogDao dao;
	@Inject
	private DatarouterWebappInstanceFiles files;

	@Handler(defaultHandler = true)
	public Mav viewLog(String webappName, String serverName){
		Mav mav = new Mav(files.jsp.admin.datarouter.webappInstances.webappInstanceLogJsp);
		WebappInstanceLogKey prefix = new WebappInstanceLogKey(webappName, serverName, null, null);
		List<WebappInstanceLog> logs = dao.scanWithPrefix(prefix)
				.sorted(Comparator.comparing(log -> log.getKey().getStartupDate(), Collections.reverseOrder()))
				.list();
		int logCount = logs.size();
		List<WebappInstanceLogJspDto> logJspDtos = new ArrayList<>(logCount);
		for(int i = 0; i < logCount; ++i){
			Date fallbackRefreshedLast = i == 0 ? new Date() : logs.get(i - 1).getKey().getStartupDate();
			logJspDtos.add(new WebappInstanceLogJspDto(logs.get(i), fallbackRefreshedLast));
		}
		mav.put("logs", logJspDtos);
		return mav;
	}

	public static class WebappInstanceLogJspDto{

		private static final int LATEST_WEBAPP_INSTANCES = 1;

		private final String webappName;
		private final String serverName;
		private final Date startupDate;
		private final Date refreshedLast;
		private final Date buildDate;
		private final String buildId;
		private final String commitId;
		private final String javaVersion;
		private final String servletContainerVersion;

		public WebappInstanceLogJspDto(WebappInstanceLog log, Date fallbackRefreshedLast){
			this(
					log.getKey().getWebappName(),
					log.getKey().getServerName(),
					log.getKey().getStartupDate(),
					log.getKey().getBuildDate(),
					log.getRefreshedLast() == null ? fallbackRefreshedLast : log.getRefreshedLast(),
					log.getBuildId(),
					log.getCommitId(),
					log.getJavaVersion(),
					log.getServletContainerVersion());
		}

		protected WebappInstanceLogJspDto(String webappName, String serverName, Date startupDate, Date buildDate,
				Date refreshedLast, String buildId, String commitId, String javaVersion,
				String servletContainerVersion){
			this.webappName = webappName;
			this.serverName = serverName;
			this.startupDate = startupDate;
			this.buildDate = buildDate;
			this.refreshedLast = refreshedLast;
			this.buildId = buildId;
			this.commitId = commitId;
			this.javaVersion = javaVersion;
			this.servletContainerVersion = servletContainerVersion;
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

		public Date getRefreshedLast(){
			return refreshedLast;
		}

		public String getStartupDatePrintable(){
			return DateTool.getAgoString(startupDate.toInstant());
		}

		public String getBuildDatePrintable(){
			return DateTool.getAgoString(buildDate.toInstant());
		}

		public String getBuildId(){
			return buildId;
		}

		public String getCommitId(){
			return commitId;
		}

		public boolean getLatest(){
			return DateTool.getDaysBetween(buildDate, new Date()) < LATEST_WEBAPP_INSTANCES;
		}

		public String getJavaVersion(){
			return javaVersion;
		}

		public String getServletContainerVersion(){
			return servletContainerVersion;
		}

	}

}
