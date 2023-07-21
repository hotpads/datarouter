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
package io.datarouter.bytes.blockfile.checksum;

import java.util.List;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.scanner.Scanner;

public class BlockfileStandardChecksummers{

	// If needed, could add MD5 or SHA, but those could also be applied to the whole file and stored externally.
	public static final BlockfileChecksummer
			NONE = new BlockfileChecksummer(
					"NONE",
					0,
					$ -> EmptyArray.BYTE),
			ADLER_32 = new BlockfileChecksummer(
					"ADLER_32",
					4,
					data -> BlockfileChecksumTool.checksum32(new Adler32(), data)),
			CRC_32 = new BlockfileChecksummer(
					"CRC_32",
					4,
					data -> BlockfileChecksumTool.checksum32(new CRC32(), data));

	public static final List<BlockfileChecksummer> ALL = List.of(NONE, ADLER_32, CRC_32);
	public static final Map<String,BlockfileChecksummer> BY_ENCODED_NAME = Scanner.of(ALL)
			.toMap(BlockfileChecksummer::encodedName);

}
