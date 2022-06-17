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
package io.datarouter.trace.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.instrumentation.trace.Trace2BundleDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;

public class TraceBlobDto{

	private static final String V1 = "v1";

	public final String apiKey;
	public final List<Trace2BundleDto> traceBundles;

	public TraceBlobDto(String apiKey, List<Trace2BundleDto> traceBundles){
		this.apiKey = Require.notBlank(apiKey);
		this.traceBundles = Require.notEmpty(traceBundles);
	}

	public byte[] serializeToBytes(){
		List<String> strings = serializeToStrings(Integer.MAX_VALUE).list();
		Require.equals(1, strings.size());
		return strings.get(0).getBytes(StandardCharsets.UTF_8);
	}

	public Scanner<String> serializeToStrings(int sizeLimit){
		String metadata = toBase64ByteString(String.join("\t", List.of(V1, apiKey))) + ",";
		return serializeTraceBundles(sizeLimit - metadata.length())
				.map(metadata::concat);
	}

	public static TraceBlobDto deserializeFromString(String string){
		String[] lines = string.split(",", 2);
		String metadataString = fromBase64ByteString(lines[0]);
		String[] parts = metadataString.split("\t");//metadata line parts
		Require.equals(parts[0], V1);
		for(int i = 1; i < parts.length; i++){
			Require.notBlank(parts[i]);
		}
		List<Trace2BundleDto> items;
		items = deserializeTraceBundles(lines[1]);
		return new TraceBlobDto(
				parts[1],
				items);
	}

	private Scanner<String> serializeTraceBundles(int sizeLimit){
		var builder = new TracesSplittingStringBuilders(sizeLimit);
		traceBundles.forEach(builder::append);
		return builder.scanSplitItems();
	}

	private static List<Trace2BundleDto> deserializeTraceBundles(String lines){
		return Scanner.of(lines.split(","))
				.map(TraceBlobDto::fromBase64ByteString)
				.map(line -> GsonTool.GSON.fromJson(line, Trace2BundleDto.class))
				.list();
	}

	private static String toBase64ByteString(String string){
		return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
	}

	private static String fromBase64ByteString(String base64Bytes){
		return new String(Base64.getDecoder().decode(base64Bytes), StandardCharsets.UTF_8);
	}

	public static class TracesSplittingStringBuilders{
		private static final Logger logger = LoggerFactory.getLogger(TraceBlobDto.TracesSplittingStringBuilders.class);

		private static final int COMMA_LENGTH = 1;

		private final int maxLength;
		private final List<StringBuilder> splitItems;

		private StringBuilder currentStringBuilder;

		public TracesSplittingStringBuilders(int maxLength){
			this.maxLength = maxLength;
			splitItems = new ArrayList<>();
		}

		public void append(Trace2BundleDto item){
			String json = GsonTool.GSON.toJson(item);
			String serialized = toBase64ByteString(json);
			int length = serialized.length();
			//discard items that will never fit
			if(length > maxLength){
				logger.warn("discarding Trace2BundleDto with length={}", json.length());
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
