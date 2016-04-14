package com.hotpads.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.encoder.JsonEncoder;
import com.hotpads.handler.mav.Mav;
import com.hotpads.util.core.concurrent.NamedThreadFactory;

public class ExecutorsMonitoringHandler extends BaseHandler{

	@Inject
	private DatarouterInjector injector;

	@Override
	protected Mav handleDefault(){
		Mav mav = new Mav("/jsp/admin/datarouter/executorsMonitoring/executors.jsp");
		mav.put("executors", getExecutors());
		return mav;
	}

	@Handler(encoder = JsonEncoder.class)
	public Collection<TextExecutor> getExecutors(){
		Collection<ExecutorService> executors = injector.getInstancesOfType(ExecutorService.class);
		List<TextExecutor> textExecutors = new ArrayList<>();
		for(ExecutorService executor : executors){
			if(executor instanceof ThreadPoolExecutor){
				textExecutors.add(new TextExecutor((ThreadPoolExecutor)executor));
			}
		}
		return textExecutors;
	}

	public static class TextExecutor{

		private int poolSize;
		private int activeCount;
		private int maxPoolSize;
		private int queueSize;
		private String name;
		private int remainingQueueCapacity;
		private long completedTaskCount;

		public TextExecutor(ThreadPoolExecutor executor){
			this.poolSize = executor.getPoolSize();
			this.activeCount = executor.getActiveCount();
			this.maxPoolSize = executor.getMaximumPoolSize();
			this.queueSize = executor.getQueue().size();
			this.remainingQueueCapacity = executor.getQueue().remainingCapacity();
			this.completedTaskCount = executor.getCompletedTaskCount();
			if(executor.getThreadFactory() instanceof NamedThreadFactory){
				this.name = ((NamedThreadFactory)executor.getThreadFactory()).getGroupName();
			}
		}

		public int getPoolSize(){
			return poolSize;
		}

		public int getActiveCount(){
			return activeCount;
		}

		public int getMaxPoolSize(){
			return maxPoolSize;
		}

		public int getQueueSize(){
			return queueSize;
		}

		public String getName(){
			return name;
		}

		public int getRemainingQueueCapacity(){
			return remainingQueueCapacity;
		}

		public long getCompletedTaskCount(){
			return completedTaskCount;
		}

	}

}
