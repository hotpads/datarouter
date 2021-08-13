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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.response.Conditional;
import io.datarouter.scanner.Scanner;
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

	public static Conditional<List<VmNativeMemoryStatsDto>> getVmNativeMemoryStats(){
		Optional<String> javaProcessId = getJavaProcessId();
		if(javaProcessId.isEmpty()){
			logger.error("Unable to get java pid");
			return Conditional.failure(new RuntimeException("Unable to get java pid"));
		}
		RunNativeDto output = DatarouterRuntimeTool.runNative(
				"jcmd",
				javaProcessId.get(),
				"VM.native_memory",
				"summary");
		List<VmNativeMemoryStatsDto> stats = new ArrayList<>();
		String[] lines = output.stdout.split("\n");
		for(String line : lines){
			if(line.contains("Total: reserved")){
				VmNativeMemoryStatsDto totalStats = extractMemoryStats(line, true);
				stats.add(totalStats);
			}
			if(!line.startsWith("-")){
				continue;
			}
			VmNativeMemoryStatsDto stat = extractMemoryStats(line, false);
			stats.add(stat);
		}
		return Conditional.success(stats);
	}

	private static VmNativeMemoryStatsDto extractMemoryStats(String line, boolean isTotal){
		String category;
		String reserved = "";
		String committed = "";
		String[] parts;
		if(isTotal){
			category = VmNativeMemoryStatsDto.TOTAL_STAT_CATEGORY;
			parts = StringTool.getStringAfterLastOccurrence("Total:", line).replace(" ", "").split(",");
		}else{
			String trimmedLine = line.replaceFirst("-", "").trim();
			category = StringTool.getStringBeforeFirstOccurrence("(", trimmedLine).trim();
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

	private static Optional<String> getJavaProcessId(){
		String[] lines = DatarouterRuntimeTool.runNative("jps").stdout.split("\n");
		return Stream.of(lines)
				.filter(line -> line.contains("Bootstrap"))
				.map(line -> StringTool.getStringBeforeFirstOccurrence(" ", line))
				.findFirst();
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

	public static Conditional<List<CgroupMemoryStatsDto>> getCgroupMemoryStatsExtended(){
		List<String> lines;
		long totalUsage;
		long totalLimit;
		long kernelMemUsage;
		try{
			lines = Files.readAllLines(Paths.get("/sys/fs/cgroup/memory/memory.stat"));
			totalUsage = readFileToLong("/sys/fs/cgroup/memory/memory.usage_in_bytes");
			totalLimit = readFileToLong("/sys/fs/cgroup/memory/memory.limit_in_bytes");
			kernelMemUsage = readFileToLong("/sys/fs/cgroup/memory/memory.kmem.usage_in_bytes");
		}catch(Exception e){
			return Conditional.failure(e);
		}
		List<CgroupMemoryStatsDto> stats = new ArrayList<>();
		stats.add(new CgroupMemoryStatsDto("Total limit", totalLimit));
		stats.add(new CgroupMemoryStatsDto("Total usage", totalUsage));
		stats.add(new CgroupMemoryStatsDto("Kernel usage", kernelMemUsage));
		Scanner.of(lines)
				.include(line -> line.contains("total_"))
				.map(line -> line.split(" "))
				.map(lineParts -> new CgroupMemoryStatsDto(
						StringTool.capitalizeFirstLetter(lineParts[0]).replace("_", " "),
						Long.parseLong(lineParts[1])))
				.forEach(stats::add);
		return Conditional.success(stats);
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
