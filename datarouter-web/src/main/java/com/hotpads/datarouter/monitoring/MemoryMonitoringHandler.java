package com.hotpads.datarouter.monitoring;

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

import com.hotpads.datarouter.app.WebAppName;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.encoder.JsonEncoder;
import com.hotpads.handler.mav.Mav;
import com.hotpads.util.core.Duration;
import com.hotpads.util.core.bytes.ByteUnitTool;

public class MemoryMonitoringHandler extends BaseHandler{

	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM H:mm");

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private WebAppName webAppName;
	@Inject
	private GitProperties gitProperties;
	@Inject
	private LoadedLibraries loadedLibraries;

	@Override
	protected Mav handleDefault(){
		Mav mav = new Mav("/jsp/admin/datarouter/memoryStats/memory.jsp");
		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		long startTime = runtimeMxBean.getStartTime();
		long uptime = runtimeMxBean.getUptime();
		mav.put("startTime", simpleDateFormat.format(new Date(startTime)));
		mav.put("upTime", new Duration(uptime, TimeUnit.MILLISECONDS).toString(TimeUnit.MINUTES));
		mav.put("serverName", datarouterProperties.getServerName());
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
		mav.put("detailedLibraries", loadedLibraries.getDetailedLibraries());
		mav.put("otherLibraries", loadedLibraries.getOtherLibraries());

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

		return mav;
	}

	@Handler(encoder = JsonEncoder.class)
	private GarbabeCollectingResult garbageCollector(){
		String serverName = params.required("serverName");
		if(!serverName.equals(datarouterProperties.getServerName())){
			return new GarbabeCollectingResult(false, null, null);
		}
		List<MemoryPoolMXBean> memoryPoolMxBeans = ManagementFactory.getMemoryPoolMXBeans();
		Map<String,Long> map = new HashMap<>();
		for(MemoryPoolMXBean memoryPoolMxBean : memoryPoolMxBeans){
			map.put(memoryPoolMxBean.getName(), memoryPoolMxBean.getUsage().getUsed());
		}
		long start = System.currentTimeMillis();
		System.gc();
		long duration = System.currentTimeMillis() - start;
		List<GcEffect> effects = new LinkedList<>();
		for(MemoryPoolMXBean memoryPoolMxBean : ManagementFactory.getMemoryPoolMXBeans()){
			GcEffect gcEffect = new GcEffect(memoryPoolMxBean.getName(), map.get(memoryPoolMxBean.getName()),
					memoryPoolMxBean.getUsage().getUsed());
			effects.add(gcEffect);
		}
		return new GarbabeCollectingResult(true, duration, effects);
	}

	@SuppressWarnings("unused")
	private static class GarbabeCollectingResult{
		private final boolean success;
		private final Long duration;
		private final List<GcEffect> effects;

		private GarbabeCollectingResult(boolean success, Long duration, List<GcEffect> effects){
			this.success = success;
			this.duration = duration;
			this.effects = effects;
		}
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
