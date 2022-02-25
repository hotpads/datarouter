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
package io.datarouter.metric.counter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;
import io.datarouter.util.tuple.Pair;

public class CountBlobDto{

	private static final String
			VERSION = "version",
			ULID = "ulid",
			SERVICE_NAME = "serviceName",
			SERVER_NAME = "serverName",
			API_KEY = "apiKey",
			V1 = "v1";

	public final String version;
	public final String ulid;
	public final String serviceName;
	public final String serverName;
	public final Map<Long,Map<String,Long>> counts;
	public final String apiKey;
	public final String signature;

	public CountBlobDto(String ulid, String serviceName, String serverName, Map<Long,Map<String,Long>> counts,
			String apiKey){
		this.version = V1;
		this.ulid = Require.notBlank(ulid);
		this.serviceName = Require.notBlank(serviceName);
		this.serverName = Require.notBlank(serverName);
		Require.isFalse(counts.isEmpty());
		this.counts = counts;
		this.apiKey = Require.notBlank(apiKey);
		//this depends on some other fields, so keep it at the end
		this.signature = "unused";//TODO make v2 with no signature
	}

	private CountBlobDto(String version, String ulid, String serviceName, String serverName, String apiKey,
			String signature, Map<Long,Map<String,Long>> counts){
		this.version = Require.notBlank(version);
		this.ulid = Require.notBlank(ulid);
		this.serviceName = Require.notBlank(serviceName);
		this.serverName = Require.notBlank(serverName);
		this.counts = counts;
		this.apiKey = Require.notBlank(apiKey);
		this.signature = Require.notBlank(signature);
	}

	//make one metadata line and 1+ count lines
	public byte[] serializeToBytes(){
		return serializeToString().getBytes(StandardCharsets.UTF_8);
	}

	public String serializeToString(){
		String metadataLine = String.join("\t", List.of(version,
				ulid,
				serviceName,
				serverName,
				apiKey,
				signature));
		String countLines = serializeCounts();
		return metadataLine + "\n" + countLines;
	}

	public static CountBlobDto deserializeFromString(String string){
		String[] lines = string.split("\n", 2);
		String[] parts = lines[0].split("\t");//metadata line parts
		Require.equals(parts.length, 6);
		Require.equals(parts[0], V1);
		for(int i = 1; i < parts.length; i++){
			Require.notBlank(parts[i]);
		}
		Map<Long,Map<String,Long>> counts = deserializeCounts(lines[1]);
		return new CountBlobDto(
				parts[0],
				parts[1],
				parts[2],
				parts[3],
				parts[4],
				parts[5],
				counts);
	}

	public Map<String,String> getSignatureMap(){
		return Map.of(
				VERSION, version,
				ULID, ulid,
				SERVICE_NAME, serviceName,
				SERVER_NAME, serverName,
				API_KEY, apiKey);
	}

	private String serializeCounts(){
		return Scanner.of(counts.keySet())
				.exclude(period -> counts.get(period).isEmpty())
				.map(period -> {
					//turn each period map into a single line with <timestamp>\t<name>\t<sum>[\t<name>\t<sum>...]
					return Scanner.of(counts.get(period).entrySet())
							.concatIter(countEntry -> List.of(countEntry.getKey(), countEntry.getValue().toString()))
							.collect(Collectors.joining("\t", period + "\t", ""));
				}).collect(Collectors.joining("\n"));//join each line with \n
	}

	private static Map<Long,Map<String,Long>> deserializeCounts(String lines){
		if("".equals(lines)){
			return Map.of();
		}
		return Scanner.of(lines.split("\n"))
				.map(line -> {
					String[] lineParts = line.split("\t", 2);
					Long timestamp = Long.parseLong(lineParts[0]);
					Map<String,Long> counts = Scanner.of(lineParts[1].split("\t"))
							.batch(2)//kind of hacky. is there a clearer way to do this?
							.collect(Collectors.toMap(pair -> pair.get(0), pair -> Long.parseLong(pair.get(1))));
					return new Pair<>(timestamp, counts);
				})
				.collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
	}

}