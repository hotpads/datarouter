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
package io.datarouter.exception.dto;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.instrumentation.exception.HttpRequestRecordBatchDto;
import io.datarouter.instrumentation.exception.HttpRequestRecordDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;

public class HttpRequestRecordBlobDto{

	private static final String V1 = "v1";

	public final String version;
	public final List<HttpRequestRecordDto> items;
	public final String apiKey;

	public HttpRequestRecordBlobDto(HttpRequestRecordBatchDto httpRequestRecordBatchDto, String apiKey){
		this(V1, apiKey, List.copyOf(httpRequestRecordBatchDto.records));
	}

	private HttpRequestRecordBlobDto(String version, String apiKey, List<HttpRequestRecordDto> items){
		this.version = Require.notBlank(version);
		this.apiKey = Require.notBlank(apiKey);
		this.items = Require.notEmpty(items);
	}

	public byte[] serializeToBytes(){
		List<String> strings = serializeToStrings(Integer.MAX_VALUE).list();
		Require.equals(1, strings.size());
		return strings.get(0).getBytes(StandardCharsets.UTF_8);
	}

	public Scanner<String> serializeToStrings(int sizeLimit){
		String metadataLine = String.join("\t", List.of(version, apiKey)) + "\n";
		return serializeHttpRequestRecordItems(sizeLimit - metadataLine.length())
				.map(metadataLine::concat);
	}

	public static HttpRequestRecordBlobDto deserializeFromString(String string){
		String[] lines = string.split("\n", 2);
		String[] parts = lines[0].split("\t");//metadata line parts
		Require.equals(parts.length, 2);
		Require.equals(parts[0], V1);
		for(int i = 1; i < parts.length; i++){
			Require.notBlank(parts[i]);
		}
		List<HttpRequestRecordDto> items = deserializeHttpRequestRecordItems(lines[1]);
		return new HttpRequestRecordBlobDto(parts[0], parts[1], items);
	}

	private Scanner<String> serializeHttpRequestRecordItems(int sizeLimit){
		HttpRequestRecordsSplittingStringBuilders builder = new HttpRequestRecordsSplittingStringBuilders(sizeLimit);
		items.forEach(builder::append);
		return builder.scanSplitItems();
	}

	private static List<HttpRequestRecordDto> deserializeHttpRequestRecordItems(String lines){
		return Scanner.of(lines.split("\n"))
				.map(line -> GsonTool.GSON.fromJson(line, HttpRequestRecordDto.class))
				.list();
	}

	public static class HttpRequestRecordsSplittingStringBuilders{
		private static final Logger logger = LoggerFactory.getLogger(
				HttpRequestRecordBlobDto.HttpRequestRecordsSplittingStringBuilders.class);

		private static final int NEWLINE_LENGTH = "\n".length();

		private final int maxLength;
		private final List<StringBuilder> splitItems;

		private StringBuilder currentStringBuilder;

		public HttpRequestRecordsSplittingStringBuilders(int maxLength){
			this.maxLength = maxLength;
			splitItems = new ArrayList<>();
		}

		//format for each item: <name>\t<ulid>\t<value>, with newlines in between
		public void append(HttpRequestRecordDto item){
			String json = GsonTool.GSON.toJson(item);
			int length = json.length();
			//discard items that will never fit
			if(length > maxLength){
				logger.warn("discarding HttpRequestRecordDto json={}", json);
				return;
			}
			//either this is the first item, or it won't fit in the current builder, so add a new builder
			if(currentStringBuilder == null || currentStringBuilder.length() + length + NEWLINE_LENGTH > maxLength){
				currentStringBuilder = new StringBuilder();
				splitItems.add(currentStringBuilder);
				appendInternal(json, false);
				return;
			}
			appendInternal(json, true);
		}

		public Scanner<String> scanSplitItems(){
			return Scanner.of(splitItems)
					.map(StringBuilder::toString);
		}

		private void appendInternal(String json, boolean includeNewline){
			if(includeNewline){
				currentStringBuilder.append("\n");
			}
			currentStringBuilder.append(json);
		}

	}

}
