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
package io.datarouter.util;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.management.OperatingSystemMXBean;
import com.sun.management.ThreadMXBean;

public class PlatformMxBeans{
	private static final Logger logger = LoggerFactory.getLogger(PlatformMxBeans.class);

	/*
	 * trigger the registration of internal mbeans. see ManagementFactoryHelper.registerInternalMBeans
	 */
	static{
		try{
			JmxTool.createMBean("sun.management.HotspotInternal", null);
		}catch(RuntimeException e){
			Throwable cause = e.getCause();
			if(cause != null && cause instanceof InstanceAlreadyExistsException){
				logger.warn("sun.management.HotspotInternal already registered");
			}else{
				throw e;
			}
		}
	}

	public static final RuntimeMXBean RUNTIME = ManagementFactory.getRuntimeMXBean();
	public static final MemoryMXBean MEMORY = ManagementFactory.getMemoryMXBean();
	public static final CompilationMXBean COMPILATION = ManagementFactory.getCompilationMXBean();

	public static final List<MemoryPoolMXBean> MEMEORY_POOLS = ManagementFactory.getMemoryPoolMXBeans();
	public static final List<GarbageCollectorMXBean> GCS = ManagementFactory.getGarbageCollectorMXBeans();

	public static final ThreadMXBean THREAD = ManagementFactory.getPlatformMXBean(ThreadMXBean.class);
	public static final OperatingSystemMXBean OS = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

	public static final List<BufferPoolMXBean> BUFFER_POOLS = ManagementFactory
			.getPlatformMXBeans(BufferPoolMXBean.class);

	@SuppressWarnings("unchecked")
	public static Map<String,Long> getInternalThreadCpuNs(){
		var hotspotThreading = JmxTool.newObjectName("sun.management:type=HotspotThreading");
		return (Map<String,Long>)JmxTool.getAttribute(hotspotThreading, "InternalThreadCpuTimes");
	}

}
