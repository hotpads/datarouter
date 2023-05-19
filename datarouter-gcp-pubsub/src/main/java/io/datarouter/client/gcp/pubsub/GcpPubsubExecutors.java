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
package io.datarouter.client.gcp.pubsub;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.inject.Singleton;

import io.datarouter.util.concurrent.DatarouterExecutorService;
import io.datarouter.util.concurrent.ExecutorTool;
import io.datarouter.util.concurrent.NamedThreadFactory;
import io.datarouter.util.concurrent.ScalingThreadPoolExecutor;

public class GcpPubsubExecutors{

	@Singleton
	public static class GcpPubsubSubscriberStubExecutor extends ScheduledThreadPoolExecutor{
		public GcpPubsubSubscriberStubExecutor(){
			super(2, new NamedThreadFactory("gcpPubsubSubscriberStubExecutor", true));
		}
	}

	@Singleton
	public static class GcpPubsubTransportChannelExecutor extends ScalingThreadPoolExecutor{
		public GcpPubsubTransportChannelExecutor(){
			super("gcpPubsubTransportChannelExecutor", 1);
		}
	}

	@Singleton
	public static class GcpPubsubManagedChannelExecutor extends ScalingThreadPoolExecutor{
		public GcpPubsubManagedChannelExecutor(){
			super("gcpPubsubManagedChannelExecutor", 128);
		}
	}

	@Singleton
	public static class GcpPubsubManagedChannelOffloadExecutor extends ScalingThreadPoolExecutor{
		public GcpPubsubManagedChannelOffloadExecutor(){
			super("gcpPubsubManagedChannelOffloadExecutor", 32);
		}
	}

	@Singleton
	public static class GcpPubsubEventLoopGroupExecutor extends DatarouterExecutorService{
		public GcpPubsubEventLoopGroupExecutor(){
			super(ExecutorTool.createCached("gcpPubsubEventLoopGroupExecutor"));
		}
	}

	@Singleton
	public static class GcpPubsubWatchdogExecutor extends ScheduledThreadPoolExecutor{
		public GcpPubsubWatchdogExecutor(){
			super(16, new NamedThreadFactory("gcpPubsubWatchdogExecutor", true));
		}
	}

	@Singleton
	public static class GcpPubsubPublisherExecutor extends ScheduledThreadPoolExecutor{
		public GcpPubsubPublisherExecutor(){
			super(16, new NamedThreadFactory("gcpPubsubPublisherExecutor", true));
		}
	}

	@Singleton
	public static class GcpPubsubQueueLengthMonitoringJobExecutor extends ScalingThreadPoolExecutor{
		public GcpPubsubQueueLengthMonitoringJobExecutor(){
			super("gcpPubsubQueueLengthMonitoringJobExecutor", 7);
		}
	}

}
