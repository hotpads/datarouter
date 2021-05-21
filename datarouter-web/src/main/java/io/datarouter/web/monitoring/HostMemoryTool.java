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
package io.datarouter.web.monitoring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.response.Conditional;
import io.datarouter.util.DatarouterRuntimeTool;
import io.datarouter.util.RunNativeDto;
import io.datarouter.util.string.StringTool;

public class HostMemoryTool{
	private static final Logger logger = LoggerFactory.getLogger(HostMemoryTool.class);

	public static final String HOST_MEM_NAME = "Mem";
	public static final String HOST_USED_LABEL = "used";
	public static final String HOST_TOTAL_LABEL = "total";

	public static Conditional<Map<String,Map<String,Long>>> getHostMemoryStats(){
		try{
			return Conditional.success(getHostMemoryStatsInternal());
		}catch(Exception e){
			return Conditional.failure(e);
		}
	}

	private static HashMap<String,Map<String,Long>> getHostMemoryStatsInternal(){
		RunNativeDto output = DatarouterRuntimeTool.runNative("free");
		String[] lines = output.stdout.split("\n");
		List<String> headers = null;
		var result = new HashMap<String,Map<String,Long>>();
		for(String line : lines){
			List<String> words = StringTool.splitOnCharNoRegex(line, ' ', false);
			if(headers == null){
				headers = words;
				continue;
			}
			String memName = null;
			var map = new HashMap<String,Long>();
			for(int i = 0; i < words.size(); i++){
				String word = words.get(i);
				if(i == 0){
					memName = word.substring(0, word.length() - 1);
					continue;
				}
				map.put(headers.get(i - 1), Long.parseLong(word) * 1024);
			}
			result.put(memName, map);
		}
		return result;
	}

	public static Conditional<CgroupMemoryStats> getCgroupMemoryStats(){
		try{
			var stats = new CgroupMemoryStats(
					readFileToLong("/sys/fs/cgroup/memory/memory.usage_in_bytes"),
					readFileToLong("/sys/fs/cgroup/memory/memory.limit_in_bytes"));
			return Conditional.success(stats);
		}catch(Exception e){
			return Conditional.failure(e);
		}
	}

	private static long readFileToLong(String path){
		try{
			return Long.parseLong(Files.readAllLines(Paths.get(path)).get(0));
		}catch(IOException e){
			throw new RuntimeException("failure path=" + path, e);
		}
	}

	public static void main(String[] args){
		logger.warn(getHostMemoryStats().toString());
		CgroupMemoryStats cgroupMemoryStats = getCgroupMemoryStats().orElseThrow();
		logger.warn(cgroupMemoryStats.usage + " / " + cgroupMemoryStats.limit);
	}

}
