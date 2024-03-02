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
package io.datarouter.bytes.blockfile.test.encoding;

import java.util.Arrays;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileCompressor;

public class BlockfileTestCompressor{

	public static final BlockfileCompressor INSTANCE = new BlockfileCompressor(
			"TEST",
			// Encode as <value> for readability in the output file
			() -> raw -> ByteTool.concat("<".getBytes(), raw, ">".getBytes()),
			() -> encoded -> Arrays.copyOfRange(encoded, 1, encoded.length - 1));

}
