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
package io.datarouter.metric.dto;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.gauge.GaugeBatchDto;
import io.datarouter.instrumentation.gauge.GaugeDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;

public class GaugeBlobDto{

	private static final String V1 = "v1";

	public final String version;
	public final String serviceName;
	public final String serverName;
	public final List<GaugeBlobItemDto> items;
	public final String apiKey;

	public GaugeBlobDto(GaugeBatchDto gaugeBatchDto, String apiKey){
		this.version = V1;
		Require.notEmpty(gaugeBatchDto.batch);
		this.serviceName = Require.notBlank(gaugeBatchDto.batch.get(0).serviceName);
		this.serverName = Require.notBlank(gaugeBatchDto.batch.get(0).serverName);
		this.items = Scanner.of(gaugeBatchDto.batch)
				.map(GaugeBlobItemDto::new)
				.collect(Collectors.toList());
		this.apiKey = Require.notBlank(apiKey);
	}

	private GaugeBlobDto(String version, String serviceName, String serverName, String apiKey,
			List<GaugeBlobItemDto> items){
		this.version = Require.notBlank(version);
		this.serviceName = Require.notBlank(serviceName);
		this.serverName = Require.notBlank(serverName);
		this.apiKey = Require.notBlank(apiKey);
		this.items = Require.notEmpty(items);
	}

	public static class GaugeBlobItemDto{

		public final String name;
		public final String ulid;
		public final Long value;

		public GaugeBlobItemDto(String name, String ulid, Long value){
			this.name = Require.notBlank(name);
			this.ulid = Require.notBlank(ulid);
			this.value = Require.notNull(value);
		}

		public GaugeBlobItemDto(GaugeDto gaugeDto){
			this(gaugeDto.name, gaugeDto.ulid, gaugeDto.value);
		}

	}

	//make one metadata line and 1+ gauge lines
	public byte[] serializeToBytes(){
		List<String> strings = serializeToStrings(Integer.MAX_VALUE).list();
		Require.equals(1, strings.size());
		return strings.get(0).getBytes(StandardCharsets.UTF_8);
	}

	public Scanner<String> serializeToStrings(int sizeLimit){
		String metadataLine = String.join("\t", List.of(version,
				serviceName,
				serverName,
				apiKey)) + "\n";
		return serializeGaugeItems(sizeLimit - metadataLine.length())
				.map(metadataLine::concat);
	}

	public static GaugeBlobDto deserializeFromString(String string){
		String[] lines = string.split("\n", 2);
		String[] parts = lines[0].split("\t");//metadata line parts
		Require.equals(parts.length, 4);
		Require.equals(parts[0], V1);
		for(int i = 1; i < parts.length; i++){
			Require.notBlank(parts[i]);
		}
		List<GaugeBlobItemDto> items = deserializeGaugeItems(lines[1]);
		return new GaugeBlobDto(
				parts[0],
				parts[1],
				parts[2],
				parts[3],
				items);
	}

	public List<GaugeDto> getGaugeDtos(){
		return Scanner.of(items)
				.map(item -> new GaugeDto(item.name, serviceName, serverName, item.ulid, item.value))
				.list();
	}

	private Scanner<String> serializeGaugeItems(int sizeLimit){
		GaugesSplittingStringBuilders builder = new GaugesSplittingStringBuilders(sizeLimit);
		items.forEach(builder::append);
		return builder.scanSplitGauges();
	}

	private static List<GaugeBlobItemDto> deserializeGaugeItems(String lines){
		return Scanner.of(lines.split("\n"))
				.map(line -> {
					String[] lineParts = line.split("\t");
					return new GaugeBlobItemDto(lineParts[0], lineParts[1], Long.parseLong(lineParts[2]));
				}).list();
	}

	public static class GaugesSplittingStringBuilders{
		private static final Logger logger = LoggerFactory.getLogger(
				GaugeBlobDto.GaugesSplittingStringBuilders.class);

		private static final int NEWLINE_LENGTH = "\n".length();
		private static final int TWO_TABS_LENGTH = "\t".length() * 2;

		private final int maxLength;
		private final List<StringBuilder> splitGauges;

		private StringBuilder currentStringBuilder;

		public GaugesSplittingStringBuilders(int maxLength){
			this.maxLength = maxLength;
			splitGauges = new ArrayList<>();
		}

		//format for each item: <name>\t<ulid>\t<value>, with newlines in between
		public void append(GaugeBlobItemDto item){
			String value = String.valueOf(item.value);
			int itemLength = item.name.length() + item.ulid.length() + value.length() + TWO_TABS_LENGTH;
			//discard items that will never fit
			if(itemLength > maxLength){
				logger.warn("discarding gauge name={} ulid={} value={}", item.name, item.ulid, value);
				return;
			}
			//either this is the first item, or it won't fit in the current builder, so add a new builder
			if(currentStringBuilder == null || currentStringBuilder.length() + itemLength + NEWLINE_LENGTH > maxLength){
				currentStringBuilder = new StringBuilder();
				splitGauges.add(currentStringBuilder);
				appendInternal(item, value, false);
				return;
			}
			appendInternal(item, value, true);
		}

		public Scanner<String> scanSplitGauges(){
			return Scanner.of(splitGauges)
					.map(StringBuilder::toString);
		}

		private void appendInternal(GaugeBlobItemDto item, String value, boolean includeNewline){
			if(includeNewline){
				currentStringBuilder.append("\n");
			}
			currentStringBuilder
					.append(item.name)
					.append("\t")
					.append(item.ulid)
					.append("\t")
					.append(value);
		}

	}

}
