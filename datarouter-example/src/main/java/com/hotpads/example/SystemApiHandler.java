package com.hotpads.example;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.hotpads.datarouter.config.ServletContextProvider;
import com.hotpads.datarouter.setting.cached.impl.Duration;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.MemoryMonitoringHandler.MemoryUsageForDisplay;
import com.hotpads.handler.encoder.JsonEncoder;

public class SystemApiHandler extends BaseHandler{

	@Inject
	private ServletContextProvider servletContextProvider;

	@Handler(encoder=JsonEncoder.class)
	private String containerName(){
		return servletContextProvider.get().getServerInfo();
	}

	@Handler(encoder=JsonEncoder.class)
	private Object memory(Boolean human){
		if(human == null){
			return computeMemory(true);
		}
		return computeMemory(human);
	}

	@Handler(encoder=JsonEncoder.class)
	private Object memory(){
		return computeMemory(false);
	}

	private Object computeMemory(boolean human){
		MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		if(human){
			return new MemoryUsageForDisplay(heap);
		}
		return heap;
	}

	@Handler(encoder=JsonEncoder.class)
	private TimeInfos times(){
		return new TimeInfos(new Date(), ManagementFactory.getRuntimeMXBean().getUptime());
	}

	@SuppressWarnings("unused")
	private static class TimeInfos{
		private Date currentTime;
		private long upTimeMs;
		private String upTime;

		public TimeInfos(Date currentTime, long upTimeMs){
			this.currentTime = currentTime;
			this.upTimeMs = upTimeMs;
			this.upTime = new Duration(upTimeMs, TimeUnit.MILLISECONDS).toString();
		}
	}

}
