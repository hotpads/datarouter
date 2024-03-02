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
package io.datarouter.web.monitoring.memory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.response.Conditional;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.DatarouterRuntimeTool;
import io.datarouter.util.RunNativeDto;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.monitoring.memory.VmNativeMemoryStatsDto.VmNativeMemoryStatsCategory;

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
		String[] lines = output.stdout().split("\n");
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

	public static Conditional<List<VmNativeMemoryStatsDto>> getVmNativeMemoryStats(){
		String javaProcessId = String.valueOf(ProcessHandle.current().pid());
		Optional<String[]> lines = getVmMemoryStatLines(javaProcessId);
		if(lines.isEmpty()){
			return Conditional.failure(new RuntimeException("Unable to get VM Native Memory Stats"));
		}
		List<VmNativeMemoryStatsDto> stats = new ArrayList<>();
		for(String line : lines.get()){
			if(line.contains("Total: reserved")){
				VmNativeMemoryStatsDto totalStats = extractVmMemoryStats(line, true);
				stats.add(totalStats);
			}
			if(!line.startsWith("-")){
				continue;
			}
			VmNativeMemoryStatsDto stat = extractVmMemoryStats(line, false);
			if(stat != null){
				stats.add(stat);
			}
		}
		return Conditional.success(stats);
	}

	private static VmNativeMemoryStatsDto extractVmMemoryStats(String line, boolean isTotal){
		VmNativeMemoryStatsCategory category;
		String reserved = "";
		String committed = "";
		String[] parts;
		if(isTotal){
			category = VmNativeMemoryStatsCategory.TOTAL;
			parts = StringTool.getStringAfterLastOccurrence("Total:", line).replace(" ", "").split(",");
		}else{
			String trimmedLine = line.replaceFirst("-", "").trim();
			String categoryString = StringTool.getStringBeforeFirstOccurrence("(", trimmedLine).trim();
			category = VmNativeMemoryStatsCategory.fromDisplay(categoryString);
			if(category == null){
				return null;
			}
			parts = StringTool.getStringSurroundedWith(trimmedLine, "(", ")").trim().replace(" ", "").split(",");
		}
		for(String part : parts){
			if(part.contains("reserved")){
				reserved = StringTool.getStringAfterLastOccurrence("=", part).replace("KB", "");
			}else if(part.contains("committed")){
				committed = StringTool.getStringAfterLastOccurrence("=", part).replace("KB", "");
			}
		}
		long reservedLong;
		long committedLong;
		try{
			reservedLong = Long.parseLong(reserved) * 1024;
			committedLong = Long.parseLong(committed) * 1024;
		}catch(NumberFormatException e){
			throw new RuntimeException(String.format("Unable to parse either reserved=%s or committed=%s strings",
					reserved, committed));
		}
		return new VmNativeMemoryStatsDto(category, reservedLong, committedLong);
	}

	private static Optional<String[]> getVmMemoryStatLines(String javaProcessId){
		RunNativeDto output;
		try{
			output = DatarouterRuntimeTool.runNative(
					"/usr/local/datarouter/jdk-latest/bin/jcmd",
					javaProcessId,
					"VM.native_memory",
					"summary");
		}catch(RuntimeException e){
			logger.error("Unable to get VM native memory stats", e);
			return Optional.empty();
		}
		String[] lines = output.stdout().split("\n");
		boolean notEnabled = Scanner.of(lines)
				.anyMatch(line -> line.contains("Native memory tracking is not enabled"));
		if(notEnabled){
			return Optional.empty();
		}
		return Optional.of(lines);
	}

	public static Conditional<Map<String,Long>> extractCgroupMemoryStats(){
		List<String> details;
		long limit;
		Long rss = null;
		try{
			// cgroupv1
			details = Files.readAllLines(Paths.get("/sys/fs/cgroup/memory/memory.stat"));
			limit = readFileToLong("/sys/fs/cgroup/memory/memory.limit_in_bytes");
		}catch(Exception e1){
			logger.warn("", e1);
			// cgroupv2
			try{
				details = Files.readAllLines(Paths.get("/sys/fs/cgroup/memory.stat"));
				limit = readFileToLong("/sys/fs/cgroup/memory.max");
				rss = readFileToLong("/sys/fs/cgroup/memory.current");
			}catch(Exception e2){
				logger.warn("", e2);
				return Conditional.failure(e2);
			}
		}

		var res = new LinkedHashMap<String,Long>();

		res.put("limit", limit);

		Scanner.of(details)
				.map(line -> line.split(" "))
				.forEach(parts -> res.put(parts[0], Long.parseLong(parts[1])));

		// override an previous rss entry in the map
		if(rss == null){ // cgroupv1
			res.put("rss", res.get("total_rss"));
		}else{ // cgroupv2
			res.put("rss", rss);
		}
		return Conditional.success(res);
	}

	private static long readFileToLong(String path){
		try{
			return Long.parseLong(Files.readAllLines(Paths.get(path)).getFirst());
		}catch(IOException e){
			throw new RuntimeException("failure path=" + path, e);
		}
	}

	public static void main(String[] args){
		Map<String,Long> cgroupMemoryStats = extractCgroupMemoryStats().orElseThrow();
		cgroupMemoryStats.entrySet().forEach(stat -> logger.error(stat.getKey() + " - " + stat.getValue()));
	}

}
