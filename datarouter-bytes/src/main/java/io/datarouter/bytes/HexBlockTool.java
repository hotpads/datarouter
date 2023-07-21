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
package io.datarouter.bytes;

import java.io.PrintStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.datarouter.bytes.codec.bytestringcodec.HexByteStringCodec;

public class HexBlockTool{

	public static String toHexBlock(byte[] bytes, int numTabs, int width){
		if(width % 2 != 0){
			throw new IllegalArgumentException("Please specify an even width");
		}
		String tabs = IntStream.range(0, numTabs)
				.mapToObj($ -> "\t")
				.collect(Collectors.joining(""));
		String hex = HexByteStringCodec.INSTANCE.encode(bytes);
		var sb = new StringBuilder();
		for(int i = 0; i < hex.length(); i += width){
			if(i > 0){
				sb.append("\n");
			}
			int from = i;
			int to = Math.min(hex.length(), from + width);
			sb.append(tabs);
			String line = hex.substring(from, to);
			sb.append(line);
		}
		return sb.toString();
	}

	public static void print(byte[] bytes, int numTabs, int width){
		PrintStream out = System
				.out;// Checkstyle hack
		String header = String.format("##### hex start tabs=%s width=%s #####", numTabs, width);
		String body = toHexBlock(bytes, numTabs, width);
		String footer = "##### hex end #####";
		out.println(header);
		out.println(body);
		out.println(footer);
	}

	public static void print(byte[] bytes){
		print(bytes, 0, 80);
	}

	public static byte[] fromHexBlock(String hexBlock){
		String trimmed = trim(hexBlock);
		return HexByteStringCodec.INSTANCE.decode(trimmed);
	}

	public static String trim(String hexBlock){
		String[] lines = hexBlock.split("\n");
		return String.join("", lines);
	}

}
