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
package io.datarouter.web.monitoring;

import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.util.MxBeans;
import io.datarouter.util.SystemTool;
import io.datarouter.util.bytes.ByteUnitTool;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.app.WebappName;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.dispatcher.NonEagerInitHandler;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.monitoring.OutgoingIpFinderService.OutGoingIpWrapper;
import io.datarouter.web.monitoring.memory.CgroupMemoryStatsDto;
import io.datarouter.web.monitoring.memory.HostMemoryTool;
import io.datarouter.web.monitoring.memory.VmNativeMemoryStatsDto;
import j2html.attributes.Attr;

/**
 * This class needs to access a folder containing a library which doesn't exist during unit tests
 */
public class MemoryMonitoringHandler extends BaseHandler implements NonEagerInitHandler{

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter
			.ofPattern("d MMM H:mm z")
			.withZone(ZoneId.systemDefault());

	@Inject
	private WebappName webappName;
	@Inject
	private GitProperties gitProperties;
	@Inject
	private BuildProperties buildProperties;
	@Inject
	private LoadedLibraries loadedLibraries;
	@Inject
	private DatarouterWebFiles files;
	@Inject
	private TomcatThreadMetrics tomcatThreadMetrics;
	@Inject
	private ManifestDetails manifestDetails;
	@Inject
	private OutgoingIpFinderService ipService;
	@Inject
	private ServerName serverName;

	@Handler
	public Mav view(){
		Mav mav = new Mav(files.jsp.admin.datarouter.memoryStats.memoryJsp);
		long startTime = MxBeans.RUNTIME.getStartTime();
		long uptime = MxBeans.RUNTIME.getUptime();
		Map<String,GitPropertiesJspDto> gitDetailedLibraries = Scanner.of(loadedLibraries.gitDetailedLibraries
				.entrySet())
				.toMapSupplied(Entry::getKey, entry -> new GitPropertiesJspDto(entry.getValue()), LinkedHashMap::new);
		OutGoingIpWrapper ipAddress = ipService.getIpForServer();
		mav.put("ipSource", ipAddress.ipSource);
		mav.put("ipAddr", ipAddress.ipAddress);
		mav.put("startTime", FORMATTER.format(Instant.ofEpochMilli(startTime)));
		mav.put("upTime", new DatarouterDuration(uptime, TimeUnit.MILLISECONDS).toString(TimeUnit.MINUTES));
		mav.put("serverName", serverName.get());
		mav.put("serverVersion", servletContext.getServerInfo());
		mav.put("javaVersion", SystemTool.getJavaVersion());
		mav.put("jvmVersion", MxBeans.RUNTIME.getVmName() + " (build " + MxBeans.RUNTIME.getVmVersion() + ")");
		mav.put("appName", webappName);
		mav.put("gitDescribeShort", gitProperties.getDescribeShort().orElse(GitProperties.UNKNOWN_STRING));
		mav.put("gitBranch", gitProperties.getBranch().orElse(GitProperties.UNKNOWN_STRING));
		mav.put("gitCommit", gitProperties.getIdAbbrev().orElse(GitProperties.UNKNOWN_STRING));
		mav.put("gitCommitUserName", gitProperties.getCommitUserName().orElse(GitProperties.UNKNOWN_STRING));
		mav.put("gitCommitTime", FORMATTER.format(gitProperties.getCommitTime().orElse(GitProperties.UNKNOWN_DATE)));
		mav.put("buildTime", FORMATTER.format(gitProperties.getBuildTime().orElse(GitProperties.UNKNOWN_DATE)));
		mav.put("buildId", buildProperties.getBuildId());
		mav.put("buildJdk", manifestDetails.getBuildPair());
		mav.put("manifest", manifestDetails.getManifestString());
		mav.put("detailedLibraries", gitDetailedLibraries);
		mav.put("buildDetailedLibraries", loadedLibraries.buildDetailedLibraries);
		mav.put("manifests", loadedLibraries.manifests);
		mav.put("otherLibraries", loadedLibraries.otherLibraries);
		mav.put("tomcatThreadMetrics", tomcatThreadMetrics.getTomcatPoolMetrics());

		MemoryUsage heap = MxBeans.MEMORY.getHeapMemoryUsage();
		mav.put("heap", new MemoryUsageForDisplay(heap));
		MemoryUsage nonHeapMemoryUsage = MxBeans.MEMORY.getNonHeapMemoryUsage();
		mav.put("nonHeap", new MemoryUsageForDisplay(nonHeapMemoryUsage));

		HostMemoryTool.getHostMemoryStats().ifSuccess(hostMemoryStats -> {
			mav.put("hostMemoryUsed", ByteUnitTool.byteCountToDisplaySize(hostMemoryStats.get(
					HostMemoryTool.HOST_MEM_NAME).get(HostMemoryTool.HOST_USED_LABEL)));
			mav.put("hostMemoryTotal", ByteUnitTool.byteCountToDisplaySize(hostMemoryStats.get(
					HostMemoryTool.HOST_MEM_NAME).get(HostMemoryTool.HOST_TOTAL_LABEL)));
		});

		mav.put("vmNativeMemoryStats", buildNativeMemoryStatsTable());
		mav.put("cgroupMemoryStats", buildCgroupMemoryStatsTable());

		List<MemoryPoolForDisplay> heaps = new LinkedList<>();
		List<MemoryPoolForDisplay> nonHeaps = new LinkedList<>();
		for(MemoryPoolMXBean memoryPoolMxBean : MxBeans.MEMEORY_POOLS){
			switch(memoryPoolMxBean.getType()){
			case HEAP:
				heaps.add(new MemoryPoolForDisplay(memoryPoolMxBean));
				break;
			case NON_HEAP:
				nonHeaps.add(new MemoryPoolForDisplay(memoryPoolMxBean));
				break;
			}
		}
		mav.put("heaps", heaps);
		mav.put("nonHeaps", nonHeaps);

		Runtime runtime = Runtime.getRuntime();
		int procNumber = runtime.availableProcessors();
		mav.put("procNumber", procNumber);

		int threadCount = MxBeans.THREAD.getThreadCount();
		mav.put("threadCount", threadCount);
		int daemonCount = MxBeans.THREAD.getDaemonThreadCount();
		mav.put("daemon", daemonCount);
		mav.put("nonDaemon", threadCount - daemonCount);
		mav.put("peak", MxBeans.THREAD.getPeakThreadCount());
		mav.put("started", MxBeans.THREAD.getTotalStartedThreadCount());

		List<GarbageCollectorForDisplay> gcs = new LinkedList<>();
		for(GarbageCollectorMXBean garbageCollectorMxBean : MxBeans.GCS){
			gcs.add(new GarbageCollectorForDisplay(garbageCollectorMxBean));
		}
		mav.put("gcs", gcs);

		return mav;
	}

	private String buildNativeMemoryStatsTable(){
		List<VmNativeMemoryStatsDto> memoryStats = HostMemoryTool.getVmNativeMemoryStats()
				.orElseGet($ -> new ArrayList<>());
		if(memoryStats.isEmpty()){
			return div().attr(Attr.STYLE, "display: none;").render();
		}
		var tbody = tbody()
				.with(thead(tr(th("Category"), th("Reserved Memory"), th("Committed Memory"))));
		Scanner.of(memoryStats)
				.map(stat -> tr(
						td(stat.category.getDisplay()),
						td(ByteUnitTool.byteCountToDisplaySize(stat.reservedMemoryBytes)).attr(Attr.ALIGN, "right"),
						td(ByteUnitTool.byteCountToDisplaySize(stat.committedMemoryBytes)).attr(Attr.ALIGN, "right")))
				.forEach(tbody::with);
		var table = table(tbody)
				.withClass("table table-striped table-bordered table-sm");
		return div(h2("VM Native Memory Stats"), table)
				.withClass("col-12 col-sm-6")
				.render();
	}

	private String buildCgroupMemoryStatsTable(){
		List<CgroupMemoryStatsDto> memoryStats = HostMemoryTool.extractCgroupMemoryStats()
				.orElseGet($ -> new ArrayList<>());
		if(memoryStats.isEmpty()){
			return div().attr(Attr.STYLE, "display: none;").render();
		}
		var tbody = tbody()
				.with(thead(tr(th("Category"), th("Memory value"))));
		Scanner.of(memoryStats)
				.map(stat -> tr(
						td(stat.category.getDisplay()),
						td(ByteUnitTool.byteCountToDisplaySize(stat.memoryBytes)).attr(Attr.ALIGN, "right")))
				.forEach(tbody::with);
		var table = table(tbody)
				.withClass("table table-striped table-bordered table-sm");
		return div(h2("Cgroup Memory Stats"), table)
				.withClass("col-12 col-sm-6")
				.render();
	}

	@Handler
	public GarbageCollectingResult garbageCollector(){
		String paramsServerName = params.required("serverName");
		if(!paramsServerName.equals(serverName.get())){
			return new GarbageCollectingResult(false, null, null);
		}
		Map<String,Long> map = new HashMap<>();
		for(MemoryPoolMXBean memoryPoolMxBean : MxBeans.MEMEORY_POOLS){
			map.put(memoryPoolMxBean.getName(), memoryPoolMxBean.getUsage().getUsed());
		}
		long start = System.currentTimeMillis();
		System.gc();
		long duration = System.currentTimeMillis() - start;
		List<GcEffect> effects = new LinkedList<>();
		for(MemoryPoolMXBean memoryPoolMxBean : MxBeans.MEMEORY_POOLS){
			GcEffect gcEffect = new GcEffect(memoryPoolMxBean.getName(), map.get(memoryPoolMxBean.getName()),
					memoryPoolMxBean.getUsage().getUsed());
			effects.add(gcEffect);
		}
		return new GarbageCollectingResult(true, duration, effects);
	}

	@SuppressWarnings("unused")
	private static class GarbageCollectingResult{

		private final boolean success;
		private final Long duration;
		private final List<GcEffect> effects;

		private GarbageCollectingResult(boolean success, Long duration, List<GcEffect> effects){
			this.success = success;
			this.duration = duration;
			this.effects = effects;
		}

	}

	public static class GarbageCollectorForDisplay{

		private final String name;
		private final long collectionCount;
		private final DatarouterDuration collectionTime;
		private final String[] memoryPoolNames;

		public GarbageCollectorForDisplay(GarbageCollectorMXBean garbageCollectorMxBean){
			name = garbageCollectorMxBean.getName();
			collectionCount = garbageCollectorMxBean.getCollectionCount();
			collectionTime = new DatarouterDuration(garbageCollectorMxBean.getCollectionTime(), TimeUnit.MILLISECONDS);
			memoryPoolNames = garbageCollectorMxBean.getMemoryPoolNames();
		}

		public String getName(){
			return name;
		}

		public long getCollectionCount(){
			return collectionCount;
		}

		public DatarouterDuration getCollectionTime(){
			return collectionTime;
		}

		public String[] getMemoryPoolNames(){
			return memoryPoolNames;
		}

	}

	public static class MemoryUsageForDisplay{

		private final String max;
		private final String unallocated;
		private final String allocated;
		private final String used;
		private final long usedBytes;
		private final String free;

		public MemoryUsageForDisplay(MemoryUsage memoryUsage){
			long allocatedBytes = memoryUsage.getCommitted();
			this.usedBytes = memoryUsage.getUsed();
			long maxBytes = memoryUsage.getMax();
			long freeBytes = allocatedBytes - usedBytes;
			long unallocatedBytes = maxBytes - allocatedBytes;
			this.max = ByteUnitTool.byteCountToDisplaySize(maxBytes);
			this.unallocated = ByteUnitTool.byteCountToDisplaySize(unallocatedBytes);
			this.allocated = ByteUnitTool.byteCountToDisplaySize(allocatedBytes);
			this.used = ByteUnitTool.byteCountToDisplaySize(usedBytes);
			this.free = ByteUnitTool.byteCountToDisplaySize(freeBytes);
		}

		public String getMax(){
			return max;
		}

		public String getUnallocated(){
			return unallocated;
		}

		public String getAllocated(){
			return allocated;
		}

		public String getUsed(){
			return used;
		}

		public long getUsedBytes(){
			return usedBytes;
		}

		public String getFree(){
			return free;
		}

	}

	public static class MemoryPoolForDisplay{

		private final String name;
		private final String escapedName;
		private final MemoryUsageForDisplay usage;

		public MemoryPoolForDisplay(MemoryPoolMXBean memoryPoolMxBean){
			this.name = memoryPoolMxBean.getName();
			this.escapedName = name.replace(" ", "-").replace("'", "").replace("\"", "");
			this.usage = new MemoryUsageForDisplay(memoryPoolMxBean.getUsage());
		}

		public String getName(){
			return name;
		}

		public String getEscapedName(){
			return escapedName;
		}

		public MemoryUsageForDisplay getUsage(){
			return usage;
		}

	}

	public static class GcEffect{

		private final String name;
		private String saved;
		private Double pct;

		public GcEffect(String name, long before, long after){
			this.name = name;
			long diff = before - after;
			if(diff >= 0){
				this.saved = ByteUnitTool.byteCountToDisplaySize(diff);
			}else{
				this.saved = "-" + ByteUnitTool.byteCountToDisplaySize(-diff);
			}
			if(before > 0){
				this.pct = diff / (double)before;
			}
		}

		public String getName(){
			return name;
		}

		public String getSaved(){
			return saved;
		}

		public double getPct(){
			return pct;
		}

	}
}
