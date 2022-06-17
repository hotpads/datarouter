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
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.instrumentation.exception.ExceptionRecordBatchDto;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;

//TODO remove V1 later
public class ExceptionRecordBlobDto{

	private static final String
			V1 = "v1",
			V2 = "v2";

	public final String version;
	public final String serviceName;
	public final String serverName;
	public final String appVersion;
	public final List<ExceptionRecordBlobItemDto> items;
	public final String apiKey;

	public ExceptionRecordBlobDto(ExceptionRecordBatchDto exceptionRecordBatchDto, String apiKey){
		this.version = V2;
		Require.notEmpty(exceptionRecordBatchDto.records);
		this.serviceName = Require.notBlank(exceptionRecordBatchDto.records.get(0).serviceName);
		this.serverName = Require.notBlank(exceptionRecordBatchDto.records.get(0).serverName);
		this.appVersion = Require.notBlank(exceptionRecordBatchDto.records.get(0).appVersion);
		this.items = Scanner.of(exceptionRecordBatchDto.records)
				.map(ExceptionRecordBlobItemDto::new)
				.list();
		this.apiKey = Require.notBlank(apiKey);
	}

	private ExceptionRecordBlobDto(String version, String serviceName, String serverName, String appVersion,
			String apiKey, List<ExceptionRecordBlobItemDto> items){
		this.version = Require.notBlank(version);
		this.serviceName = Require.notBlank(serviceName);
		this.serverName = Require.notBlank(serverName);
		this.appVersion = Require.notBlank(appVersion);
		this.apiKey = Require.notBlank(apiKey);
		this.items = Require.notEmpty(items);
	}

	public static class ExceptionRecordBlobItemDto{

		public final String id;
		public final Date created;
		public final String category;
		public final String name;
		public final String stackTrace;
		public final String type;
		public final String exceptionLocation;
		public final String methodName;
		public final Integer lineNumber;
		public final String callOrigin;

		public ExceptionRecordBlobItemDto(ExceptionRecordDto dto){
			this.id = dto.id;
			this.created = dto.created;
			this.category = dto.category;
			this.name = dto.name;
			this.stackTrace = dto.stackTrace;
			this.type = dto.type;
			this.exceptionLocation = dto.exceptionLocation;
			this.methodName = dto.methodName;
			this.lineNumber = dto.lineNumber;
			this.callOrigin = dto.callOrigin;
		}

	}

	public byte[] serializeToBytes(){
		List<String> strings = serializeToStrings(Integer.MAX_VALUE).list();
		Require.equals(1, strings.size());
		return strings.get(0).getBytes(StandardCharsets.UTF_8);
	}

	public Scanner<String> serializeToStringsOld(int sizeLimit){
		String metadataLine = String.join("\t", List.of(
				V1,
				serviceName,
				serverName,
				appVersion,
				apiKey)) + "\n";
		return serializeExceptionRecordItemsOld(sizeLimit - metadataLine.length())
				.map(metadataLine::concat);
	}

	public Scanner<String> serializeToStrings(int sizeLimit){
		String metadata = toBase64ByteString(String.join("\t", List.of(
				V2,//always serialize as V2
				serviceName,
				serverName,
				appVersion,
				apiKey))) + ",";
		return serializeExceptionRecordItems(sizeLimit - metadata.length())
				.map(metadata::concat);
	}

	public static ExceptionRecordBlobDto deserializeFromString(String string){
		boolean isV1 = string.startsWith(V1 + "\t");
		String[] lines = string.split(isV1 ? "\n" : ",", 2);
		String itemsString;
		//ternary operator does not work, because it evaluates code that can throw an exception
		if(isV1){
			itemsString = lines[0];
		}else{
			itemsString = fromBase64ByteString(lines[0]);
		}
		String[] parts = itemsString.split("\t");//metadata line parts
		Require.equals(parts[0], isV1 ? V1 : V2);
		for(int i = 1; i < parts.length; i++){
			Require.notBlank(parts[i]);
		}
		List<ExceptionRecordBlobItemDto> items;
		//ternary operator does not work, because it evaluates code that can throw an exception
		if(isV1){
			items = deserializeExceptionRecordItemsV1(lines[1]);
		}else{
			items = deserializeExceptionRecordItemsV2(lines[1]);
		}
		return new ExceptionRecordBlobDto(
				V2,//convert to V2 when deserializing any version
				parts[1],
				parts[2],
				parts[3],
				parts[4],
				items);
	}

	private Scanner<String> serializeExceptionRecordItemsOld(int sizeLimit){
		var builder = new ExceptionRecordsSplittingStringBuilders(sizeLimit);
		items.forEach(builder::append);
		return builder.scanSplitItems();
	}

	private Scanner<String> serializeExceptionRecordItems(int sizeLimit){
		var builder = new ExceptionRecordsSplittingStringBuildersV2(sizeLimit);
		items.forEach(builder::append);
		return builder.scanSplitItems();
	}

	private static List<ExceptionRecordBlobItemDto> deserializeExceptionRecordItemsV1(String lines){
		return Scanner.of(lines.split("\n"))
				.map(line -> GsonTool.GSON.fromJson(line, ExceptionRecordBlobItemDto.class))
				.list();
	}

	private static List<ExceptionRecordBlobItemDto> deserializeExceptionRecordItemsV2(String lines){
		return Scanner.of(lines.split(","))
				.map(ExceptionRecordBlobDto::fromBase64ByteString)
				.map(line -> GsonTool.GSON.fromJson(line, ExceptionRecordBlobItemDto.class))
				.list();
	}

	public ExceptionRecordBatchDto getExceptionRecordBatchDto(){
		return Scanner.of(items)
				.map(item -> new ExceptionRecordDto(
						item.id,
						item.created,
						serviceName,
						serverName,
						item.category,
						item.name,
						item.stackTrace,
						item.type,
						appVersion,
						item.exceptionLocation,
						item.methodName,
						item.lineNumber,
						item.callOrigin))
				.listTo(ExceptionRecordBatchDto::new);
	}

	private static String toBase64ByteString(String string){
		return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
	}

	private static String fromBase64ByteString(String base64Bytes){
		return new String(Base64.getDecoder().decode(base64Bytes), StandardCharsets.UTF_8);
	}

	public static class ExceptionRecordsSplittingStringBuilders{
		private static final Logger logger = LoggerFactory.getLogger(
				ExceptionRecordBlobDto.ExceptionRecordsSplittingStringBuilders.class);

		private static final int NEWLINE_LENGTH = "\n".length();

		private final int maxLength;
		private final List<StringBuilder> splitItems;

		private StringBuilder currentStringBuilder;

		public ExceptionRecordsSplittingStringBuilders(int maxLength){
			this.maxLength = maxLength;
			splitItems = new ArrayList<>();
		}

		public void append(ExceptionRecordBlobItemDto item){
			String json = GsonTool.GSON.toJson(item);
			int length = json.length();
			//discard items that will never fit
			if(length > maxLength){
				logger.warn("discarding ExceptionRecordBlobItemDto json={}", json);
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

	public static class ExceptionRecordsSplittingStringBuildersV2{
		private static final Logger logger = LoggerFactory.getLogger(
				ExceptionRecordBlobDto.ExceptionRecordsSplittingStringBuilders.class);

		private static final int COMMA_LENGTH = 1;

		private final int maxLength;
		private final List<StringBuilder> splitItems;

		private StringBuilder currentStringBuilder;

		public ExceptionRecordsSplittingStringBuildersV2(int maxLength){
			this.maxLength = maxLength;
			splitItems = new ArrayList<>();
		}

		public void append(ExceptionRecordBlobItemDto item){
			String json = GsonTool.GSON.toJson(item);
			String serialized = toBase64ByteString(json);
			int length = serialized.length();
			//discard items that will never fit
			if(length > maxLength){
				logger.warn("discarding ExceptionRecordBlobItemDto json={}", json);
				return;
			}
			//either this is the first item, or it won't fit in the current builder, so add a new builder
			if(currentStringBuilder == null || currentStringBuilder.length() + length + COMMA_LENGTH > maxLength){
				currentStringBuilder = new StringBuilder();
				splitItems.add(currentStringBuilder);
				appendInternal(serialized, false);
				return;
			}
			appendInternal(serialized, true);
		}

		public Scanner<String> scanSplitItems(){
			return Scanner.of(splitItems)
					.map(StringBuilder::toString);
		}

		private void appendInternal(String serialized, boolean includeComma){
			if(includeComma){
				currentStringBuilder.append(",");
			}
			currentStringBuilder.append(serialized);
		}

	}

}
