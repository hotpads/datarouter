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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.util.DateTool;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.user.session.service.Session;
import io.datarouter.webappinstance.config.DatarouterWebappInstanceFiles;
import io.datarouter.webappinstance.config.DatarouterWebappInstancePaths;
import io.datarouter.webappinstance.job.WebappInstanceUpdateJob;
import io.datarouter.webappinstance.service.OneTimeLoginService;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

public class WebappInstanceHandler extends BaseHandler{

	public static final String P_USER_ID = "userId";
	public static final String P_TOKEN = "token";
	public static final String P_USE_IP = "useIp";

	@Inject
	private DatarouterWebappInstancePaths paths;
	@Inject
	private DatarouterWebappInstanceDao dao;
	@Inject
	private OneTimeLoginService oneTimeLoginService;
	@Inject
	private DatarouterWebappInstanceFiles files;

	@Handler(defaultHandler = true)
	protected Mav view(){
		Mav mav = new Mav(files.jsp.admin.datarouter.webappInstances.webappInstanceJsp);
		List<WebappInstance> webappInstances = dao.scan()
				.sorted(Comparator.comparing(webappInstance -> webappInstance.getKey().getServerName()))
				.list();
		if(webappInstances.isEmpty()){
			return mav;
		}
		Map<String,String> mostPopularCommitIdByWebapp = webappInstances.stream()
				.collect(Collectors.groupingBy(instance -> instance.getKey().getWebappName(),
						Collectors.collectingAndThen(Collectors.toList(),
								instances -> getMostCommonValue(instances, WebappInstance::getCommitId))));

		var webappStats = new UsageStatsJspDto(webappInstances, instance -> instance.getKey().getWebappName());
		var buildIdStats = new UsageStatsJspDto(webappInstances, WebappInstance::getBuildId);
		var commitIdStats = new UsageStatsJspDto(webappInstances, WebappInstance::getCommitId);
		var publicIpStats = new UsageStatsJspDto(webappInstances, WebappInstance::getServerPublicIp);
		var buildDateStats = new UsageStatsJspDto(webappInstances, WebappInstance::getBuildDate);
		var javaVersionStats = new UsageStatsJspDto(webappInstances, WebappInstance::getJavaVersion);
		var lastUpdatedStats = new UsageStatsJspDto(webappInstances, WebappInstance::getRefreshedLast);
		var servletVersionStats = new UsageStatsJspDto(webappInstances, WebappInstance::getServletContainerVersion);

		List<WebappInstanceJspDto> dtos = IterableTool.map(webappInstances, instance -> new WebappInstanceJspDto(
				instance,
				!buildIdStats.getMostCommon().equals(instance.getBuildId()),
				!mostPopularCommitIdByWebapp.get(instance.getKey().getWebappName()).equals(instance.getCommitId()),
				!javaVersionStats.getMostCommon().equals(instance.getJavaVersion()),
				!servletVersionStats.getMostCommon().equals(instance.getServletContainerVersion())));

		mav.put("webappInstances", dtos);
		mav.put("webappStats", webappStats);
		mav.put("buildIdStats", buildIdStats);
		mav.put("commitIdStats", commitIdStats);
		mav.put("publicIpStats", publicIpStats);
		mav.put("buildDateStats", buildDateStats);
		mav.put("javaVersionStats", javaVersionStats);
		mav.put("lastUpdatedStats", lastUpdatedStats);
		mav.put("servletVersionStats", servletVersionStats);

		mav.put("uri", request.getRequestURI());
		mav.put("logPath", paths.datarouter.webappInstanceLog.toSlashedString());
		return mav;
	}

	@Handler
	protected Mav instanceLogin(String webappName, String serverName){
		Session session = getSessionInfo().getRequiredSession();
		Boolean shouldUseIp = params.optionalBoolean(P_USE_IP).orElse(false);
		return oneTimeLoginService.createToken(session, webappName, serverName, shouldUseIp, request);
	}

	private <T> T getMostCommonValue(List<WebappInstance> objects, Function<WebappInstance,T> mapper){
		return objects.stream()
				.collect(Collectors.groupingBy(mapper, Collectors.counting()))
				.entrySet().stream()
				.reduce((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? entry1 : entry2)
				.map(Entry::getKey)
				.get();
	}

	public static class WebappInstanceJspDto{

		private static final int STALE_WEBAPP_INSTANCE_DAYS = 1;
		private static final int OLD_WEBAPP_INSTANCE_DAYS = 4;

		private final String webappName;
		private final String serverName;
		private final String serverType;
		private final String servletContextPath;
		private final String serverPublicIp;
		private final String serverPrivateIp;
		private final Date refreshedLast;
		private final Date startupDate;
		private final Date buildDate;
		private final String buildId;
		private final boolean highlightBuildId;
		private final String commitId;
		private final boolean highlightCommitId;
		private final String javaVersion;
		private final boolean highlightJavaVersion;
		private final String servletContainerVersion;
		private final boolean highlightServletContainerVersion;

		public WebappInstanceJspDto(WebappInstance databean, boolean highlightBuildId, boolean highlightCommitId,
				boolean highlightJavaVersion, boolean highlightServletContainerVersion){
			this(
					databean.getKey().getWebappName(),
					databean.getKey().getServerName(),
					databean.getServerType(),
					databean.getServletContextPath(),
					databean.getServerPublicIp(),
					databean.getServerPrivateIp(),
					databean.getRefreshedLast(),
					databean.getStartupDate(),
					databean.getBuildDate(),
					databean.getBuildId(),
					highlightBuildId,
					databean.getCommitId(),
					highlightCommitId,
					databean.getJavaVersion(),
					highlightJavaVersion,
					databean.getServletContainerVersion(),
					highlightServletContainerVersion);
		}

		protected WebappInstanceJspDto(String webappName, String serverName, String serverType,
				String servletContextPath, String serverPublicIp, String serverPrivateIp, Date refreshedLast,
				Date startupDate, Date buildDate, String buildId, boolean highlightBuildId, String commitId,
				boolean highlightCommitId, String javaVersion, boolean highlightJavaVersion,
				String servletContainerVersion, boolean highlightServletContainerVersion){
			this.webappName = webappName;
			this.serverName = serverName;
			this.serverType = serverType;
			this.servletContextPath = servletContextPath;
			this.serverPublicIp = serverPublicIp;
			this.serverPrivateIp = serverPrivateIp;
			this.refreshedLast = refreshedLast;
			this.startupDate = startupDate;
			this.buildDate = buildDate;
			this.buildId = buildId;
			this.highlightBuildId = highlightBuildId;
			this.commitId = commitId;
			this.highlightCommitId = highlightCommitId;
			this.javaVersion = javaVersion;
			this.highlightJavaVersion = highlightJavaVersion;
			this.servletContainerVersion = servletContainerVersion;
			this.highlightServletContainerVersion = highlightServletContainerVersion;
		}

		public String getLastUpdatedTimeAgoPrintable(){
			if(refreshedLast == null){
				return "inactive";
			}
			return DateTool.getAgoString(refreshedLast);
		}

		public String getStartupDatePrintable(){
			return DateTool.getDateTime(startupDate);
		}

		public String getBuildDatePrintable(){
			return DateTool.getDateTime(buildDate);
		}

		public String getUpTimePrintable(){
			return DateTool.getAgoString(startupDate);
		}

		public String getBuildTimeAgoPrintable(){
			return DateTool.getAgoString(buildDate);
		}

		public Duration getDurationSinceLastUpdated(){
			long nowMs = System.currentTimeMillis();
			long refreshedLastOrNowMs = refreshedLast == null ? nowMs : refreshedLast.getTime();
			return Duration.ofMillis(nowMs - refreshedLastOrNowMs);
		}

		public String getWebappName(){
			return webappName;
		}

		public String getServerName(){
			return serverName;
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

		public boolean getHighlightRefreshedLast(){
			if(refreshedLast == null){
				return false;
			}
			double secondsSinceLastRefresh = DateTool.getSecondsBetween(refreshedLast, new Date());
			return secondsSinceLastRefresh > (WebappInstanceUpdateJob.WEBAPP_INSTANCE_UPDATE_SECONDS_DELAY + 1);
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

		public boolean getHighlightBuildId(){
			return highlightBuildId;
		}

		public String getCommitId(){
			return commitId;
		}

		public boolean getHighlightCommitId(){
			return highlightCommitId;
		}

		public String getJavaVersion(){
			return javaVersion;
		}

		public boolean getHighlightJavaVersion(){
			return highlightJavaVersion;
		}

		public String getServletContainerVersion(){
			return servletContainerVersion;
		}

		public boolean getHighlightServletContainerVersion(){
			return highlightServletContainerVersion;
		}

		public boolean getStaleWebappInstance(){
			return DateTool.getDaysBetween(buildDate, new Date()) > STALE_WEBAPP_INSTANCE_DAYS;
		}

		public boolean getOldWebappInstance(){
			return DateTool.getDaysBetween(buildDate, new Date()) > OLD_WEBAPP_INSTANCE_DAYS;
		}

	}

	public static class UsageStatsJspDto{

		private final List<UsageEntry> usage;
		private final int uniqueCount;
		private final boolean allCommon;
		private final Object mostCommon;

		public <T> UsageStatsJspDto(List<T> instances, Function<T,?> mapper){
			Map<?,Long> frequency = instances.stream()
					.map(mapper)
					.filter(Objects::nonNull)
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			uniqueCount = frequency.size();
			allCommon = uniqueCount <= 1;
			mostCommon = frequency.entrySet().stream()
					.reduce((one, two) -> one.getValue() > two.getValue() ? one : two)
					.map(Entry::getKey)
					.orElse(null);
			usage = frequency.entrySet().stream()
					.sorted(Entry.comparingByValue(Comparator.reverseOrder()))
					.map(entry -> new UsageEntry(entry.getKey(), entry.getValue() / (float)instances.size() * 100))
					.collect(Collectors.toList());
		}

		public int getUniqueCount(){
			return uniqueCount;
		}

		public boolean getAllCommon(){
			return allCommon;
		}

		public Object getMostCommon(){
			return mostCommon;
		}

		public String getUsageBreakdownHtml(){
			ContainerTag ul = TagCreator.ul();
			for(UsageEntry entry : usage){
				ul.with(TagCreator.li(
						TagCreator.strong(entry.getUsagePercentagePrintable() + '%'),
						TagCreator.text(" - " + entry.key)));
			}
			return ul.render();
		}

	}

	public static class UsageEntry{

		private final Object key;
		private final float usagePercentage;

		public UsageEntry(Object key, float usagePercentage){
			this.key = key;
			this.usagePercentage = usagePercentage;
		}

		public Object getKey(){
			return key;
		}

		public String getUsagePercentagePrintable(){
			return String.format("%.1f", usagePercentage);
		}

	}

}
