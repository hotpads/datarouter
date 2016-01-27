package com.hotpads.handler;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import com.hotpads.WebAppName;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.setting.cached.impl.Duration;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.handler.mav.Mav;
import com.hotpads.util.core.bytes.ByteUnitTool;

public class MemoryMonitoringHandler extends BaseHandler{

	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM H:mm");

	@Inject
	private Datarouter datarouter;
	@Inject
	private WebAppName webAppName;
	@Inject
	private GitProperties gitProperties;
	@Inject
	private LoadedLibraries loadedLibraries;

	private String getRedirectUrl(){
		return Mav.REDIRECT + servletContext.getContextPath() + DatarouterWebDispatcher.URL_DATAROUTER
				+ DatarouterWebDispatcher.MEMORY_STATS;
	}

	@Override
	protected Mav handleDefault(){
		Mav mav = new Mav("/jsp/admin/datarouter/memoryStats/memory.jsp");
		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		long startTime = runtimeMxBean.getStartTime();
		long uptime = runtimeMxBean.getUptime();
		mav.put("startTime", simpleDateFormat.format(new Date(startTime)));
		mav.put("upTime", new Duration(uptime, TimeUnit.MILLISECONDS).toString(TimeUnit.MINUTES));
		mav.put("serverName", datarouter.getServerName());
		mav.put("serverVersion", servletContext.getServerInfo());
		mav.put("javaVersion", System.getProperty("java.version"));
		mav.put("jvmVersion", runtimeMxBean.getVmName() + " (build " + runtimeMxBean.getVmVersion() + ")");
		mav.put("appName", webAppName);
		mav.put("gitDescribeShort", gitProperties.getDescribeShort());
		mav.put("gitBranch", gitProperties.getBranch());
		mav.put("gitCommit", gitProperties.getIdAbbrev());
		mav.put("gitCommitUserName", gitProperties.getCommitUserName());
		mav.put("gitCommitTime", simpleDateFormat.format(gitProperties.getCommitTime()));
		mav.put("buildTime", simpleDateFormat.format(gitProperties.getBuildTime()));
		mav.put("gitTags", gitProperties.getTags());
		mav.put("hotPadsLibraries", loadedLibraries.getHotpadsLibraries());
		mav.put("nonHotPadsLibraries", loadedLibraries.getNonHotpadsLibraries());

		Runtime runtime = Runtime.getRuntime();
		MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		mav.put("heap", new MemoryUsageForDisplay(heap));
		MemoryUsage nonHeapMemoryUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
		mav.put("nonHeap", new MemoryUsageForDisplay(nonHeapMemoryUsage));

		List<MemoryPoolMXBean> memoryPoolMxBeans = ManagementFactory.getMemoryPoolMXBeans();
		List<MemoryPoolForDisplay> heaps = new LinkedList<>();
		List<MemoryPoolForDisplay> nonHeaps = new LinkedList<>();
		for(MemoryPoolMXBean memoryPoolMxBean : memoryPoolMxBeans){
			switch(memoryPoolMxBean.getType()){
			case HEAP:
				heaps.add(new MemoryPoolForDisplay(memoryPoolMxBean));
				break;
			case NON_HEAP:
				nonHeaps.add(new MemoryPoolForDisplay(memoryPoolMxBean));
				break;
			default:
				break;
			}
		}
		mav.put("heaps", heaps);
		mav.put("nonHeaps", nonHeaps);

		ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
		int procNumber = runtime.availableProcessors();
		int threadCount = threadMxBean.getThreadCount();
		int daemonCount = threadMxBean.getDaemonThreadCount();

		mav.put("procNumber", procNumber);
		mav.put("threadCount", threadCount);
		mav.put("daemon", daemonCount);
		mav.put("nonDaemon", threadCount - daemonCount);
		mav.put("peak", threadMxBean.getPeakThreadCount());
		mav.put("started", threadMxBean.getTotalStartedThreadCount());

		List<GarbageCollectorMXBean> garbageCollectorMxBeans = ManagementFactory.getGarbageCollectorMXBeans();
		List<GarbageCollectorForDisplay> es = new LinkedList<>();
		for(GarbageCollectorMXBean garbageCollectorMxBean : garbageCollectorMxBeans){
			es.add(new GarbageCollectorForDisplay(garbageCollectorMxBean));
		}
		mav.put("gcs", es);

		HttpSession session = request.getSession();
		Object duration = session.getAttribute("duration");
		if(duration != null){
			mav.put("duration", duration);
			mav.put("effects", session.getAttribute("effects"));
			session.removeAttribute("duration");
			session.removeAttribute("effects");
		}
		return mav;
	}

	@Handler
	private Mav garbageCollector(){
		String serverName = params.required("serverName");
		if (!serverName.equals(datarouter.getServerName())){
			String redirectUrl = getRedirectUrl();
			redirectUrl += "?sameServer=1";
			return new Mav(redirectUrl);
		}
		List<MemoryPoolMXBean> memoryPoolMxBeans = ManagementFactory.getMemoryPoolMXBeans();
		Map<String, Long> map = new HashMap<>();
		for(MemoryPoolMXBean memoryPoolMxBean : memoryPoolMxBeans){
			map.put(memoryPoolMxBean.getName(), memoryPoolMxBean.getUsage().getUsed());
		}
		long start = System.currentTimeMillis();
		System.gc();
		long duration = System.currentTimeMillis() - start;
		HttpSession session = request.getSession();
		session.setAttribute("duration", duration);
		memoryPoolMxBeans = ManagementFactory.getMemoryPoolMXBeans();
		List<GcEffect> effects = new LinkedList<>();
		for(MemoryPoolMXBean memoryPoolMxBean : memoryPoolMxBeans){
			GcEffect gcEffect = new GcEffect(memoryPoolMxBean.getName(), map.get(memoryPoolMxBean.getName()),
					memoryPoolMxBean.getUsage().getUsed());
			effects.add(gcEffect);
		}
		session.setAttribute("effects", effects);
		return new Mav(getRedirectUrl());
	}

	public static class GarbageCollectorForDisplay{

		private String name;
		private long collectionCount;
		private Duration collectionTime;
		private String[] memoryPoolNames;

		public GarbageCollectorForDisplay(GarbageCollectorMXBean garbageCollectorMxBean){
			name = garbageCollectorMxBean.getName();
			collectionCount = garbageCollectorMxBean.getCollectionCount();
			collectionTime = new Duration(garbageCollectorMxBean.getCollectionTime(), TimeUnit.MILLISECONDS);
			memoryPoolNames = garbageCollectorMxBean.getMemoryPoolNames();
		}

		public String getName(){
			return name;
		}

		public long getCollectionCount(){
			return collectionCount;
		}

		public Duration getCollectionTime(){
			return collectionTime;
		}

		public String[] getMemoryPoolNames(){
			return memoryPoolNames;
		}

	}

	public static class MemoryUsageForDisplay{

		private String max;
		private String unallocated;
		private String allocated;
		private String used;
		private long usedBytes;
		private String free;

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

		private String name;
		private String escapedName;
		private MemoryUsageForDisplay usage;

		public MemoryPoolForDisplay(MemoryPoolMXBean memoryPoolMxBean){
			this.name = memoryPoolMxBean.getName();
			this.escapedName = name.replace(" ", "-");
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

		private String name;
		private String saved;
		private double pct;

		public GcEffect(String name, long before, long after){
			this.name = name;
			long diff = before - after;
			if(diff >= 0){
				this.saved = ByteUnitTool.byteCountToDisplaySize(diff);
			}else{
				this.saved = "-" + ByteUnitTool.byteCountToDisplaySize(-diff);
			}
			this.pct = diff / (double)before;
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
