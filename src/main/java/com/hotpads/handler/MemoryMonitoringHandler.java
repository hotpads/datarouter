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
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;

import com.hotpads.WebAppName;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.handler.dispatcher.DataRouterDispatcher;
import com.hotpads.handler.mav.Mav;
import com.hotpads.setting.cached.imp.Duration;
import com.hotpads.util.core.bytes.ByteUnitTool;

@Singleton
public class MemoryMonitoringHandler extends BaseHandler{

	@Inject
	private DataRouterContext dataRouterContext;
	@Inject 
	private WebAppName webAppName;
	@Inject
	private GitProperties gitProperties;

	private String getRedirectUrl(){
		return Mav.REDIRECT + servletContext.getContextPath() + DataRouterDispatcher.URL_DATAROUTER + DataRouterDispatcher.MEMORY_STATS;
	}

	@Override
	protected Mav handleDefault() throws Exception{
		Mav mav = new Mav("/jsp/admin/datarouter/memoryStats/memory.jsp");
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		long startTime = runtimeMXBean.getStartTime();
		long uptime = runtimeMXBean.getUptime();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM H:m");
		mav.put("startTime", simpleDateFormat.format(new Date(startTime)));
		mav.put("upTime", new Duration(uptime, TimeUnit.MILLISECONDS).toString(TimeUnit.MINUTES));
		mav.put("serverName", dataRouterContext.getServerName());
		mav.put("appName", webAppName);
		mav.put("gitBranch", gitProperties.getBranch());
		mav.put("gitCommit", gitProperties.getIdAbbrev());

		Runtime runtime = Runtime.getRuntime();
		MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		mav.put("heap", new MemoryUsageForDisplay(heap));
		MemoryUsage nonHeapMemoryUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
		mav.put("nonHeap", new MemoryUsageForDisplay(nonHeapMemoryUsage));

		List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
		List<MemoryPoolForDisplay> heaps = new LinkedList<>();
		List<MemoryPoolForDisplay> nonHeaps = new LinkedList<>();
		for(MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans){
			switch(memoryPoolMXBean.getType()){
			case HEAP:
				heaps.add(new MemoryPoolForDisplay(memoryPoolMXBean));
				break;
			case NON_HEAP:
				nonHeaps.add(new MemoryPoolForDisplay(memoryPoolMXBean));
				break;
			}
		}
		mav.put("heaps", heaps);
		mav.put("nonHeaps", nonHeaps);

		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		int procNumber = runtime.availableProcessors();
		int threadCount = threadMXBean.getThreadCount();
		int daemonCount = threadMXBean.getDaemonThreadCount();

		mav.put("procNumber", procNumber);
		mav.put("threadCount", threadCount);
		mav.put("daemon", daemonCount);
		mav.put("nonDaemon", threadCount - daemonCount);
		mav.put("peak", threadMXBean.getPeakThreadCount());
		mav.put("started", threadMXBean.getTotalStartedThreadCount());

		List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
		List<GarbageCollectorForDisplay> es = new LinkedList<>();
		for(GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans){
			es.add(new GarbageCollectorForDisplay(garbageCollectorMXBean));
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
	private Mav garbageCollector() throws Exception {
		String serverName = params.required("serverName");
		if (!serverName.equals(dataRouterContext.getServerName())) {
			String redirectUrl = getRedirectUrl();
			redirectUrl += "?sameServer=1";
			return new Mav(redirectUrl);
		}
		List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
		Map<String, Long> map = new HashMap<>();
		for(MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans){
			map.put(memoryPoolMXBean.getName(), memoryPoolMXBean.getUsage().getUsed());
		}
		long start = System.currentTimeMillis();
		System.gc();
		long duration = System.currentTimeMillis() - start;
		HttpSession session = request.getSession();
		session.setAttribute("duration", duration);
		memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
		List<GCEffect> effects = new LinkedList<>();
		for(MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans){
			GCEffect gcEffect = new GCEffect(memoryPoolMXBean.getName(), map.get(memoryPoolMXBean.getName()), memoryPoolMXBean.getUsage().getUsed());
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

		public GarbageCollectorForDisplay(GarbageCollectorMXBean garbageCollectorMXBean){
			name = garbageCollectorMXBean.getName();
			collectionCount = garbageCollectorMXBean.getCollectionCount();
			collectionTime = new Duration(garbageCollectorMXBean.getCollectionTime(), TimeUnit.MILLISECONDS);
			memoryPoolNames = garbageCollectorMXBean.getMemoryPoolNames();
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

		public MemoryPoolForDisplay(MemoryPoolMXBean memoryPoolMXBean){
			this.name = memoryPoolMXBean.getName();
			this.escapedName = name.replace(" ", "-");
			this.usage = new MemoryUsageForDisplay(memoryPoolMXBean.getUsage());
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

	public static class GCEffect{

		private String name;
		private String saved;
		private double pct;

		public GCEffect(String name, long before, long after){
			this.name = name;
			long diff = before - after;
			if(diff >= 0){
				this.saved = ByteUnitTool.byteCountToDisplaySize(diff);
			} else {
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
