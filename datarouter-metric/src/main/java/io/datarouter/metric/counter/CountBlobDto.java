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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;
import io.datarouter.util.tuple.Pair;

public class CountBlobDto{

	private static final String V1 = "v1";

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
		List<String> strings = serializeToStrings(Integer.MAX_VALUE).list();
		Require.equals(1, strings.size());
		return strings.get(0).getBytes(StandardCharsets.UTF_8);
	}

	public Scanner<String> serializeToStrings(int sizeLimit){
		String metadataLine = String.join("\t", List.of(version,
				ulid,
				serviceName,
				serverName,
				apiKey,
				signature)) + "\n";
		return serializeCounts(sizeLimit - metadataLine.length())
				.map(metadataLine::concat);
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

	private Scanner<String> serializeCounts(int sizeLimit){
		CountsSplittingStringBuilders builder = new CountsSplittingStringBuilders(sizeLimit);
		Scanner.of(counts.keySet())
				.exclude(period -> counts.get(period).isEmpty())
				.forEach(period -> {
					Scanner.of(counts.get(period).entrySet())
							.exclude(entry -> entry.getValue() == null)
							.forEach(countEntry -> builder.append(
									String.valueOf(period),
									countEntry.getKey(),
									String.valueOf(countEntry.getValue())));
				});
		return builder.scanSplitCounts();
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

	public static class CountsSplittingStringBuilders{
		private static final Logger logger = LoggerFactory.getLogger(CountBlobDto.CountsSplittingStringBuilders.class);

		private static final int NEWLINE_LENGTH = "\n".length();
		private static final int TWO_TABS_LENGTH = "\t".length() * 2;

		private final int maxLength;
		private final List<StringBuilder> splitCounts;

		private StringBuilder currentStringBuilder;
		private String currentTimestamp = null;

		public CountsSplittingStringBuilders(int maxLength){
			this.maxLength = maxLength;
			currentStringBuilder = new StringBuilder();
			splitCounts = new ArrayList<>(List.of(currentStringBuilder));
		}

		//final format for each period: <timestamp>\t<name>\t<sum>[\t<name>\t<sum>...]
		//but this format can be split across multiple builders if necessary. each builder will go into a separate blob.
		public void append(String timestamp, String name, String sum){
			Require.notBlank(timestamp);
			Require.notBlank(name);
			Require.notBlank(sum);
			int countLength = getCountLength(name, sum);
			//first count to be appended after initialization
			if(currentTimestamp == null){
				if(shouldDiscard(timestamp, name, sum)){
					return;
				}
				appendCountAndTimestamp(timestamp, name, sum, false);
				return;
			}
			//new count for the same period (line) fits in the current builder
			if(timestamp.equals(currentTimestamp) && currentStringBuilder.length() + countLength <= maxLength){
				appendCount(name, sum);
				return;
			}
			//new count for a new period (line) fits in the current builder
			if(!timestamp.equals(currentTimestamp)
					&& currentStringBuilder.length() + NEWLINE_LENGTH + timestamp.length() + countLength <= maxLength){
				appendCountAndTimestamp(timestamp, name, sum, true);
				return;
			}
			//new count does not fit in the current builder. the new builder needs timestamp but no newline.
			if(shouldDiscard(timestamp, name, sum)){
				return;
			}
			currentStringBuilder = new StringBuilder();
			splitCounts.add(currentStringBuilder);
			appendCountAndTimestamp(timestamp, name, sum, false);
		}

		public Scanner<String> scanSplitCounts(){
			return Scanner.of(splitCounts)
					.map(StringBuilder::toString);
		}

		private void appendCountAndTimestamp(String timestamp, String name, String sum, boolean includeNewline){
			currentTimestamp = timestamp;
			if(includeNewline){
				currentStringBuilder.append("\n");
			}
			currentStringBuilder
					.append(timestamp);
			appendCount(name, sum);
		}

		private void appendCount(String name, String sum){
			currentStringBuilder
					.append("\t")
					.append(name)
					.append("\t")
					.append(sum);
		}

		private int getCountLength(String name, String sum){
			return name.length() + sum.length() + TWO_TABS_LENGTH;
		}

		//size limit only needs to be checked when at the beginning of a new builder, since things that don't
		//fit will always end up there
		private boolean shouldDiscard(String timestamp, String name, String sum){
			if(timestamp.length() + getCountLength(name, sum) > maxLength){
				logger.warn("discarding count timestamp={} name={} sum={}", timestamp, name, sum);
				return true;
			}
			return false;
		}

	}

}