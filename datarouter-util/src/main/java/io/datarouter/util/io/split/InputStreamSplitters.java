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
package io.datarouter.util.io.split;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import io.datarouter.util.io.split.InputStreamSplitter.ByteSplitMapper;

public class InputStreamSplitters{

	public static final int CHUNK_KB_256 = 256 * 1024;

	public static final byte DELIMITER_LINE_FEED = 10;
	public static final byte DELIMITER_CARRIAGE_RETURN = 13;

	public static final ByteSplitMapper<byte[]> TO_BYTES = (bytes, start, length) ->
			Arrays.copyOfRange(bytes, start, start + length);
	public static final ByteSplitMapper<String> TO_STRING_US_ASCII = (bytes, start, length) ->
			new String(bytes, start, length, StandardCharsets.US_ASCII);
	public static final ByteSplitMapper<String> TO_STRING_US_ASCII_CR_LF = (bytes, start, length) ->
			new String(bytes, start, length - 2, StandardCharsets.US_ASCII);

	public static final InputStreamSplitter<byte[]> BYTES = new InputStreamSplitter<>(
			DELIMITER_LINE_FEED,
			false,
			TO_BYTES);
	public static final InputStreamSplitter<String> CRLF_SKIP_HEADER = new InputStreamSplitter<>(
			DELIMITER_LINE_FEED,
			true,
			TO_STRING_US_ASCII_CR_LF);

}