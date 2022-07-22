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
package io.datarouter.filesystem.snapshot.compress;

import io.datarouter.bytes.ByteTool;
import io.datarouter.scanner.Scanner;

public class CompressedBlock{

	public final int totalLength;
	public final byte[][] chunks;

	public CompressedBlock(byte[] bytes){
		this(bytes.length, new byte[][]{bytes});
	}

	public CompressedBlock(byte[][] chunks){
		this(ByteTool.totalLength(chunks), chunks);
	}

	public CompressedBlock(int totalLength, byte[][] chunks){
		this.totalLength = totalLength;
		this.chunks = chunks;
	}

	public Scanner<byte[]> chunkScanner(){
		return Scanner.of(chunks);
	}

}
